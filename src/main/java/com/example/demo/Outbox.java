package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class Outbox {
    @Id
    @GeneratedValue
    private Long id;

    private String actionType;

    private String actionId;

    private String eventType;

    private OutboxStatus status;

    private Instant createdAt;

    private Instant processedAt;

    public Outbox(String actionType, String actionId, String eventType, String payload) {
        this.actionType = actionType;
        this.actionId = actionId;
        this.eventType = eventType;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getactionType() { return actionType; }
    public String getactionId() { return actionId; }
    public String getEventType() { return eventType; }
    public OutboxStatus getStatus() { return status; }
    public void setStatus(OutboxStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }



}
