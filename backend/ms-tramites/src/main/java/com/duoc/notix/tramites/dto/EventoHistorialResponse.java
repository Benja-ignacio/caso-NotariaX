package com.duoc.notix.tramites.dto;

import com.duoc.notix.tramites.model.EventoHistorial;
import com.duoc.notix.tramites.model.RolEvento;
import java.time.LocalDateTime;

public record EventoHistorialResponse(
        Long id,
        String descripcion,
        LocalDateTime fechaHora,
        RolEvento rol
) {

    public static EventoHistorialResponse from(EventoHistorial evento) {
        return new EventoHistorialResponse(
                evento.getId(),
                evento.getDescripcion(),
                evento.getFechaHora(),
                evento.getRol()
        );
    }
}
