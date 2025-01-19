package com.pisareff.jdbc.dao;

import com.pisareff.jdbc.entity.Flight;

import java.util.List;
import java.util.Optional;

public interface Dao<K, E> {

    E save(E entity);

    boolean delete(K id);

    void update(E entity);

    Optional<E> findById(K id);

    List<E> findAll();

}
