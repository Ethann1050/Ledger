package com.example.demo;

import java.math.BigDecimal;

public class CreateAccountRequest {
    private BigDecimal balance;
    private String ownerName;
    private String idempotencyKey;

    public CreateAccountRequest(){};

    public BigDecimal getBalance(){
        return balance;
    }
    public String getOwner(){
        return ownerName;
    }

    public String getIdempotencyKey() {return idempotencyKey;}

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
