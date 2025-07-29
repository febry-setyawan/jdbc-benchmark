package id.co.hibank.benchmark.jdbc.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        User user = super.findById(id);
        if (user != null) {
            Role role = roleRepo.findById(user.getRole().getId());
            user.setRole(role);
        }
        return user;
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
        String sql = "SELECT u.id, u.name, u.email, r.id AS role_id, r.name AS role_name " +
                    "FROM users u JOIN roles r ON u.role_id = r.id " +
                    "WHERE LOWER(u.name) LIKE :filter OR LOWER(r.name) LIKE :filter " +
                    "ORDER BY " + sortBy + " " + sortDir + " LIMIT :limit OFFSET :offset";

        return jdbcClient.sql(sql)
            .param("filter", "%" + filter.toLowerCase() + "%")
            .param("limit", size)
            .param("offset", page * size)
            .query((rs, rowNum) -> {
                Role role = new Role(rs.getLong("role_id"), rs.getString("role_name"));
                return new User(rs.getLong("id"), rs.getString("name"), rs.getString("email"), role);
            })
            .list();
    }

}