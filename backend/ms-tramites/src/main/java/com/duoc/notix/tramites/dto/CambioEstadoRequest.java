package com.duoc.notix.tramites.dto;

import com.duoc.notix.tramites.model.EstadoTramite;
import com.duoc.notix.tramites.model.RolEvento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambioEstadoRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoTramite estado,

        @Size(max = 500, message = "La observación no puede superar 500 caracteres")
        String observacion,

        boolean legibilidadConfirmada,

        @NotNull(message = "El rol que realiza la acción es obligatorio")
        RolEvento rol,

        @Size(max = 300, message = "El comentario no puede superar 300 caracteres")
        String comentario
) {
}
