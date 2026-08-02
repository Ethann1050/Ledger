package com.example.demo;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferRequest {
    private Long fromId;
    private Long toId;
    private BigDecimal amount;
    private String idempotencyKey;

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

    public String getIdempotencyKey(){return idempotencyKey;}

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setFromId(Long fromId) {
        this.fromId = fromId;
    }

    public void setToId(Long toId) {
        this.toId = toId;
    }

    public void setIdempotencyKey(String idempotencyKey){this.idempotencyKey= idempotencyKey;
    }
}
