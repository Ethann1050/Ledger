package com.example.demo;
import java.math.BigDecimal;
import java.time.Duration;

public class holdTransferRequest {

    private Long fromId;
    private Long toId;
    private BigDecimal amount;
    private String idempotencyKey;
    private Duration duration;

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

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
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

        public void setIdempotencyKey(String idempotencyKey){this.idempotencyKey= idempotencyKey;
        }
    }


