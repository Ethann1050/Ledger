package com.example.demo;

import java.math.BigDecimal;

public class Account {

    private final Long id;
    private BigDecimal balance;
    private final String ownerName;

    public Account(Long id, BigDecimal balance, String ownerName){
        this.id = id;
        this.balance=balance;
        this.ownerName = ownerName;
    }

    public Long getId(){
        return id;
    }
    public BigDecimal getBalance(){
        return balance;
    }
    public String getOwner(){
        return ownerName;
    }

    public void credit(BigDecimal amount){this.balance=balance.add(amount);}
}
