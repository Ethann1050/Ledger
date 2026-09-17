package com.example.demo;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class AccountService {
    private final Repo<Account,Long> accountRepo;
    private final ProcessedTransferRepo processedTransferRepo;

    public AccountService(Repo<Account,Long> accountRepo, ProcessedTransferRepo processedTransferRepo) {
        this.accountRepo = accountRepo;
        this.processedTransferRepo=processedTransferRepo;
    }
    @Transactional
    public int createAccount(BigDecimal balance, String ownerName, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be null or blank");
        }

        if (!processedTransferRepo.tryClaim(idempotencyKey, Instant.now())) {
            return 1;
    }
        Account account = new Account(balance, ownerName);
        accountRepo.save(account);
        return 0;
    }
}
