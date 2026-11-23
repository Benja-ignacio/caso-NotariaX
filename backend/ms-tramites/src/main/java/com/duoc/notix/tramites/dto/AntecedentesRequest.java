package com.duoc.notix.tramites.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AntecedentesRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre completo no puede superar 120 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El documento de identidad es obligatorio")
        @Size(max = 30, message = "El documento de identidad no puede superar 30 caracteres")
        String documentoIdentidad,

        @NotBlank(message = "El correo de contacto es obligatorio")
        @Email(message = "El correo de contacto no tiene un formato válido")
        @Size(max = 160, message = "El correo de contacto no puede superar 160 caracteres")
        String correoContacto,

        @NotBlank(message = "La comuna es obligatoria")
        @Size(max = 80, message = "La comuna no puede superar 80 caracteres")
        String comuna
) {
}
