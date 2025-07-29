package id.co.hibank.benchmark.jdbc.model.mapper;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.dto.RoleDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleMapperTest {

    private final RoleMapper mapper = new RoleMapper();

    @Test
    void toDto_shouldMapCorrectly() {
        Role role = new Role(10L, "Manager");

        RoleDto dto = mapper.toDto(role);

        assertEquals(10L, dto.getId());
        assertEquals("Manager", dto.getName());
    }

    @Test
    void toDto_shouldReturnNullIfInputIsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toEntity_shouldMapCorrectly() {
        RoleDto dto = new RoleDto();
        dto.setId(20L);
        dto.setName("Staff");

        Role role = mapper.toEntity(dto);

        assertEquals(20L, role.getId());
        assertEquals("Staff", role.getName());
    }

    @Test
    void toEntity_shouldReturnNullIfDtoIsNull() {
        assertNull(mapper.toEntity(null));
    }
}