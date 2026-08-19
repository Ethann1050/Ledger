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

    @PersistenceContext
    EntityManager entityManager;

    @Scheduled(fixedDelay=10000)
    @Transactional
    public void checkForPending(){
        String sql="SELECT e FROM Outbox e WHERE e.status='PENDING'";
        List<Outbox> pendingOutboxes=entityManager.createQuery(sql).setMaxResults(10).setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
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
        entityManager.merge(outbox);

    }

}
