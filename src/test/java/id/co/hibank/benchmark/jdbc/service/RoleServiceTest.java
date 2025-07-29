package id.co.hibank.benchmark.jdbc.service;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role mockRole;

    @BeforeEach
    void setUp() {
        mockRole = new Role(1L, "Admin");
    }

    @Test
    void testSave_shouldDelegateToRepository() {
        roleService.save(mockRole);
        verify(roleRepository).save(mockRole);
    }

    @Test
    void testSave_shouldThrowExceptionIfNull() {
        assertThrows(IllegalArgumentException.class, () -> roleService.save(null));
    }

    @Test
    void testGet_shouldReturnRole() {
        Role role = new Role(1L, "Admin");
        when(roleRepository.findById(1L)).thenReturn(role);

        Role result = roleService.get(1L);
        assertNotNull(result);
        assertEquals("Admin", result.getName());
    }

    @Test
    void testGet_shouldThrowIfNotFound() {
        when(roleRepository.findById(99L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> roleService.get(99L));
    }

    @Test
    void testUpdate_shouldDelegateToRepository() {
        roleService.update(mockRole);
        verify(roleRepository).update(mockRole);
    }

    @Test
    void testUpdate_shouldThrowIfNull() {
        assertThrows(IllegalArgumentException.class, () -> roleService.update(null));
    }

    @Test
    void testUpdate_shouldThrowIfIdIsNull() {
        Role role = new Role();
        role.setName("Admin");
        assertThrows(IllegalArgumentException.class, () -> roleService.update(role));
    }

    @Test
    void testDelete_shouldDelegateToRepository() {
        roleService.delete(1L);
        verify(roleRepository).delete(1L);
    }

    @Test
    void testGetAll_shouldReturnListOfRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(mockRole));
        List<Role> result = roleService.getAll();
        assertEquals(1, result.size());
        assertEquals("Admin", result.get(0).getName());
    }

    @Test
    void testGetAll_shouldReturnEmptyList() {
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());
        List<Role> result = roleService.getAll();
        assertTrue(result.isEmpty());
    }
} 
