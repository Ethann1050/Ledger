package com.example.demo;

public class CommitHoldRequest {
    private String transferId;
    private Long accountId;
    private String idempotencyKey;

    public CommitHoldRequest (String transferId, Long accountId, String idempotencyKey){
        this.transferId=transferId;
        this.accountId=accountId;
        this.idempotencyKey=idempotencyKey;

    }

    public Long getAccountId() {
        return accountId;
    }

    public String getTransferId() {
        return transferId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
