package id.co.hibank.benchmark.jdbc.model.mapper;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.model.dto.UserDto;
import id.co.hibank.benchmark.jdbc.service.RoleService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    RoleService roleService;

    @InjectMocks
    UserMapper mapper;

    @Test
    void testToEntity_shouldMapCorrectly() {
        UserDto dto = new UserDto(1L, "John", "john@example.com", 2L, "Admin");
    
        when(roleService.get(2L)).thenReturn(new Role(2L, "Admin"));

        User user = mapper.toEntity(dto);

        assertEquals("John", user.getName());
        assertEquals("Admin", user.getRole().getName());
    }

    @Test
    void testToDto_shouldMapCorrectly() {
        Role role = new Role(2L, "Admin");
        User user = new User(1L, "John", "john@example.com", role);

        UserDto dto = mapper.toDto(user);

        assertEquals(1L, dto.getId());
        assertEquals("John", dto.getName());
        assertEquals(2L, dto.getRoleId());
        assertEquals("Admin", dto.getRoleName());
    }
}