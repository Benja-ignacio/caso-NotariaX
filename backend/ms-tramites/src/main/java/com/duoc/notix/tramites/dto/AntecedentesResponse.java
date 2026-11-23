package com.duoc.notix.tramites.dto;

import com.duoc.notix.tramites.model.Antecedentes;

public record AntecedentesResponse(
        String nombreCompleto,
        String documentoIdentidad,
        String correoContacto,
        String comuna
) {

    public static AntecedentesResponse from(Antecedentes antecedentes) {
        return new AntecedentesResponse(
                antecedentes.getNombreCompleto(),
                antecedentes.getDocumentoIdentidad(),
                antecedentes.getCorreoContacto(),
                antecedentes.getComuna()
        );
    }
}
