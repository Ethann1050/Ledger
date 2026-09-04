package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Check(constraints="balance>=0")
public class Account {
    @Id
    @GeneratedValue
    private Long accountId;
    private BigDecimal balance;
    private BigDecimal pendingDebits=BigDecimal.ZERO;
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
    public BigDecimal getPendingDebts(){return pendingDebits;}

    public BigDecimal getAvailableBalance(){return this.getBalance().subtract(this.getPendingDebts());}

    public void credit(BigDecimal amount){this.balance=this.balance.add(amount);}
    public void debit(BigDecimal amount){
        if (amount.compareTo(this.balance) > 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }


    public void addPendingDebit(BigDecimal amount) {
        if (amount.compareTo(getAvailableBalance()) > 0) {
            throw new IllegalStateException("Insufficient available funds for hold");
        }
        this.pendingDebits = this.pendingDebits.add(amount);
    }

    public void commitPendingDebit(BigDecimal amount) {
        if (amount.compareTo(this.pendingDebits) > 0) {
            throw new IllegalStateException("Cannot commit more than current pending debits");
        }
        this.pendingDebits = this.pendingDebits.subtract(amount);
        this.balance = this.balance.subtract(amount);
    }

    public void voidPendingDebit(BigDecimal amount) {
        if (amount.compareTo(this.pendingDebits) > 0) {
            throw new IllegalStateException("Cannot void more than current pending debits");
        }
        this.pendingDebits = this.pendingDebits.subtract(amount);
    }
}

