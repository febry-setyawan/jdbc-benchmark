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

@Repository
public class UserRepository extends JdbcClientDaoImpl<User> {
    private final RoleRepository roleRepo; // roleRepo masih dibutuhkan untuk operasi RoleService lainnya, tapi tidak lagi untuk memuat Role di User.

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
        // Kueri SQL yang melakukan JOIN untuk mengambil data User dan Role dalam satu panggilan
        String sql = "SELECT u.id, u.name, u.email, r.id AS role_id, r.name AS role_name " +
                     "FROM users u LEFT JOIN roles r ON u.role_id = r.id " + // Gunakan LEFT JOIN untuk User tanpa Role
                     "WHERE u.id = :id";
        
        // Gunakan resilienceHelper dan JdbcClient untuk menjalankan kueri
        Optional<User> userOptional = resilienceHelper.safeOptional(
            () -> jdbcClient.sql(sql)
                    .param("id", id)
                    .query((rs, rowNum) -> {
                        // Buat objek Role jika role_id tidak null
                        Role role = null;
                        if (rs.getObject("role_id") != null) {
                            role = new Role(rs.getLong("role_id"), rs.getString("role_name"));
                        }
                        // Buat objek User dengan Role yang sudah dimuat
                        return new User(rs.getLong("id"), rs.getString("name"), rs.getString("email"), role);
                    })
                    .single()
        );
        
        return userOptional.orElse(null);
    }

    @Override
    public List<User> findAll() {
        // Kueri SQL yang melakukan JOIN untuk mengambil semua data User dan Role dalam satu panggilan
        String sql = "SELECT u.id, u.name, u.email, r.id AS role_id, r.name AS role_name " +
                     "FROM users u LEFT JOIN roles r ON u.role_id = r.id"; // Gunakan LEFT JOIN

        return Optional.ofNullable(
                resilienceHelper.safe(
                        () -> jdbcClient.sql(sql)
                                .query((rs, rowNum) -> {
                                    // Buat objek Role jika role_id tidak null
                                    Role role = null;
                                    if (rs.getObject("role_id") != null) {
                                        role = new Role(rs.getLong("role_id"), rs.getString("role_name"));
                                    }
                                    // Buat objek User dengan Role yang sudah dimuat
                                    return new User(rs.getLong("id"), rs.getString("name"), rs.getString("email"), role);
                                })
                                .list()
                )
        ).orElse(List.of());
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

        // Kueri SQL yang melakukan JOIN untuk mengambil data User dan Role dalam satu panggilan
        String sql = "SELECT u.id, u.name, u.email, r.id AS role_id, r.name AS role_name " +
                    "FROM users u LEFT JOIN roles r ON u.role_id = r.id " + // Pastikan LEFT JOIN juga di sini
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
