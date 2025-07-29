package id.co.hibank.benchmark.jdbc.repository;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.util.JdbcResilienceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleRepositoryTest {

    @Mock
    JdbcClient jdbcClient;

    @Mock
    JdbcClient.StatementSpec statementSpec;

    @Mock
    JdbcClient.MappedQuerySpec<Role> mappedQuerySpec;

    @Mock
    JdbcResilienceHelper resilienceHelper;

    @Mock
    Function<String, JdbcClient.StatementSpec> executor;

    @InjectMocks
    RoleRepository roleRepository;

    Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = new Role(1L, "Admin");
        roleRepository = new RoleRepository(jdbcClient, resilienceHelper, executor);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSave_shouldExecuteInsert() {
        when(executor.apply(anyString())).thenReturn(statementSpec);
        when(statementSpec.params(any(Map.class))).thenReturn(statementSpec);
        when(statementSpec.update()).thenReturn(1);

        when(resilienceHelper.executeResilient(any(Supplier.class), any(Function.class)))
                .thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());

        roleRepository.save(mockRole);
        verify(executor).apply(startsWith("INSERT INTO"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testFindById_shouldReturnRole() {
        when(resilienceHelper.safeOptional(any(Supplier.class))).thenReturn(Optional.of(mockRole));
        Role result = roleRepository.findById(1L);
        assertNotNull(result);
        assertEquals("Admin", result.getName());
    }

    @Test
    void testFindById_shouldReturnNullIfNotFound() {
        when(resilienceHelper.safeOptional(any())).thenReturn(Optional.empty());
        Role result = roleRepository.findById(99L);
        assertNull(result);
    }

    @Test
    void testFindAll_shouldReturnList() {
        when(resilienceHelper.safe(any())).thenReturn(List.of(mockRole));
        List<Role> result = roleRepository.findAll();
        assertEquals(1, result.size());
    }

    @Test
    void testFindAll_shouldReturnEmptyList() {
        when(resilienceHelper.safe(any())).thenReturn(List.of());
        List<Role> result = roleRepository.findAll();
        assertTrue(result.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testUpdate_shouldCallUpdateQuery() {
        when(executor.apply(anyString())).thenReturn(statementSpec);
        when(statementSpec.params(any(Map.class))).thenReturn(statementSpec);
        when(statementSpec.update()).thenReturn(1);

        when(resilienceHelper.executeResilient(any(Supplier.class), any(Function.class)))
                .thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());

        roleRepository.update(mockRole);
        verify(executor).apply(startsWith("UPDATE"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDelete_shouldCallDeleteQuery() {
        when(executor.apply(anyString())).thenReturn(statementSpec);
        when(statementSpec.param(anyString(), any())).thenReturn(statementSpec);
        when(statementSpec.update()).thenReturn(1);

        when(resilienceHelper.executeResilient(any(Supplier.class), any(Function.class)))
                .thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());

        roleRepository.delete(1L);
        verify(executor).apply(startsWith("DELETE FROM"));
    }

    @Test
    void testSave_shouldThrowException() {
        when(resilienceHelper.executeResilient(any(), any())).thenThrow(new RuntimeException("Insert failed"));
        assertThrows(RuntimeException.class, () -> roleRepository.save(mockRole));
    }

    @Test
    void testUpdate_shouldThrowException() {
        when(resilienceHelper.executeResilient(any(), any())).thenThrow(new RuntimeException("Update failed"));
        assertThrows(RuntimeException.class, () -> roleRepository.update(mockRole));
    }

    @Test
    void testDelete_shouldThrowException() {
        when(resilienceHelper.executeResilient(any(), any())).thenThrow(new RuntimeException("Delete failed"));
        assertThrows(RuntimeException.class, () -> roleRepository.delete(1L));
    }
}