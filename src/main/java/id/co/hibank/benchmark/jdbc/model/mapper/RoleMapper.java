package id.co.hibank.benchmark.jdbc.model.mapper;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.dto.RoleDto;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public RoleDto toDto(Role role) {
        if (role == null) return null;

        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        return dto;
    }

    public Role toEntity(RoleDto dto) {
        if (dto == null) return null;
        return new Role(dto.getId(), dto.getName());
    }
}