package id.co.hibank.benchmark.jdbc.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.co.hibank.benchmark.jdbc.exception.NotFoundException;
import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.repository.RoleRepository;

@Service
public class RoleService implements BaseService<Role> {
    private final RoleRepository repo;

    public RoleService(RoleRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void save(Role r) {
        if (r == null) throw new IllegalArgumentException("Role must not be null");
        repo.save(r);
    }

    @Override
    @Transactional(readOnly = true)
    public Role get(Long id) {
        Role role = repo.findById(id);
        if (role == null) {
            throw new NotFoundException("Role not found with id: " + id);
        }
        return role;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getAll() {
        return repo.findAll();
    }

    @Override
    @Transactional
    public void update(Role r) {
        if (r == null) throw new IllegalArgumentException("Role must not be null");
        if (r.getId() == null) throw new IllegalArgumentException("Role ID must not be null for update");
        repo.update(r);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repo.delete(id);
    }

    @Transactional(readOnly = true)
    public List<Role> search(String filter, int page, int size, String sortBy, String sortDir) {
        return repo.search(filter, page, size, sortBy, sortDir);
    }
}