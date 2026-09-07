package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {
    private final TransferService transferService;
    private final AccountService accountService;

    public AccountController(TransferService transferService, AccountService accountService){
        this.transferService=transferService;
        this.accountService = accountService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transferExecution(@RequestBody TransferRequest request){
        if (transferService.transfer(request.getFromId(), request.getToId(), request.getAmount(), request.getIdempotencyKey())==1){
            return ResponseEntity.ok ("Transfer already done, duplicate idempotency key");
        }
        return ResponseEntity.ok("Transfer All Good");
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request.getBalance(), request.getOwner(), request.getIdempotencyKey());
        return ResponseEntity.ok(account);
    }

    @PostMapping("/holdtransfer")
    public ResponseEntity<String> holdTransfer(@RequestBody holdTransferRequest request) {
        String Response=transferService.holdTransfer(request.getFromId(), request.getToId(), request.getAmount(), request.getIdempotencyKey(), request.getDuration());
        if (request.getIdempotencyKey().equals(Response)){
            return ResponseEntity.ok("Transfer already happened or exists");
        }
        return ResponseEntity.ok("Transfer is being held"+ "transferId is"+ Response);
    }

    @PostMapping("/commit")
    public ResponseEntity<String> commitHold(@RequestBody CommitHoldRequest request){
        transferService.commitHold(request.getAccountId(), request.getTransferId(), request.getIdempotencyKey());
        return ResponseEntity.ok("Hold committed successfully");
    }

}
