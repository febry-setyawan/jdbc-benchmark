package id.co.hibank.benchmark.jdbc.service;

import java.util.List;

import org.springframework.stereotype.Service;

import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.repository.UserRepository;

@Service
public class UserService implements BaseService<User> {
    private final UserRepository repo;
    public UserService(UserRepository repo) { this.repo = repo; }
    public void save(User u) { repo.save(u); }
    public User get(Long id) { return repo.findById(id); }
    public List<User> getAll() { return repo.findAll(); }
    public void update(User u) { repo.update(u); }
    public void delete(Long id) { repo.delete(id); }
    public List<User> search(String filter, int page, int size, String sortBy, String sortDir) {
        return repo.search(filter, page, size, sortBy, sortDir);
    }
}