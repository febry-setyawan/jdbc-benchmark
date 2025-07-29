package id.co.hibank.benchmark.jdbc.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.co.hibank.benchmark.jdbc.exception.NotFoundException;
import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.repository.UserRepository;

@Service
public class UserService implements BaseService<User> {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        repo.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User get(Long id) {
        User user = repo.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found for id: " + id);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAll() {
        return repo.findAll();
    }

    @Override
    @Transactional
    public void update(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID must not be null for update");
        }
        repo.update(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repo.delete(id);
    }

    @Transactional(readOnly = true)
    public List<User> search(String filter, int page, int size, String sortBy, String sortDir) {
        return repo.search(filter, page, size, sortBy, sortDir);
    }
}