package com.example.demo;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransferService {

    private final Repo<Account,Long> accountRepo;
    private final Repo<LedgerEntry,Long> ledgerRepo;
    private final Repo<ProcessedTransfer,String> processedTransferRepo;

    public TransferService (Repo<Account,Long> accountRepo, Repo<LedgerEntry,Long> ledgerRepo, Repo<ProcessedTransfer,String> processedTransferRepo){
        this.accountRepo=accountRepo;
        this.ledgerRepo=ledgerRepo;
        this.processedTransferRepo = processedTransferRepo;
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount, String IdempotencyKey){

        Account from;
        Account to;

        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (processedTransferRepo.findById(IdempotencyKey).isPresent()){
            return;
        }

        Long firstId = Math.min(fromId, toId);
        Long secondId = Math.max(fromId, toId);

        Account first = accountRepo.findById(firstId).orElseThrow();  // local
        Account second = accountRepo.findById(secondId).orElseThrow();      // local

        if  (fromId.equals(firstId)){
             from =first;
             to = second;
        }
        else {
             from =second;
             to = first;
        }


        from.debit(amount);
        to.credit(amount);
        accountRepo.save(from);
        accountRepo.save(to);

        String transferId = UUID.randomUUID().toString();

        LedgerEntry debitEntry = new LedgerEntry(from, amount.negate(), TransactionType.DEBIT, Instant.now(), transferId);
        LedgerEntry creditEntry = new LedgerEntry(to, amount, TransactionType.CREDIT, Instant.now(), transferId);

        ProcessedTransfer processedTransfer= new ProcessedTransfer(IdempotencyKey,Instant.now());

        ledgerRepo.save(debitEntry);
        ledgerRepo.save(creditEntry);

        processedTransferRepo.save(processedTransfer);

    }
}
