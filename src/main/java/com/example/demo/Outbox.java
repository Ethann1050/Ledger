package com.example.demo;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class Outbox {
    @Id
    @GeneratedValue
    private Long id;

    private String actionType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String actionId;

    private String eventType;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private Instant createdAt;

    private Instant processedAt;

    private int retries;

    protected Outbox() {}

    public Outbox(String actionType, String actionId, String eventType, String payload, int retries) {
        this.actionType = actionType;
        this.actionId = actionId;
        this.eventType = eventType;
        this.payload=payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
        this.retries=retries;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public int getRetries() {return retries;}
    public String getPayload() {return payload;}
    public String getactionType() { return actionType; }
    public String getactionId() { return actionId; }
    public String getEventType() { return eventType; }
    public OutboxStatus getStatus() { return status; }
    public void setStatus(OutboxStatus status) { this.status = status; }
    public void increaseRetry(){this.retries=this.retries+1;}
    public Instant getCreatedAt() { return createdAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }



}
