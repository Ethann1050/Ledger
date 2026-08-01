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
        transferService.transfer(request.getFromId(), request.getToId(), request.getAmount());
        return ResponseEntity.ok("Transfer All Good");
    }

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request.getBalance(), request.getOwner());
        return ResponseEntity.ok(account);
    }
}
