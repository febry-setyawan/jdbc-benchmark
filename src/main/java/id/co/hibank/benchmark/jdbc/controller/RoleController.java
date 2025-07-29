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
import org.springframework.web.bind.annotation.RestController;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.service.RoleService;

@RestController
@RequestMapping("/roles")
public class RoleController {
    private final RoleService service;
    public RoleController(RoleService s) { this.service = s; }

    @GetMapping public List<Role> getAll() { return service.getAll(); }
    @PostMapping public void create(@RequestBody Role r) { service.save(r); }
    @PutMapping("/{id}") public void update(@PathVariable Long id, @RequestBody Role r) { r.setId(id); service.update(r); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
    @GetMapping("/{id}") public Role getById(@PathVariable Long id) { return service.get(id); }
}
