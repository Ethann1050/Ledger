package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        String sql = """
        SELECT * FROM outbox
        WHERE status = 'PENDING'
        ORDER BY created_at ASC
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """;

        return entityManager.createNativeQuery(sql, Outbox.class)
                .setParameter("batchSize", batchsize)
                .getResultList();
    }

}
