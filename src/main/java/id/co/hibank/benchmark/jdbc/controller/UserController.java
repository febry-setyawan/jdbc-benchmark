package id.co.hibank.benchmark.jdbc.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.model.dto.RoleDto;
import id.co.hibank.benchmark.jdbc.model.dto.UserDto;
import id.co.hibank.benchmark.jdbc.model.mapper.UserMapper;
import id.co.hibank.benchmark.jdbc.service.UserService;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService service;
    private final UserMapper userMapper;

    public UserController(UserService service, UserMapper userMapper) {
        this.service = service;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        List<UserDto> result = service.getAll().stream()
                .map(userMapper::toDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        User user = service.get(id);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid UserDto dto) {
        User user = userMapper.toEntity(dto);
        service.save(user);
        log.info("User created: {}", user.getId());
        return ResponseEntity.created(URI.create("/users/" + user.getId())).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @RequestBody @Valid UserDto dto) {
        User updated = userMapper.toEntity(dto);
        updated.setId(id);
        service.update(updated);
        log.info("User updated: {}", id);
        return ResponseEntity.ok(userMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        log.info("User deleted: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> search(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        List<UserDto> results = service.search(filter, page, size, sortBy, sortDir).stream()
                .map(userMapper::toDto)
                .toList();

        return ResponseEntity.ok(results);
    }
}