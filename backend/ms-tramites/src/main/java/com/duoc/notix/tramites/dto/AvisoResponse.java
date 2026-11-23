package com.duoc.notix.tramites.dto;

import com.duoc.notix.tramites.model.Aviso;
import java.time.LocalDateTime;

public record AvisoResponse(
        String id,
        String destinatarioId,
        String tramiteId,
        String mensaje,
        LocalDateTime fechaHora,
        boolean leido
) {

    public static AvisoResponse from(Aviso aviso) {
        return new AvisoResponse(
                aviso.getId(),
                aviso.getDestinatarioId(),
                aviso.getTramiteId(),
                aviso.getMensaje(),
                aviso.getFechaHora(),
                aviso.isLeido()
        );
    }
}
