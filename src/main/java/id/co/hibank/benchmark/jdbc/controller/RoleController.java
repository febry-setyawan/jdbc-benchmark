package id.co.hibank.benchmark.jdbc.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.dto.RoleDto;
import id.co.hibank.benchmark.jdbc.model.mapper.RoleMapper;
import id.co.hibank.benchmark.jdbc.service.RoleService;

@RestController
@RequestMapping("/roles")
@Validated
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    private final RoleService service;
    private final RoleMapper roleMapper;

    public RoleController(RoleService service, RoleMapper roleMapper) {
        this.service = service;
        this.roleMapper = roleMapper;
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> getAll() {
        List<RoleDto> result = service.getAll().stream()
                .map(roleMapper::toDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getById(@PathVariable @Min(1) Long id) {
        Role role = service.get(id);
        log.debug("Get role by ID: {}", id);
        return ResponseEntity.ok(roleMapper.toDto(role));
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid RoleDto dto) {
        Role role = roleMapper.toEntity(dto);
        service.save(role);
        log.info("Role created: {}", role.getId());
        return ResponseEntity.created(URI.create("/roles/" + role.getId())).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDto> update(@PathVariable Long id, @RequestBody @Valid RoleDto dto) {
        Role updated = roleMapper.toEntity(dto);
        updated.setId(id);
        service.update(updated);
        return ResponseEntity.ok(roleMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Min(1) Long id) {
        service.delete(id);
        log.info("Role deleted: {}", id);
        return ResponseEntity.noContent().build();
    }
}