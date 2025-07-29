package id.co.hibank.benchmark.jdbc.dao;

import id.co.hibank.benchmark.jdbc.model.DummyEntity;
import id.co.hibank.benchmark.jdbc.util.JdbcResilienceHelper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.HashMap;
import java.util.Map;

public class DummyEntityDao extends JdbcClientDaoImpl<DummyEntity> {

    public DummyEntityDao(JdbcClient jdbcClient, JdbcResilienceHelper helper) {
        super(jdbcClient, helper, DummyEntity.class, "dummy_entity");
    }

    @Override
    protected Map<String, Object> toMap(DummyEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("name", entity.getName());
        return map;
    }
}