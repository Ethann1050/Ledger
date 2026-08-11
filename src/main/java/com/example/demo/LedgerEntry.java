package com.example.demo;

import jakarta.persistence.*;
import org.springframework.jmx.export.naming.IdentityNamingStrategy;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class LedgerEntry {
    @Id
    @GeneratedValue
    private Long ledgerId;
    @ManyToOne
    @JoinColumn(name = "account_Id")
    private Account account;
    private BigDecimal amount;
    private TransactionType type;
    private Instant timestamp;
    private String transferId;
    private Instant expiresAt;

    public LedgerEntry(Account account,BigDecimal amount,TransactionType type,Instant timestamp,String transferId, Instant expiresAt){
        this.account=account;
        this.amount=amount;
        this.type=type;
        this.timestamp=timestamp;
        this.transferId=transferId;
        this.expiresAt=expiresAt;
    }

    protected LedgerEntry(){}

    public Long getLedgerId() {return ledgerId;}
    public Long getAccountId() {return account.getId();}
    public BigDecimal getAmount() {return amount;}
    public TransactionType getType() {return type;}
    public Instant getTimestamp() {return timestamp;}
    public String getTransferId() {return transferId;}
    public Instant getExpiresAt(){return expiresAt;}

}
