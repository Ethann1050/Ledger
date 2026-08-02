package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.Instant;


@Entity
public class ProcessedTransfer {
    @Id
    private String IdempotencyKey;
    private Instant ProcessedAt;

    public ProcessedTransfer (String idempotencyKey, Instant processedAt){
        this.IdempotencyKey=idempotencyKey;
        this.ProcessedAt=processedAt;
    }

    protected ProcessedTransfer(){}

    public Instant getProcessedAt() {
        return ProcessedAt;
    }

    public String getIdempotencyKey() {
        return IdempotencyKey;
    }


}
