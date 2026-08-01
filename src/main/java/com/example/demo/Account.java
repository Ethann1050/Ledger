package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class Account {
    @Id
    @GeneratedValue
    private Long accountId;
    private BigDecimal balance;
    private String ownerName;

    public Account(BigDecimal balance, String ownerName){
        this.balance=balance;
        this.ownerName = ownerName;
    }
    protected Account(){}

    public Long getId(){
        return accountId;
    }
    public BigDecimal getBalance(){
        return balance;
    }
    public String getOwner(){
        return ownerName;
    }

    public void credit(BigDecimal amount){this.balance=balance.add(amount);}
    public void debit(BigDecimal amount){
        if (amount.compareTo(this.balance) > 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }
}

