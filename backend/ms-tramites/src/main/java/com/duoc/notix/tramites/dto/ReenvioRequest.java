package com.duoc.notix.tramites.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReenvioRequest(
        @NotNull(message = "Los antecedentes son obligatorios")
        @Valid
        AntecedentesRequest antecedentes,

        @NotEmpty(message = "Debes adjuntar al menos un documento")
        List<@Valid DocumentoRequest> documentos,

        @Size(max = 300, message = "El comentario no puede superar 300 caracteres")
        String comentario
) {
}
