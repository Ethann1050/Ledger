package com.example.demo;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface Repo<T> {

    T save(T t);
    void delete(Long id);
    Optional<T> findById(Long id);
    List<T> findAll();

}