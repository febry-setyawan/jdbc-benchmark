package id.co.hibank.benchmark.jdbc.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.model.dto.UserDto;
import id.co.hibank.benchmark.jdbc.model.mapper.UserMapper;
import id.co.hibank.benchmark.jdbc.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;
    private UserMapper userMapper;
    public UserController(UserService s) { this.service = s; }

    @GetMapping public List<User> getAll() { return service.getAll(); }
    @PostMapping public void create(@RequestBody User u) { service.save(u); }
    @PutMapping("/{id}") public void update(@PathVariable Long id, @RequestBody User u) { u.setId(id); service.update(u); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
    @GetMapping("/{id}") public User getById(@PathVariable Long id) { return service.get(id); }
    @GetMapping("/search")
    public List<UserDto> searchUsers(
        @RequestParam(defaultValue = "") String filter,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "name") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir) {

        List<User> users = service.search(filter, page, size, sortBy, sortDir);
        return users.stream().map(userMapper::toDto).toList();
    }
}
