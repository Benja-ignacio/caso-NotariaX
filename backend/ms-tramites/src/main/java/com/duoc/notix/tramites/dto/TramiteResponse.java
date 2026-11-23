package com.duoc.notix.tramites.dto;

import com.duoc.notix.tramites.model.EstadoTramite;
import com.duoc.notix.tramites.model.TipoFirma;
import com.duoc.notix.tramites.model.Tramite;
import java.time.LocalDateTime;
import java.util.List;

public record TramiteResponse(
        String id,
        String formatoId,
        String formatoNombre,
        String formatoCategoria,
        String solicitanteId,
        String solicitanteNombre,
        String actuaEnNombreDe,
        TipoFirma tipoFirma,
        EstadoTramite estado,
        AntecedentesResponse antecedentes,
        List<DocumentoResponse> documentos,
        String observacion,
        boolean legibilidadConfirmada,
        String firmaEvidencia,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        List<EventoHistorialResponse> historial
) {

    public static TramiteResponse from(Tramite tramite) {
        return new TramiteResponse(
                tramite.getId(),
                tramite.getFormatoId(),
                tramite.getFormatoNombre(),
                tramite.getFormatoCategoria(),
                tramite.getSolicitanteId(),
                tramite.getSolicitanteNombre(),
                tramite.getActuaEnNombreDe(),
                tramite.getTipoFirma(),
                tramite.getEstado(),
                AntecedentesResponse.from(tramite.getAntecedentes()),
                tramite.getDocumentos().stream().map(DocumentoResponse::from).toList(),
                tramite.getObservacion(),
                tramite.isLegibilidadConfirmada(),
                tramite.getFirmaEvidencia(),
                tramite.getFechaCreacion(),
                tramite.getFechaActualizacion(),
                tramite.getHistorial().stream().map(EventoHistorialResponse::from).toList()
        );
    }
}
