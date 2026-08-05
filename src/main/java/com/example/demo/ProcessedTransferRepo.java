package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProcessedTransferRepo implements Repo<ProcessedTransfer,String> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ProcessedTransfer save(ProcessedTransfer processedTransfer) {
        if (processedTransfer.getIdempotencyKey()==null){
            entityManager.persist(processedTransfer);
            return processedTransfer;
        }
        else{
            return entityManager.merge(processedTransfer);
        }
    }

    @Override
    public Optional<ProcessedTransfer> findById(String Key) {
        return Optional.ofNullable(entityManager.find(ProcessedTransfer.class,Key));
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
