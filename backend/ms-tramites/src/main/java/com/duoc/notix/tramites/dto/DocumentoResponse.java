package com.duoc.notix.tramites.dto;

import com.duoc.notix.tramites.model.Documento;
import com.duoc.notix.tramites.model.EstadoDocumento;

public record DocumentoResponse(
        Long id,
        String nombre,
        Integer tamanoKb,
        EstadoDocumento estado
) {

    public static DocumentoResponse from(Documento documento) {
        return new DocumentoResponse(
                documento.getId(),
                documento.getNombre(),
                documento.getTamanoKb(),
                documento.getEstado()
        );
    }
}
