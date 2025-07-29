package id.co.hibank.benchmark.jdbc.service;

import java.util.List;

import org.springframework.stereotype.Service;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.repository.RoleRepository;

@Service
public class RoleService implements BaseService<Role> {
    private final RoleRepository repo;

    public RoleService(RoleRepository repo) {
        this.repo = repo;
    }

    @Override
    public void save(Role r) {
        if (r == null) throw new IllegalArgumentException("Role must not be null");
        repo.save(r);
    }

    @Override
    public Role get(Long id) {
        Role role = repo.findById(id);
        if (role == null) {
            throw new IllegalArgumentException("Role not found with id: " + id);
        }
        return role;
    }

    @Override
    public List<Role> getAll() {
        return repo.findAll();
    }

    @Override
    public void update(Role r) {
        if (r == null) throw new IllegalArgumentException("Role must not be null");
        if (r.getId() == null) throw new IllegalArgumentException("Role ID must not be null for update");
        repo.update(r);
    }

    @Override
    public void delete(Long id) {
        repo.delete(id);
    }
}