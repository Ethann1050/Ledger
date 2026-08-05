package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepo implements Repo<Account,Long>{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Account save(Account account){
        if (account.getId()==null){
            entityManager.persist(account); //auto generates ID if it doesn't have one
        return account;
        }
        else{
            return entityManager.merge(account);
        }
    }

    @Override
    public Optional<Account> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Account.class, id, LockModeType.PESSIMISTIC_WRITE));
    }

    public Optional<Account> findByIdCheck(Long id) {
        return Optional.ofNullable(entityManager.find(Account.class, id));
    }
    @Override
    public List<Account> findAll() {
        return entityManager.createQuery("SELECT a FROM Account a", Account.class).getResultList();
    }
    @Override
    public void delete(Long id) {
        Account account = entityManager.find(Account.class, id);
        if (account != null) {
            entityManager.remove(account);
        }
    }

}
