package id.co.hibank.benchmark.jdbc.service;

import java.util.List;

public interface BaseService<T> {
    void save(T entity);
    T get(Long id);
    List<T> getAll();
    void update(T entity);
    void delete(Long id);
}
