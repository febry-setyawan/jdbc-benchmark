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

    // Metode search yang diperbarui untuk mendukung pencarian berdasarkan ID atau Nama
    public List<Role> search(String filter, int page, int size, String sortBy, String sortDir) {
        List<String> allowedSortFields = List.of("id", "name");
        List<String> allowedSortDirs = List.of("asc", "desc");

        String finalSortBy = allowedSortFields.contains(sortBy) ? sortBy : "name";
        String finalSortDir = allowedSortDirs.contains(sortDir.toLowerCase()) ? sortDir.toUpperCase() : "ASC";

        StringBuilder sqlBuilder = new StringBuilder("SELECT id, name FROM roles WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (filter != null && !filter.trim().isEmpty()) {
            // Coba parsing filter sebagai Long untuk ID
            try {
                Long filterId = Long.parseLong(filter.trim());
                sqlBuilder.append("AND (LOWER(name) LIKE :nameFilter OR id = :idFilter) ");
                params.put("idFilter", filterId);
            } catch (NumberFormatException e) {
                // Jika bukan angka, cari berdasarkan nama saja
                sqlBuilder.append("AND LOWER(name) LIKE :nameFilter ");
            }
            params.put("nameFilter", "%" + filter.toLowerCase() + "%");
        }

        sqlBuilder.append("ORDER BY ").append(finalSortBy).append(" ").append(finalSortDir);
        sqlBuilder.append(" LIMIT :limit OFFSET :offset");

        params.put("limit", size);
        params.put("offset", page * size);

        return jdbcClient.sql(sqlBuilder.toString())
                .params(params)
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
