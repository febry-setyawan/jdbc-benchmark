package id.co.hibank.benchmark.jdbc.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.co.hibank.benchmark.jdbc.exception.NotFoundException;
import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.repository.UserRepository;

@Service
// @Transactional
public class UserService implements BaseService<User> {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true) // Hapus semua entri dari cache 'users' setelah operasi save
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        repo.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id") // Coba ambil dari cache 'users' dengan kunci 'id'. Jika tidak ada, panggil metode dan simpan hasilnya.
    public User get(Long id) {
        User user = repo.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found for id: " + id);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'allUsers'") // Cache semua pengguna dengan kunci tetap 'allUsers'
    public List<User> getAll() {
        return repo.findAll();
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#user.id") // Perbarui entri cache 'users' dengan kunci user.id setelah operasi update
    @CacheEvict(value = "users", key = "'allUsers'", condition = "#user != null") // Hapus entri 'allUsers' karena data berubah
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
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#id"), // Hapus entri cache 'users' dengan kunci 'id' setelah operasi delete
        @CacheEvict(value = "users", key = "'allUsers'", condition = "#id != null") // Hapus entri 'allUsers' karena data berubah
    })    
    public void delete(Long id) {
        repo.delete(id);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "{#filter, #page, #size, #sortBy, #sortDir}") // Cache hasil pencarian berdasarkan semua parameter
    public List<User> search(String filter, int page, int size, String sortBy, String sortDir) {
        return repo.search(filter, page, size, sortBy, sortDir);
    }
}