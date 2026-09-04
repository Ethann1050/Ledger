package com.example.demo;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransferService {

    private final Repo<Account, Long> accountRepo;
    private final LedgerRepo ledgerRepo;
    private final ProcessedTransferRepo processedTransferRepo;
    private final OutboxRepo outboxRepo;

    public TransferService(Repo<Account, Long> accountRepo, LedgerRepo ledgerRepo, ProcessedTransferRepo processedTransferRepo, OutboxRepo outboxRepo) {
        this.accountRepo = accountRepo;
        this.ledgerRepo = ledgerRepo;
        this.processedTransferRepo = processedTransferRepo;
        this.outboxRepo=outboxRepo;
    }

    @Transactional
    public int transfer(Long fromId, Long toId, BigDecimal amount, String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be null or blank");
        }

        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }


        if (!processedTransferRepo.tryClaim(idempotencyKey, Instant.now())) {
            return 1;
        }

        Account from;
        Account to;

        Long firstId = Math.min(fromId, toId);
        Long secondId = Math.max(fromId, toId);

        Account first = accountRepo.findById(firstId).orElseThrow();
        Account second = accountRepo.findById(secondId).orElseThrow();

        if (fromId.equals(firstId)) {
            from = first;
            to = second;
        } else {
            from = second;
            to = first;
        }

        from.debit(amount);
        to.credit(amount);
        accountRepo.save(from);
        accountRepo.save(to);

        String transferId = UUID.randomUUID().toString();

        LedgerEntry debitEntry = new LedgerEntry(from, amount.negate(), TransactionType.DEBIT, Instant.now(), transferId,null);
        LedgerEntry creditEntry = new LedgerEntry(to, amount, TransactionType.CREDIT, Instant.now(), transferId,null);

        ledgerRepo.save(debitEntry);
        ledgerRepo.save(creditEntry);

        // Save Outbox Event
        String payload = String.format(
                "{\"transferId\":\"%s\",\"fromId\":%d,\"toId\":%d,\"amount\":%s}",
                transferId, fromId, toId, amount
        );
        Outbox outbox = new Outbox("TRANSFER", fromId.toString(), "TRANSFER_COMPLETED", payload);
        outboxRepo.save(outbox);

        return 0;

    }

    @Transactional
    public String holdTransfer(Long fromId, Long toId, BigDecimal amount, String idempotencyKey, Duration holdDuration) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be null or blank");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (!processedTransferRepo.tryClaim(idempotencyKey, Instant.now())) {
            // Returns existing transfer ID or reference if re-submitted
            return idempotencyKey;
        }

        Long firstId = Math.min(fromId, toId);
        Long secondId = Math.max(fromId, toId);

        Account first = accountRepo.findById(firstId).orElseThrow();
        Account second = accountRepo.findById(secondId).orElseThrow();

        Account from;
        Account to;



        if (fromId.equals(firstId)) {
            from = first;
            to = second;
        } else {
            from = second;
            to = first;
        }

        // Reserves funds on sender account (availableBalance drops immediately)
        from.addPendingDebit(amount);
        accountRepo.save(from);

        String transferId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        // Write HOLD_RESERVE to ledger
        LedgerEntry reserveEntry = new LedgerEntry(from, amount.negate(), TransactionType.HOLD_RESERVE, now, transferId, now.plus(holdDuration));
        ledgerRepo.save(reserveEntry);

        // Save Outbox Event
        String payload = String.format(
                "{\"transferId\":\"%s\",\"fromId\":%d,\"toId\":%d,\"amount\":%s,\"expiresAt\":\"%s\"}",
                transferId, fromId, toId, amount, now.plus(holdDuration)
        );
        Outbox outbox = new Outbox("HOLD", fromId.toString(), "HOLD_RESERVED", payload);
        outboxRepo.save(outbox);

        return transferId;
    }

    @Transactional
    public void commitHold(Long toId, String transferId,String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (!processedTransferRepo.tryClaim(idempotencyKey, Instant.now())) {
                return; // Idempotency key already claimed
            }
        }
        List<LedgerEntry> entries=ledgerRepo.findByTransferIdAndType(transferId, TransactionType.HOLD_RESERVE);

        if (entries.isEmpty()) {
            throw new IllegalArgumentException("Original hold reserve not found for transferId: " + transferId);
        }

        Long fromId=entries.get(0).getAccountId();
        BigDecimal amount=entries.get(0).getAmount().abs();


        Long firstId = Math.min(fromId, toId);
        Long secondId = Math.max(fromId, toId);

        Account first = accountRepo.findById(firstId).orElseThrow();
        Account second = accountRepo.findById(secondId).orElseThrow();

        Account from = fromId.equals(firstId) ? first : second;
        Account to = fromId.equals(firstId) ? second : first;

        from.commitPendingDebit(amount);
        to.credit(amount);

        accountRepo.save(from);
        accountRepo.save(to);

        Instant now = Instant.now();
        LedgerEntry debitEntry = new LedgerEntry(from, amount.negate(), TransactionType.HOLD_COMMIT, now, transferId,null);
        LedgerEntry creditEntry = new LedgerEntry(to, amount, TransactionType.HOLD_COMMIT, now, transferId,null);

        ledgerRepo.save(debitEntry);
        ledgerRepo.save(creditEntry);

        // Save Outbox Event
        String payload = String.format(
                "{\"transferId\":\"%s\",\"fromId\":%d,\"toId\":%d,\"amount\":%s}",
                transferId, fromId, toId, amount
        );
        Outbox outbox = new Outbox("HOLD", fromId.toString(), "HOLD_COMMITTED", payload);
        outboxRepo.save(outbox);
    }

    @Transactional
    public void voidHold(Long fromId, BigDecimal amount, String transferId) {
        Account from = accountRepo.findById(fromId).orElseThrow();

        from.voidPendingDebit(amount);
        accountRepo.save(from);

        LedgerEntry voidEntry = new LedgerEntry(from, amount, TransactionType.HOLD_VOID, Instant.now(), transferId,null);
        ledgerRepo.save(voidEntry);

        String payload = String.format(
                "{\"transferId\":\"%s\",\"fromId\":%d,\"amount\":%s}",
                transferId, fromId, amount
        );
        Outbox outbox = new Outbox("HOLD", fromId.toString(), "HOLD_VOIDED", payload);
        outboxRepo.save(outbox);
    }
}

