package com.example.demo;

import java.util.List;
import java.util.Optional;

public interface Repo {

    Account save(Account account);
    void delete(Long id);
    Optional<Account> findById(Long id);
    List<Account> findAll();

}