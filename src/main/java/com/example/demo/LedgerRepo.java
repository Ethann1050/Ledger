package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class LedgerRepo implements Repo<LedgerEntry,Long> {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public LedgerEntry save(LedgerEntry entry) {
        if (entry.getLedgerId() == null) {
            entityManager.persist(entry);
            return entry;
        } else {
            return entityManager.merge(entry);
        }
    }

    public void deleteAll() {
        entityManager.createQuery("DELETE FROM LedgerEntry").executeUpdate();
    }

    @Override
    @Transactional
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

    public List<LedgerEntry> findByTransferIdAndType(String transferId, TransactionType HOLD_RESERVE){
        String sql = "SELECT e FROM LedgerEntry e WHERE e.transferId = ?1 AND e.type = ?2";

         List<LedgerEntry> entries = entityManager.createQuery(sql, LedgerEntry.class)
                .setParameter(1, transferId)
                .setParameter(2, HOLD_RESERVE)
                .getResultList();

         return entries;
    }

    public boolean existsByTransferIdAndTypeIn(String transferId, List<TransactionType> types) {
        String jpql = "SELECT e FROM LedgerEntry e WHERE e.transferId = ?1 AND e.type IN ?2";

        List<LedgerEntry> entries = entityManager.createQuery(jpql, LedgerEntry.class)
                .setParameter(1, transferId)
                .setParameter(2, types)
                .getResultList();

        return !entries.isEmpty();
    }

    public List<LedgerEntry> findExpiredUnsettledHolds(Instant now) {
        String jpql = "SELECT r FROM LedgerEntry r " +
                "WHERE r.type = TransactionType.HOLD_RESERVE " +
                "  AND r.expiresAt IS NOT NULL " +
                "  AND r.expiresAt <= ?1 " +
                "  AND r.transferId NOT IN (" +
                "      SELECT s.transferId FROM LedgerEntry s " +
                "      WHERE s.type IN (?2, ?3)" +
                "  )";

        List<LedgerEntry> entries = entityManager.createQuery(jpql, LedgerEntry.class)
                .setParameter(1, now)
                .setParameter(2, TransactionType.HOLD_COMMIT)
                .setParameter(3, TransactionType.HOLD_VOID)
                .getResultList();

        return entries;
    }

}