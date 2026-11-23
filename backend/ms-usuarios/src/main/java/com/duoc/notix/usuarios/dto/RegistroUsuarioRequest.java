package com.duoc.notix.usuarios.dto;

import com.duoc.notix.usuarios.model.TipoCuenta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioRequest(
        @NotNull(message = "El tipo de cuenta es obligatorio")
        TipoCuenta tipoCuenta,

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre completo no puede superar 120 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 160, message = "El correo no puede superar 160 caracteres")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 80, message = "La contraseña debe tener entre 6 y 80 caracteres")
        String contrasena,

        @NotBlank(message = "El RUN es obligatorio")
        @Size(max = 20, message = "El RUN no puede superar 20 caracteres")
        String run,

        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 30, message = "El número de documento no puede superar 30 caracteres")
        String numeroDocumento,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
        String telefono,

        @NotBlank(message = "La comuna es obligatoria")
        @Size(max = 80, message = "La comuna no puede superar 80 caracteres")
        String comuna,

        String razonSocialEmpresa,
        String rutEmpresa,
        String giroEmpresa,

        @Min(value = 1, message = "El número de notaría debe ser mayor o igual a 1")
        Integer numeroNotaria
) {
}
