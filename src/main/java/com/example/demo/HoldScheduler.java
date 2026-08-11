package com.example.demo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class HoldScheduler {
    private final LedgerRepo ledgerRepo;
    private  final TransferService transferService;

    public HoldScheduler(LedgerRepo ledgerRepo,TransferService transferService){
        this.ledgerRepo=ledgerRepo;
        this.transferService=transferService;
    }

    @Scheduled(fixedDelay=30000)
    public void cleanExpiredHolds(){
        Instant now=Instant.now();
        System.out.println("Checking");
        List<LedgerEntry> expiredHolds=ledgerRepo.findExpiredUnsettledHolds(now);

        if (expiredHolds.isEmpty()) {
            return;
        }
        System.out.println("Needs wiping");
        for (LedgerEntry hold :expiredHolds){
            try{
                transferService.voidHold(hold.getAccountId(),hold.getAmount().abs(),hold.getTransferId());
                System.out.println("Successfully auto-voided expired hold [" + hold.getTransferId() + "]");
            } catch (Exception e) {
                System.err.println("Failed to auto-void expired hold [" + hold.getTransferId() + "]: " + e.getMessage());
            }
        }

    }
}

