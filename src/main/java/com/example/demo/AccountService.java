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
    public Account createAccount(BigDecimal balance, String ownerName, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be null or blank");
        }

//        if (!processedTransferRepo.tryClaim(idempotencyKey, Instant.now())) {
//            return accountRepo.findByIdempotencyKey(idempotencyKey)
//                .orElseThrow(() -> new IllegalStateException("Duplicate request in progress or failed"));
//    }
        Account account = new Account(balance, ownerName);
        return accountRepo.save(account);
    }
}
