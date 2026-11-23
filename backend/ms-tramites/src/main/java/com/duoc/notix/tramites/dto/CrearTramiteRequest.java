package com.duoc.notix.tramites.dto;

import com.duoc.notix.tramites.model.TipoFirma;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CrearTramiteRequest(
        @NotBlank(message = "El formato es obligatorio")
        @Size(max = 10, message = "El identificador del formato no puede superar 10 caracteres")
        String formatoId,

        @NotBlank(message = "El nombre del formato es obligatorio")
        @Size(max = 120, message = "El nombre del formato no puede superar 120 caracteres")
        String formatoNombre,

        @NotBlank(message = "La categoría del formato es obligatoria")
        @Size(max = 80, message = "La categoría del formato no puede superar 80 caracteres")
        String formatoCategoria,

        @NotBlank(message = "El solicitante es obligatorio")
        @Size(max = 40, message = "El identificador del solicitante no puede superar 40 caracteres")
        String solicitanteId,

        @NotBlank(message = "El nombre del solicitante es obligatorio")
        @Size(max = 120, message = "El nombre del solicitante no puede superar 120 caracteres")
        String solicitanteNombre,

        @Size(max = 160, message = "La representación no puede superar 160 caracteres")
        String actuaEnNombreDe,

        @NotNull(message = "El tipo de firma es obligatorio")
        TipoFirma tipoFirma,

        @NotBlank(message = "La evidencia de firma es obligatoria")
        @Size(max = 60, message = "La evidencia de firma no puede superar 60 caracteres")
        String firmaEvidencia,

        @NotNull(message = "Los antecedentes son obligatorios")
        @Valid
        AntecedentesRequest antecedentes,

        @NotEmpty(message = "Debes adjuntar al menos un documento")
        List<@Valid DocumentoRequest> documentos
) {
}
