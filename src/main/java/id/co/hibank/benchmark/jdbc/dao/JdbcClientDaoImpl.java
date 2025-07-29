package id.co.hibank.benchmark.jdbc.dao;

import id.co.hibank.benchmark.jdbc.util.JdbcResilienceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public abstract class JdbcClientDaoImpl<T> implements BaseDao<T> {

    protected final JdbcClient jdbcClient;
    private final Class<T> type;
    private final String tableName;
    protected final JdbcResilienceHelper resilienceHelper;
    private final Function<String, JdbcClient.StatementSpec> executor;

    protected JdbcClientDaoImpl(
            JdbcClient jdbcClient,
            JdbcResilienceHelper resilienceHelper,
            Class<T> type,
            String tableName
    ) {
        this(jdbcClient, resilienceHelper, type, tableName, jdbcClient::sql);
    }

    protected JdbcClientDaoImpl(
            JdbcClient jdbcClient,
            JdbcResilienceHelper resilienceHelper,
            Class<T> type,
            String tableName,
            Function<String, JdbcClient.StatementSpec> executor
    ) {
        this.jdbcClient = jdbcClient;
        this.resilienceHelper = resilienceHelper;
        this.type = type;
        this.tableName = tableName;
        this.executor = executor;
    }

    @Override
    public void save(T entity) {
        Map<String, Object> paramMap = toMap(entity);
        String sql = buildInsertSql(paramMap);
        log.debug("Executing INSERT: {}", sql);
        resilienceHelper.executeResilient(
                () -> executor.apply(sql)
                        .params(paramMap)
                        .update(),
                fail("Insert failed")
        );
    }

    @Override
    public void update(T entity) {
        Map<String, Object> paramMap = toMap(entity);
        if (!paramMap.containsKey("id")) {
            throw new IllegalArgumentException("Missing 'id' in entity for update");
        }
        String sql = buildUpdateSql(paramMap);
        log.debug("Executing UPDATE: {}", sql);
        resilienceHelper.executeResilient(
                () -> executor.apply(sql)
                        .params(paramMap)
                        .update(),
                fail("Update failed")
        );
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = :id";
        log.debug("Executing DELETE: {}", sql);
        resilienceHelper.executeResilient(
                () -> executor.apply(sql)
                        .param("id", id)
                        .update(),
                fail("Delete failed")
        );
    }

    @Override
    public T findById(Long id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = :id";
        log.debug("Executing SELECT by ID: {}", sql);
        return resilienceHelper.safeOptional(
                () -> executor.apply(sql)
                        .param("id", id)
                        .query(type)
                        .single()
        ).orElse(null);
    }

    @Override
    public List<T> findAll() {
        String sql = "SELECT * FROM " + tableName;
        log.debug("Executing SELECT all: {}", sql);
        return Optional.ofNullable(
                resilienceHelper.safe(
                        () -> executor.apply(sql)
                                .query(type)
                                .list()
                )
        ).orElse(List.of());
    }

    protected abstract Map<String, Object> toMap(T entity);

    protected Function<Throwable, Integer> fail(String message) {
        return ex -> {
            log.error("{}: {}", message, ex.getMessage());
            throw new RuntimeException(message, ex);
        };
    }

    private String buildInsertSql(Map<String, Object> fields) {
        String columns = String.join(", ", fields.keySet());
        String params = fields.keySet().stream().map(k -> ":" + k).collect(Collectors.joining(", "));
        return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, params);
    }

    private String buildUpdateSql(Map<String, Object> fields) {
        String setClause = fields.keySet().stream()
                .filter(k -> !"id".equals(k))
                .map(k -> k + " = :" + k)
                .collect(Collectors.joining(", "));
        return String.format("UPDATE %s SET %s WHERE id = :id", tableName, setClause);
    }
}