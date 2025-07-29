package id.co.hibank.benchmark.jdbc.model.mapper;

import org.springframework.stereotype.Component;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.dto.RoleDto;

@Component
public class RoleMapper {
    public RoleDto toDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        return dto;
    }

    public Role toEntity(RoleDto dto) {
        return new Role(dto.getId(), dto.getName());
    }
}

