package id.co.hibank.benchmark.jdbc.dao;

import java.util.List;

public interface BaseDao<T> {
    void save(T entity);
    T findById(Long id);
    List<T> findAll();
    void update(T entity);
    void delete(Long id);
}
