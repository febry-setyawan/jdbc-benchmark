package id.co.hibank.benchmark.jdbc.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.co.hibank.benchmark.jdbc.exception.NotFoundException;
import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.repository.RoleRepository;

@Service
// @Transactional
public class RoleService implements BaseService<Role> {
    private final RoleRepository repo;

    public RoleService(RoleRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    @CacheEvict(value = "roles", allEntries = true) // Hapus semua entri dari cache 'roles' setelah operasi save
    public void save(Role r) {
        if (r == null) throw new IllegalArgumentException("Role must not be null");
        repo.save(r);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#id") // Coba ambil dari cache 'roles' dengan kunci 'id'. Jika tidak ada, panggil metode dan simpan hasilnya.
    public Role get(Long id) {
        Role role = repo.findById(id);
        if (role == null) {
            throw new NotFoundException("Role not found with id: " + id);
        }
        return role;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "'allRoles'") // Cache semua peran dengan kunci tetap 'allRoles'
    public List<Role> getAll() {
        return repo.findAll();
    }

    @Override
    @Transactional
    @CachePut(value = "roles", key = "#r.id") // Perbarui entri cache 'roles' dengan kunci r.id setelah operasi update
    @CacheEvict(value = "roles", key = "'allRoles'", condition = "#r != null") // Hapus entri 'allRoles' karena data berubah
    public void update(Role r) {
        if (r == null) throw new IllegalArgumentException("Role must not be null");
        if (r.getId() == null) throw new IllegalArgumentException("Role ID must not be null for update");
        repo.update(r);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "roles", key = "#id"), // Hapus entri cache 'roles' dengan kunci 'id' setelah operasi delete
        @CacheEvict(value = "roles", key = "'allRoles'", condition = "#id != null") // Hapus entri 'allRoles' karena data berubah
    })
    public void delete(Long id) {
        repo.delete(id);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "{#filter, #page, #size, #sortBy, #sortDir}") // Cache hasil pencarian berdasarkan semua parameter
    public List<Role> search(String filter, int page, int size, String sortBy, String sortDir) {
        return repo.search(filter, page, size, sortBy, sortDir);
    }
}