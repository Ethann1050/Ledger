package com.example.demo;

import java.math.BigDecimal;

public class TransferRequest {
    private Long fromId;
    private Long toId;
    private BigDecimal amount;

    public TransferRequest(){};

    public Long getFromId() {
        return fromId;
    }

    public Long getToId() {
        return toId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setFromId(Long fromId) {
        this.fromId = fromId;
    }

    public void setToId(Long toId) {
        this.toId = toId;
    }
}
