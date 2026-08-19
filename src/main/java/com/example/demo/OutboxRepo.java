package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@Repository
public class OutboxRepo implements Repo<Outbox, Long>{

    @PersistenceContext
    EntityManager entityManager;

    @Override
    @Transactional
    public Outbox save(Outbox entry) {
        if (entry.getId() == null) {
            entityManager.persist(entry);
            return entry;
        } else {
            return entityManager.merge(entry);
        }
    }

    @Override
    public void delete(Long id) {
        Outbox entry= entityManager.find(Outbox.class, id);
        if (entry !=null){
            entityManager.remove(entry);
        }
    }

    @Override
    public Optional<Outbox> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Outbox.class, id));

    }

    @Override
    public List<Outbox> findAll() {
        return entityManager.createQuery("SELECT e FROM Outbox e", Outbox.class).getResultList();
    }

    public List<Outbox> findPendingBatch(int batchsize){
        String sql="SELECT e FROM Outbox e WHERE e.status= OutboxStatus.PENDING ORDER BY e.createdAt ASC ";
        return entityManager.createNativeQuery(sql, Outbox.class).setMaxResults(batchsize).setLockMode(PESSIMISTIC_WRITE).getResultList();
    }

}
