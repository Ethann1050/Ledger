package com.example.demo;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {
    private final Repo<Account> accountRepo;

    public AccountService(Repo<Account> accountRepo) {
        this.accountRepo = accountRepo;
    }
    @Transactional
    public Account createAccount(BigDecimal balance, String ownerName) {
        Account account = new Account(balance, ownerName);
        return accountRepo.save(account);
    }
}
