package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class ProcessedTransferRepo implements Repo<ProcessedTransfer, String> {

    @PersistenceContext
    private EntityManager entityManager;

    public boolean tryClaim(String idempotencyKey, Instant processedAt) {
        String nativeSql = "INSERT INTO processed_transfer (idempotency_key, processed_at) " +
                "VALUES (?1, ?2) ON CONFLICT (idempotency_key) DO NOTHING";

        int rowsInserted = entityManager.createNativeQuery(nativeSql)
                .setParameter(1, idempotencyKey)
                .setParameter(2, Timestamp.from(processedAt))
                .executeUpdate();

        return rowsInserted == 1;
    }

    @Override
    public ProcessedTransfer save(ProcessedTransfer processedTransfer) {
        entityManager.persist(processedTransfer);
        return processedTransfer;
    }

    public void deleteAll() {
        entityManager.createQuery("DELETE FROM ProcessedTransfer").executeUpdate();
    }

    @Override
    public Optional<ProcessedTransfer> findById(String key) {
        return Optional.ofNullable(entityManager.find(ProcessedTransfer.class, key));
    }

    @Override
    public List<ProcessedTransfer> findAll() {
        return entityManager.createQuery("SELECT p FROM ProcessedTransfer p", ProcessedTransfer.class).getResultList();
    }

    @Override
    public void delete(String id) {
        ProcessedTransfer processedTransfer = entityManager.find(ProcessedTransfer.class, id);
        if (processedTransfer != null) {
            entityManager.remove(processedTransfer);
        }
    }
}