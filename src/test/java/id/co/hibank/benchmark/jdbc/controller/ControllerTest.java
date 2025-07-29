package id.co.hibank.benchmark.jdbc.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.hibank.benchmark.jdbc.model.Role;
import id.co.hibank.benchmark.jdbc.model.User;
import id.co.hibank.benchmark.jdbc.model.dto.RoleDto;
import id.co.hibank.benchmark.jdbc.model.dto.UserDto;
import id.co.hibank.benchmark.jdbc.model.mapper.RoleMapper;
import id.co.hibank.benchmark.jdbc.model.mapper.UserMapper;
import id.co.hibank.benchmark.jdbc.service.RoleService;
import id.co.hibank.benchmark.jdbc.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest({UserController.class, RoleController.class})
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RoleService roleService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private RoleMapper roleMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Role role;
    private UserDto userDto;
    private RoleDto roleDto;

    @BeforeEach
    void setUp() {
        role = new Role(1L, "Admin");
        user = new User(1L, "jdoe", "john.doe@gmail.com", role);
        roleDto = new RoleDto(1L, "Admin");
        userDto = new UserDto(1L, "jdoe", "john.doe@gmail.com", 1L, "Admin");

        when(userService.getAll()).thenReturn(List.of(user));
        when(userService.get(1L)).thenReturn(user);
        when(userService.search(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(List.of(user));

        when(roleService.getAll()).thenReturn(List.of(role));
        when(roleService.get(1L)).thenReturn(role);

        when(userMapper.toDto(any(User.class))).thenReturn(userDto);
        when(userMapper.toEntity(any(UserDto.class))).thenReturn(user);

        when(roleMapper.toDto(any(Role.class))).thenReturn(roleDto);
        when(roleMapper.toEntity(any(RoleDto.class))).thenReturn(role);
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("jdoe"));
    }

    @Test
    void shouldGetUserById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("jdoe"));
    }

    @Test
    void shouldGetAllRoles() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Admin"));
    }

    @Test
    void shouldGetRoleById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Admin"));
    }

    @Test
    void shouldUpdateRole() throws Exception {
        String payload = objectMapper.writeValueAsString(roleDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        String payload = objectMapper.writeValueAsString(userDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeleteRole() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/roles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldCreateUser() throws Exception {
        String payload = objectMapper.writeValueAsString(userDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldCreateRole() throws Exception {
        String payload = objectMapper.writeValueAsString(roleDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldSearchUsers() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("jdoe"));
    }
}