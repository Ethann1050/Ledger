package com.example.demo;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class RelayPoller {

    private final OutboxRepo outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public RelayPoller(OutboxRepo outboxRepo, KafkaTemplate<String,String> kafkaTemplate) {
        this.outboxRepo = outboxRepo;
        this.kafkaTemplate=kafkaTemplate;
    }


    @Scheduled(fixedDelay=10000)
    public void checkForPending()  {
        List<Outbox> pendingOutboxes=outboxRepo.findPendingBatch(10);
        if (!pendingOutboxes.isEmpty()){
            for (Outbox outbox : pendingOutboxes){
                try {
                    sendToKafka(outbox);
                    updateOutboxStatus(outbox,OutboxStatus.SENT);
                } catch (Exception e){
                    System.out.println("Failed to send outbox entry ID {} to Kafka");
                    updateOutboxStatus(outbox, OutboxStatus.FAILED);
                }



            }
        }
    }

    private void sendToKafka(Outbox outbox) throws ExecutionException, InterruptedException {
        kafkaTemplate.send("ledger-events",outbox.getactionId(),outbox.getPayload()).get();
    }

    private void updateOutboxStatus(Outbox outbox ,OutboxStatus status){
        outbox.setStatus(status);
        outboxRepo.save(outbox);

    }

}
