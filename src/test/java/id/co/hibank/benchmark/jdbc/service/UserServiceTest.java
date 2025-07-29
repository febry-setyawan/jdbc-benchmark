package id.co.hibank.benchmark.jdbc.service;

import id.co.hibank.benchmark.jdbc.exception.NotFoundException;
import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
        user = new User(1L, "John Doe", "john@example.com", new Role(2L, "Admin"));
    }

    @Test
    void testSave_shouldCallRepo() {
        userService.save(user);
        verify(userRepository).save(user);
    }

    @Test
    void testSave_shouldThrowExceptionIfNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.save(null));
    }

    @Test
    void testGet_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(user);
        User result = userService.get(1L);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void testGet_shouldThrowIfNotFound() {
        when(userRepository.findById(99L)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> userService.get(99L));
    }

    @Test
    void testGetAll_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<User> users = userService.getAll();
        assertEquals(1, users.size());
    }

    @Test
    void testUpdate_shouldCallRepo() {
        userService.update(user);
        verify(userRepository).update(user);
    }

    @Test
    void testUpdate_shouldThrowIfNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.update(null));
    }

    @Test
    void testUpdate_shouldThrowIfIdIsNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class, () -> userService.update(user));
    }

    @Test
    void testDelete_shouldCallRepo() {
        userService.delete(1L);
        verify(userRepository).delete(1L);
    }

    @Test
    void testSearch_shouldReturnResults() {
        when(userRepository.search("john", 0, 10, "name", "asc")).thenReturn(List.of(user));
        List<User> results = userService.search("john", 0, 10, "name", "asc");
        assertEquals(1, results.size());
    }
}