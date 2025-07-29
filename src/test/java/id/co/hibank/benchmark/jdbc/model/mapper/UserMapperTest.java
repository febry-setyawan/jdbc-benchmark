package id.co.hibank.benchmark.jdbc.model.mapper;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.model.dto.UserDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toDto_shouldMapFieldsCorrectly() {
        Role role = new Role(1L, "Admin");
        User user = new User(100L, "John", "john@example.com", role);

        UserDto dto = mapper.toDto(user);

        assertEquals(100L, dto.getId());
        assertEquals("John", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals(1L, dto.getRoleId());
        assertEquals("Admin", dto.getRoleName());
    }

    @Test
    void toDto_shouldHandleNullRole() {
        User user = new User(101L, "Jane", "jane@example.com", null);

        UserDto dto = mapper.toDto(user);

        assertEquals(101L, dto.getId());
        assertEquals("Jane", dto.getName());
        assertEquals("jane@example.com", dto.getEmail());
        assertNull(dto.getRoleId());
        assertNull(dto.getRoleName());
    }

    @Test
    void toDto_shouldReturnNullIfInputIsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toEntity_shouldMapFieldsCorrectly() {
        UserDto dto = new UserDto();
        dto.setId(200L);
        dto.setName("Alice");
        dto.setEmail("alice@example.com");

        Role role = new Role(2L, "User");

        User entity = mapper.toEntity(dto, role);

        assertEquals(200L, entity.getId());
        assertEquals("Alice", entity.getName());
        assertEquals("alice@example.com", entity.getEmail());
        assertNotNull(entity.getRole());
        assertEquals(2L, entity.getRole().getId());
        assertEquals("User", entity.getRole().getName());
    }

    @Test
    void toEntity_shouldReturnNullIfDtoIsNull() {
        assertNull(mapper.toEntity(null, new Role()));
    }
}