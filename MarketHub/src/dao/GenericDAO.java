package dao;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * GenericDAO Interface
 * Demonstrates Core Java Generics (<T, ID>) and modern java.util.Optional.
 * Part of Core Java Concepts (10 Marks).
 *
 * @param <T>  The entity type
 * @param <ID> The entity primary key type
 */
public interface GenericDAO<T, ID extends Serializable> {

    Optional<T> findById(ID id);

    List<T> findAll();

    T save(T entity);

    boolean update(T entity);

    boolean deleteById(ID id);
}
