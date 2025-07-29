package id.co.hibank.benchmark.jdbc.service;

import id.co.hibank.benchmark.jdbc.exception.NotFoundException;
import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserService implements BaseService<User> {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        repo.save(user);
    }

    @Override
    public User get(Long id) {
        User user = repo.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found for id: " + id);
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        return repo.findAll();
    }

    @Override
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
    public void delete(Long id) {
        repo.delete(id);
    }

    public List<User> search(String filter, int page, int size, String sortBy, String sortDir) {
        return repo.search(filter, page, size, sortBy, sortDir);
    }
}