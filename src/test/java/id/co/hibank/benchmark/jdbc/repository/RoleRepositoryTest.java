package id.co.hibank.benchmark.jdbc.repository;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.util.JdbcResilienceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleRepositoryTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
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

    Role role;

    @BeforeEach
    void setUp() {
        role = new Role(1L, "Admin");
        roleRepository = new RoleRepository(jdbcClient, resilienceHelper, executor);

        when(executor.apply(anyString())).thenReturn(statementSpec);
        when(statementSpec.params(any(Map.class))).thenReturn(statementSpec);
        when(statementSpec.param(anyString(), any())).thenReturn(statementSpec);
        when(statementSpec.update()).thenReturn(1);
        when(resilienceHelper.executeResilient(any(Supplier.class), any(Function.class)))
                .thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());
    }

    @Test
    void testSave_shouldExecuteInsertSql() {
        roleRepository.save(role);
        verify(resilienceHelper).executeResilient(any(Supplier.class), any(Function.class));
        verify(executor).apply(startsWith("INSERT INTO"));
        verify(statementSpec).params(any(Map.class));
    }

    @Test
    void testUpdate_shouldExecuteUpdateSql() {
        roleRepository.update(role);
        verify(resilienceHelper).executeResilient(any(Supplier.class), any(Function.class));
        verify(executor).apply(startsWith("UPDATE"));
        verify(statementSpec).params(any(Map.class));
    }

    @Test
    void testDelete_shouldExecuteDeleteSql() {
        roleRepository.delete(1L);
        verify(resilienceHelper).executeResilient(any(Supplier.class), any(Function.class));
        verify(executor).apply(startsWith("DELETE FROM"));
        verify(statementSpec).param(eq("id"), eq(1L));
    }

    @Test
    void testFindById_shouldReturnRole() {
        when(resilienceHelper.safeOptional(any(Supplier.class))).thenReturn(Optional.of(role));
        Role result = roleRepository.findById(1L);
        assertNotNull(result);
        assertEquals("Admin", result.getName());
    }

    @Test
    void testFindById_shouldReturnNullIfNotFound() {
        when(resilienceHelper.safeOptional(any(Supplier.class))).thenReturn(Optional.empty());
        Role result = roleRepository.findById(99L);
        assertNull(result);
    }

    @Test
    void testFindAll_shouldReturnList() {
        when(resilienceHelper.safe(any(Supplier.class))).thenReturn(List.of(role));
        List<Role> result = roleRepository.findAll();
        assertEquals(1, result.size());
        assertEquals("Admin", result.get(0).getName());
    }

    @Test
    void testFindAll_shouldReturnEmptyList() {
        when(resilienceHelper.safe(any(Supplier.class))).thenReturn(List.of());
        List<Role> result = roleRepository.findAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSave_shouldHandleException() {
        when(resilienceHelper.executeResilient(any(), any())).thenThrow(new RuntimeException("Insert failed"));
        assertThrows(RuntimeException.class, () -> roleRepository.save(role));
    }

    @Test
    void testUpdate_shouldHandleException() {
        when(resilienceHelper.executeResilient(any(), any())).thenThrow(new RuntimeException("Update failed"));
        assertThrows(RuntimeException.class, () -> roleRepository.update(role));
    }

    @Test
    void testDelete_shouldHandleException() {
        when(resilienceHelper.executeResilient(any(), any())).thenThrow(new RuntimeException("Delete failed"));
        assertThrows(RuntimeException.class, () -> roleRepository.delete(1L));
    }
}
