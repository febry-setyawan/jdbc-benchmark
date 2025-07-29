package id.co.hibank.benchmark.jdbc.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import id.co.hibank.benchmark.jdbc.dao.JdbcClientDaoImpl;
import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.util.JdbcResilienceHelper;

@Repository
public class RoleRepository extends JdbcClientDaoImpl<Role> {
    @Autowired
    public RoleRepository(JdbcClient jdbcClient, JdbcResilienceHelper resilienceHelper) {
        super(jdbcClient, resilienceHelper, Role.class, "roles");
    }

    public RoleRepository(JdbcClient jdbcClient, JdbcResilienceHelper resilienceHelper, Function<String, JdbcClient.StatementSpec> executor) {
        super(jdbcClient, resilienceHelper, Role.class, "roles", executor);
    }

    @Override
    protected Map<String, Object> toMap(Role role) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", role.getId());
        map.put("name", role.getName());
        return map;
    }
}
