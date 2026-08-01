package com.example.demo;

import java.math.BigDecimal;

public class CreateAccountRequest {

    private Long accountId;
    private BigDecimal balance;
    private String ownerName;

    public CreateAccountRequest(){};

    public Long getId(){
        return accountId;
    }
    public BigDecimal getBalance(){
        return balance;
    }
    public String getOwner(){
        return ownerName;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
