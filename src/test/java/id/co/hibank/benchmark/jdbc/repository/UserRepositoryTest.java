package id.co.hibank.benchmark.jdbc.repository;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.util.JdbcResilienceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserRepositoryTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    JdbcClient jdbcClient;

    @Mock
    JdbcClient.StatementSpec statementSpec;

    @Mock
    JdbcClient.MappedQuerySpec<User> mappedQuerySpec;

    @Mock
    JdbcResilienceHelper resilienceHelper;

    @Mock
    RoleRepository roleRepo;

    @Mock
    Function<String, JdbcClient.StatementSpec> executor;

    @InjectMocks
    UserRepository userRepository;

    User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User(1L, "John", "john@example.com", new Role(2L, "Admin"));
        userRepository = new UserRepository(jdbcClient, resilienceHelper, roleRepo, executor);

        when(executor.apply(anyString())).thenReturn(statementSpec);     
        when(jdbcClient.sql(anyString())).thenReturn(statementSpec);
        when(statementSpec.params(any(Map.class))).thenReturn(statementSpec);
        when(statementSpec.param(anyString(), any())).thenReturn(statementSpec);
        when(statementSpec.update()).thenReturn(1);
        when(resilienceHelper.executeResilient(any(Supplier.class), any(Function.class)))
            .thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());
    }

    @Test
    void testSave_shouldExecuteInsertSql() {
        userRepository.save(mockUser);
        verify(resilienceHelper).executeResilient(any(Supplier.class), any(Function.class));
        verify(executor).apply(startsWith("INSERT INTO"));
        verify(statementSpec).params(any(Map.class));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(executor).apply(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().startsWith("INSERT INTO"));
    }

    @Test
    void testUpdate_shouldExecuteUpdateSql() {
        userRepository.update(mockUser);
        verify(resilienceHelper).executeResilient(any(Supplier.class), any(Function.class));
        verify(executor).apply(startsWith("UPDATE"));
        verify(statementSpec).params(any(Map.class));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(executor).apply(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().startsWith("UPDATE"));
    }

    @Test
    void testDelete_shouldExecuteDeleteSql() {
        userRepository.delete(1L);
        verify(resilienceHelper).executeResilient(any(Supplier.class), any(Function.class));
        verify(executor).apply(startsWith("DELETE FROM"));
        verify(statementSpec).param(eq("id"), eq(1L));

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(executor).apply(sqlCaptor.capture());
        assertTrue(sqlCaptor.getValue().startsWith("DELETE FROM"));
    }

    @Test
    void testFindById_shouldReturnUserWithRole() {
        when(resilienceHelper.safeOptional(any(Supplier.class))).thenReturn(Optional.of(mockUser));
        when(roleRepo.findById(2L)).thenReturn(new Role(2L, "Admin"));

        User result = userRepository.findById(1L);

        assertNotNull(result);
        assertEquals("Admin", result.getRole().getName());
    }

    @Test
    void testFindAll_shouldReturnUserList() {
        when(resilienceHelper.safe(any(Supplier.class))).thenReturn(List.of(mockUser));

        List<User> result = userRepository.findAll();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getName());        
    }

    @Test
    void testSearch_shouldReturnEmptyListIfNoResult() {
        JdbcClient.StatementSpec stmtSpec = mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<User> querySpec = mock(JdbcClient.MappedQuerySpec.class);

        when(jdbcClient.sql(startsWith("SELECT"))).thenReturn(stmtSpec);
        when(stmtSpec.param(anyString(), any())).thenReturn(stmtSpec);
        when(stmtSpec.query(any(RowMapper.class))).thenReturn(querySpec);
        when(querySpec.list()).thenReturn(emptyList()); // Empty list

        List<User> result = userRepository.search("nonexistent", 0, 10, "name", "asc");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAll_shouldReturnEmptyListIfNoUsers() {
        when(resilienceHelper.safe(any(Supplier.class))).thenReturn(emptyList()); // Empty list

        List<User> result = userRepository.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById_shouldReturnNullIfNotFound() {
        when(resilienceHelper.safeOptional(any())).thenReturn(Optional.empty());
        User result = userRepository.findById(99L);
        assertNull(result);
    }

    @Test
    void testSave_shouldHandleExceptionGracefully() {
        when(resilienceHelper.executeResilient(any(), any())).thenThrow(new RuntimeException("Insert failed"));
        assertThrows(RuntimeException.class, () -> userRepository.save(mockUser));
    }

    @Test
    void testUpdate_shouldHandleExceptionGracefully() {
        when(resilienceHelper.executeResilient(any(), any())).thenThrow(new RuntimeException("Update failed"));
        assertThrows(RuntimeException.class, () -> userRepository.update(mockUser));
    }

    @Test
    void testDelete_shouldHandleExceptionGracefully() {
        when(resilienceHelper.executeResilient(any(), any())).thenThrow(new RuntimeException("Delete failed"));
        assertThrows(RuntimeException.class, () -> userRepository.delete(1L));
    }

   @Test
    void testSearch_shouldQueryJdbcClient() {
        String filter = "john";
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String sortDir = "asc";

        User expectedUser = new User(1L, "John", "john@example.com", new Role(2L, "Admin"));

        JdbcClient.StatementSpec stmtSpec = mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<User> mappedQuerySpec = mock(JdbcClient.MappedQuerySpec.class);

        when(jdbcClient.sql(startsWith("SELECT"))).thenReturn(stmtSpec);
        when(stmtSpec.param(eq("filter"), any())).thenReturn(stmtSpec);
        when(stmtSpec.param(eq("limit"), any())).thenReturn(stmtSpec);
        when(stmtSpec.param(eq("offset"), any())).thenReturn(stmtSpec);
        when(stmtSpec.query((RowMapper<User>) any())).thenReturn(mappedQuerySpec);
        when(mappedQuerySpec.list()).thenReturn(List.of(expectedUser));

        List<User> results = userRepository.search(filter, page, size, sortBy, sortDir);

        assertEquals(1, results.size());
        assertEquals("John", results.get(0).getName());
    }

    @Test
    void testSearch_withNullRole_shouldNotThrowException() {
        User userWithoutRole = new User(1L, "John", "john@example.com", null);

        JdbcClient.StatementSpec stmtSpec = mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<User> querySpec = mock(JdbcClient.MappedQuerySpec.class);

        when(jdbcClient.sql(startsWith("SELECT"))).thenReturn(stmtSpec);
        when(stmtSpec.param(anyString(), any())).thenReturn(stmtSpec);
        when(stmtSpec.query(any(RowMapper.class))).thenReturn(querySpec);
        when(querySpec.list()).thenReturn(List.of(userWithoutRole));

        List<User> result = userRepository.search("john", 0, 10, "name", "asc");
        assertEquals(1, result.size());
        assertNull(result.get(0).getRole());
    }

    @Test
    void testSearch_shouldCaptureSqlStatement() {
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        JdbcClient.StatementSpec stmtSpec = mock(JdbcClient.StatementSpec.class);
        JdbcClient.MappedQuerySpec<User> querySpec = mock(JdbcClient.MappedQuerySpec.class);

        when(jdbcClient.sql(sqlCaptor.capture())).thenReturn(stmtSpec);
        when(stmtSpec.param(anyString(), any())).thenReturn(stmtSpec);
        when(stmtSpec.query(any(RowMapper.class))).thenReturn(querySpec);
        when(querySpec.list()).thenReturn(emptyList());

        userRepository.search("john", 0, 10, "name", "asc");

        String capturedSql = sqlCaptor.getValue();
        assertTrue(capturedSql.startsWith("SELECT"));
        assertTrue(capturedSql.contains("FROM users u JOIN roles r ON u.role_id = r.id"));
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> emptyList() {
        return (List<T>) List.of();
    }
}