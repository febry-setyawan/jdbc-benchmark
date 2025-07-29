package id.co.hibank.benchmark.jdbc.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private Long id; // ID biasanya tidak divalidasi dengan @NotNull/@NotBlank saat membuat, tetapi penting untuk pembaruan.

    @NotBlank(message = "Role name cannot be empty")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    private String name;
}