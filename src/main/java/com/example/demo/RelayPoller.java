package com.example.demo;



import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class RelayPoller {

    private final OutboxRepo outboxRepo;


    public RelayPoller(OutboxRepo outboxRepo) {
        this.outboxRepo = outboxRepo;

    }


    @Scheduled(fixedDelay=10000)
    public void checkForPending()  {
        List<Outbox> pendingOutboxes=outboxRepo.findPendingBatch(10);
        System.out.println("Pending search");
        if (!pendingOutboxes.isEmpty()){
            for (Outbox outbox : pendingOutboxes){
                try {
                    sendToRabbitMQ(outbox);
                    updateOutboxStatus(outbox,OutboxStatus.SENT);
                } catch (Exception e){
                    System.out.println("Failed to send outbox entry ID {} to Kafka");
                    updateOutboxStatus(outbox, OutboxStatus.FAILED);
                }



            }
        }
    }

    private void sendToRabbitMQ(Outbox outbox) throws ExecutionException, InterruptedException {
        return;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateOutboxStatus(Outbox outbox ,OutboxStatus status){
        outbox.setStatus(status);
        outboxRepo.save(outbox);

    }

}
