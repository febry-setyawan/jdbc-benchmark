package id.co.hibank.benchmark.jdbc.model.mapper;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.model.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

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

    public User toEntity(UserDto dto, Role role) {
        if (dto == null) return null;
        return new User(
            dto.getId(),
            dto.getName(),
            dto.getEmail(),
            role
        );
    }
}