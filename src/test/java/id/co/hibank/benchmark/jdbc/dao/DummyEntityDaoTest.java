package id.co.hibank.benchmark.jdbc.dao;

import id.co.hibank.benchmark.jdbc.model.DummyEntity;
import id.co.hibank.benchmark.jdbc.util.JdbcResilienceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DummyEntityDaoTest {

    private JdbcClient jdbcClient;
    private JdbcResilienceHelper resilienceHelper;
    private DummyEntityDao dao;

    @BeforeEach
    void setUp() {
        jdbcClient = mock(JdbcClient.class, Answers.RETURNS_DEEP_STUBS);
        resilienceHelper = mock(JdbcResilienceHelper.class);
        dao = new DummyEntityDao(jdbcClient, resilienceHelper);
    }

    @Test
    void testSaveDelegatesToJdbcClient() {
        DummyEntity entity = new DummyEntity(1L, "Test");
        when(resilienceHelper.executeResilient(any(Supplier.class), any(Function.class))).thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());
        dao.save(entity);
        verify(resilienceHelper).executeResilient(any(Supplier.class), any(Function.class));
    }

    @Test
    void testFindByIdReturnsEntity() {
        DummyEntity entity = new DummyEntity(1L, "Test");
        when(resilienceHelper.safeOptional(any())).thenReturn(java.util.Optional.of(entity));
        DummyEntity result = dao.findById(1L);
        assertNotNull(result);
        assertEquals("Test", result.getName());
    }

    @Test
    void testFindAllReturnsList() {
        List<DummyEntity> list = List.of(new DummyEntity(1L, "Test"));
        when(resilienceHelper.safe(any())).thenReturn(list);
        List<DummyEntity> result = dao.findAll();
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateCallsJdbcClient() {
        DummyEntity entity = new DummyEntity(1L, "Updated");
        when(resilienceHelper.executeResilient(any(Supplier.class), any(Function.class))).thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());
        dao.update(entity);
        verify(resilienceHelper).executeResilient(any(Supplier.class), any(Function.class));
    }

    @Test
    void testDeleteCallsJdbcClient() {
        when(resilienceHelper.executeResilient(any(Supplier.class), any(Function.class))).thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());
        dao.delete(1L);
        verify(resilienceHelper).executeResilient(any(Supplier.class), any(Function.class));
    }
}