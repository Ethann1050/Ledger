package com.example.demo;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface Repo<T,Id> {

    T save(T t);
    void delete(Id id);
    Optional<T> findById(Id id);

    List<T> findAll();

}