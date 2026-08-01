package com.example.demo;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransferService {

    private final Repo<Account> accountRepo;
    private final Repo<LedgerEntry> ledgerRepo;

    public TransferService (Repo<Account> accountRepo, Repo<LedgerEntry> ledgerRepo){
        this.accountRepo=accountRepo;
        this.ledgerRepo=ledgerRepo;
    }

    public void transfer(Long fromId, Long toId, BigDecimal amount){
        Account from = accountRepo.findById(fromId).orElseThrow();  // local
        Account to = accountRepo.findById(toId).orElseThrow();      // local
        from.debit(amount);
        to.credit(amount);
        accountRepo.save(from);
        accountRepo.save(to);

        String transferId = UUID.randomUUID().toString();

        LedgerEntry debitEntry = new LedgerEntry(from, amount, TransactionType.DEBIT, Instant.now(), transferId);
        LedgerEntry creditEntry = new LedgerEntry(to, amount, TransactionType.CREDIT, Instant.now(), transferId);

        ledgerRepo.save(debitEntry);
        ledgerRepo.save(creditEntry);

    }
}
