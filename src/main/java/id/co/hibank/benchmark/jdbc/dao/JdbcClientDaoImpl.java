package id.co.hibank.benchmark.jdbc.dao;

import id.co.hibank.benchmark.jdbc.util.JdbcResilienceHelper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class JdbcClientDaoImpl<T> implements BaseDao<T> {

    protected final JdbcClient jdbcClient;
    private final Class<T> type;
    private final String tableName;
    private final JdbcResilienceHelper resilienceHelper;
    private final Function<String, JdbcClient.StatementSpec> executor;

    protected JdbcClientDaoImpl(
            JdbcClient jdbcClient,
            JdbcResilienceHelper resilienceHelper,
            Class<T> type,
            String tableName) {
        this(jdbcClient, resilienceHelper, type, tableName, jdbcClient::sql);
    }

    protected JdbcClientDaoImpl(
            JdbcClient jdbcClient,
            JdbcResilienceHelper resilienceHelper,
            Class<T> type,
            String tableName,
            Function<String, JdbcClient.StatementSpec> executor) {
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
        resilienceHelper.executeResilient(
                () -> executor.apply(sql)
                        .params(paramMap)
                        .update(),
                ex -> {
                    throw new RuntimeException("Insert failed", ex);
                });
    }

    @Override
    public T findById(Long id) {
        return resilienceHelper.safeOptional(
                () -> executor.apply("SELECT * FROM " + tableName + " WHERE id = :id")
                        .param("id", id)
                        .query(type)
                        .single())
                .orElse(null);
    }

    @Override
    public List<T> findAll() {
        return resilienceHelper.safe(
                () -> executor.apply("SELECT * FROM " + tableName)
                        .query(type)
                        .list());
    }

    @Override
    public void update(T entity) {
        Map<String, Object> paramMap = toMap(entity);
        String sql = buildUpdateSql(paramMap);
        resilienceHelper.executeResilient(
                () -> executor.apply(sql)
                        .params(paramMap)
                        .update(),
                ex -> {
                    throw new RuntimeException("Update failed", ex);
                });
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = :id";
        resilienceHelper.executeResilient(
                () -> executor.apply(sql)
                        .param("id", id)
                        .update(),
                ex -> {
                    throw new RuntimeException("Delete failed", ex);
                });
    }

    protected abstract Map<String, Object> toMap(T entity);

    private String buildInsertSql(Map<String, Object> fields) {
        String columns = String.join(", ", fields.keySet());
        String params = fields.keySet().stream().map(k -> ":" + k).collect(Collectors.joining(", "));
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + params + ")";
    }

    private String buildUpdateSql(Map<String, Object> fields) {
        String setClause = fields.keySet().stream()
                .filter(k -> !"id".equals(k))
                .map(k -> k + " = :" + k)
                .collect(Collectors.joining(", "));
        return "UPDATE " + tableName + " SET " + setClause + " WHERE id = :id";
    }
}