package id.co.hibank.benchmark.jdbc.service;

import java.util.List;

import org.springframework.stereotype.Service;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.repository.RoleRepository;

@Service
public class RoleService implements BaseService<Role> {
    private final RoleRepository repo;
    public RoleService(RoleRepository repo) { this.repo = repo; }
    public void save(Role r) { repo.save(r); }
    public Role get(Long id) { return repo.findById(id); }
    public List<Role> getAll() { return repo.findAll(); }
    public void update(Role r) { repo.update(r); }
    public void delete(Long id) { repo.delete(id); }
}
