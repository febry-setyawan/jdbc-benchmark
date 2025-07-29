package id.co.hibank.benchmark.jdbc.model.mapper;

import org.springframework.stereotype.Component;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.model.dto.UserDto;
import id.co.hibank.benchmark.jdbc.service.RoleService;

@Component
public class UserMapper {

    private final RoleService roleService;

    public UserMapper(RoleService roleService) {
        this.roleService = roleService;
    }

    public UserDto toDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        
        Role role = user.getRole();
        if (role != null) {
            dto.setRoleId(role.getId());
            dto.setRoleName(role.getName());
        }

        return dto;
    }

    public User toEntity(UserDto dto) {
        Role role = roleService.get(dto.getRoleId());
        return new User(dto.getId(), dto.getName(), dto.getEmail(), role);
    }
}