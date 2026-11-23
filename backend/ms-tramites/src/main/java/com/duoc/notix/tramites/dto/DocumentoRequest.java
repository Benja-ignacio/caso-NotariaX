package com.duoc.notix.tramites.dto;

import com.duoc.notix.tramites.model.EstadoDocumento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentoRequest(
        @NotBlank(message = "El nombre del documento es obligatorio")
        @Size(max = 180, message = "El nombre del documento no puede superar 180 caracteres")
        String nombre,

        @NotNull(message = "El tamaño del documento es obligatorio")
        @Min(value = 1, message = "El tamaño del documento debe ser mayor que cero")
        Integer tamanoKb,

        EstadoDocumento estado
) {
}
