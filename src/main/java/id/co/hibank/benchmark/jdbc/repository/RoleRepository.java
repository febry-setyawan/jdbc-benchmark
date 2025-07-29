package id.co.hibank.benchmark.jdbc.repository;

import id.co.hibank.benchmark.jdbc.dao.JdbcClientDaoImpl;
import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.util.JdbcResilienceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

    public List<Role> search(String filter, int page, int size, String sortBy, String sortDir) {
        List<String> allowedSortFields = List.of("id", "name");
        List<String> allowedSortDirs = List.of("asc", "desc");

        String finalSortBy = allowedSortFields.contains(sortBy) ? sortBy : "name";
        String finalSortDir = allowedSortDirs.contains(sortDir.toLowerCase()) ? sortDir.toUpperCase() : "ASC";

        String sql = "SELECT id, name FROM roles WHERE LOWER(name) LIKE :filter " +
                     "ORDER BY " + finalSortBy + " " + finalSortDir + " LIMIT :limit OFFSET :offset";

        return jdbcClient.sql(sql)
                .param("filter", "%" + filter.toLowerCase() + "%")
                .param("limit", size)
                .param("offset", page * size)
                .query((rs, rowNum) -> new Role(rs.getLong("id"), rs.getString("name")))
                .list();
    }

    public Role findByName(String name) {
        return resilienceHelper.safeOptional(() ->
            jdbcClient.sql("SELECT * FROM roles WHERE LOWER(name) = :name")
                    .param("name", name.toLowerCase())
                    .query(Role.class)
                    .single()
        ).orElse(null);
    }
}