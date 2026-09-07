package com.example.demo;



import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class RelayPoller {

    private final OutboxRepo outboxRepo;
    private final RabbitTemplate rabbitTemplate;


    public RelayPoller(OutboxRepo outboxRepo, RabbitTemplate rabbitTemplate) {
        this.outboxRepo = outboxRepo;
        this.rabbitTemplate=rabbitTemplate;

    }


    @Scheduled(fixedDelay=10000)
    public void checkForPending()  {
        List<Outbox> pendingOutboxes=outboxRepo.findPendingBatch(10);
        System.out.println("Pending search");
        if (!pendingOutboxes.isEmpty()){
            for (Outbox outbox : pendingOutboxes){
                try {
                    sendToRabbitMQ(outbox);
                } catch (Exception e){
                    System.out.println(""+e);
                }



            }
        }
    }

    private void sendToRabbitMQ(Outbox outbox) throws ExecutionException, InterruptedException {
        CorrelationData data = new CorrelationData(String.valueOf(outbox.getId()));

        data.getFuture().whenComplete((confirm,ex)-> {
            if (ex!=null || !confirm.isAck()){
                System.err.println("RabbitMQ Failed for this Outbox ID: " + outbox.getId());
                if (outbox.getRetries()<3){
                    outbox.increaseRetry();
                    updateOutboxStatus(outbox, OutboxStatus.PENDING);
                }
                else{
                    updateOutboxStatus(outbox, OutboxStatus.FAILED);
                }
            }
            else {
                System.err.println("RabbitMQ Suceeded for this Outbox ID: " + outbox.getId());
                updateOutboxStatus(outbox, OutboxStatus.SENT);
            }
        });


        rabbitTemplate.convertAndSend("notifications.exchange","send.device",outbox, data);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void updateOutboxStatus(Outbox outbox ,OutboxStatus status){
        outbox.setStatus(status);
        outboxRepo.save(outbox);

    }

}
