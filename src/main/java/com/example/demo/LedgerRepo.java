package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LedgerRepo implements Repo<LedgerEntry,Long> {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        if (entry.getLedgerId() == null) {
            entityManager.persist(entry);
            return entry;
        } else {
            return entityManager.merge(entry);
        }
    }

    @Override
    public Optional<LedgerEntry> findById(Long id) {
        return Optional.ofNullable(entityManager.find(LedgerEntry.class, id));
    }

    @Override
    public List<LedgerEntry> findAll() {
        return entityManager.createQuery("SELECT l FROM LedgerEntry l", LedgerEntry.class).getResultList();
    }

    @Override
    public void delete(Long id) {
        LedgerEntry entry = entityManager.find(LedgerEntry.class, id);
        if (entry != null) {
            entityManager.remove(entry);
        }
    }
}