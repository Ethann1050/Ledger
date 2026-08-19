package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class RelayPoller {

    private final OutboxRepo outboxRepo;

    public RelayPoller(OutboxRepo outboxRepo) {
        this.outboxRepo = outboxRepo;
    }


    @Scheduled(fixedDelay=10000)
    @Transactional
    public void checkForPending(){
        List<Outbox> pendingOutboxes=outboxRepo.findPendingBatch(10);
        if (!pendingOutboxes.isEmpty()){
            for (Outbox outbox : pendingOutboxes){
                sendToKafka(outbox);
                updateOutboxStatus(outbox, OutboxStatus.SENT);
            }
        }
    }

    private void sendToKafka(Outbox outbox){

    }

    public void receiveFromKafka(){}


    private void updateOutboxStatus(Outbox outbox, OutboxStatus status){
        outbox.setStatus(status);
        outboxRepo.save(outbox);

    }

}
