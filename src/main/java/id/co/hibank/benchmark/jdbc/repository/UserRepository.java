package id.co.hibank.benchmark.jdbc.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import id.co.hibank.benchmark.jdbc.dao.JdbcClientDaoImpl;
import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.util.JdbcResilienceHelper;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class UserRepository extends JdbcClientDaoImpl<User> {
    private final RoleRepository roleRepo;

    @Autowired
    public UserRepository(JdbcClient jdbcClient, JdbcResilienceHelper resilienceHelper, RoleRepository roleRepo) {
        super(jdbcClient, resilienceHelper, User.class, "users");
        this.roleRepo = roleRepo;
    }

    public UserRepository(JdbcClient jdbcClient, JdbcResilienceHelper resilienceHelper,
            RoleRepository roleRepo, Function<String, JdbcClient.StatementSpec> executor) {
        super(jdbcClient, resilienceHelper, User.class, "users", executor);
        this.roleRepo = roleRepo;
    }

    @Override
    public User findById(Long id) {
        // Kita tidak bisa hanya memanggil super.findById(id) karena itu tidak akan
        // memuat role_id.
        // Kita perlu melakukan kueri yang lebih spesifik di sini.
        String sql = "SELECT u.id, u.name, u.email, u.role_id FROM users u WHERE u.id = :id";

        // Gunakan resilienceHelper dan JdbcClient untuk menjalankan kueri
        Optional<User> userOptional = resilienceHelper.safeOptional(
                () -> jdbcClient.sql(sql)
                        .param("id", id)
                        .query((rs, rowNum) -> {
                            // Buat objek User dasar dari hasil ResultSet
                            User user = new User(rs.getLong("id"), rs.getString("name"), rs.getString("email"), null);

                            // Cek apakah role_id ada dan tidak null
                            Long roleId = rs.getObject("role_id", Long.class);
                            if (roleId != null) {
                                // Muat objek Role lengkap menggunakan RoleRepository
                                Role role = roleRepo.findById(roleId);
                                user.setRole(role);
                            }
                            return user;
                        })
                        .single());

        return userOptional.orElse(null);
    }

    @Override
    public List<User> findAll() {
        // Override findAll untuk memuat Role dengan benar, mirip dengan metode search
        String sql = "SELECT u.id, u.name, u.email, r.id AS role_id, r.name AS role_name " +
                "FROM users u JOIN roles r ON u.role_id = r.id";

        return Optional.ofNullable(
                resilienceHelper.safe(
                        () -> jdbcClient.sql(sql)
                                .query((rs, rowNum) -> {
                                    Role role = null;
                                    if (rs.getObject("role_id") != null) {
                                        role = new Role(rs.getLong("role_id"), rs.getString("role_name"));
                                    }
                                    return new User(rs.getLong("id"), rs.getString("name"), rs.getString("email"),
                                            role);
                                })
                                .list()))
                .orElse(List.of());
    }

    @Override
    protected Map<String, Object> toMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("role_id", user.getRole() != null ? user.getRole().getId() : null);
        return map;
    }

    public List<User> search(String filter, int page, int size, String sortBy, String sortDir) {
        // Validasi sortBy dan sortDir
        List<String> allowedSortFields = List.of("id", "name", "email");
        List<String> allowedSortDirs = List.of("asc", "desc");

        String finalSortBy = allowedSortFields.contains(sortBy) ? "u." + sortBy : "u.name";
        String finalSortDir = allowedSortDirs.contains(sortDir.toLowerCase()) ? sortDir.toUpperCase() : "ASC";

        String sql = "SELECT u.id, u.name, u.email, r.id AS role_id, r.name AS role_name " +
                "FROM users u JOIN roles r ON u.role_id = r.id " +
                "WHERE LOWER(u.name) LIKE :filter OR LOWER(r.name) LIKE :filter " +
                "ORDER BY " + finalSortBy + " " + finalSortDir + " LIMIT :limit OFFSET :offset";

        return jdbcClient.sql(sql)
                .param("filter", "%" + filter.toLowerCase() + "%")
                .param("limit", size)
                .param("offset", page * size)
                .query((rs, rowNum) -> {
                    Role role = null;
                    if (rs.getObject("role_id") != null) {
                        role = new Role(rs.getLong("role_id"), rs.getString("role_name"));
                    }
                    return new User(rs.getLong("id"), rs.getString("name"), rs.getString("email"), role);
                })
                .list();
    }

}