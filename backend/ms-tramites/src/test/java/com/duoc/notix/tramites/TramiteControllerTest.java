package com.duoc.notix.tramites;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.duoc.notix.tramites.web.ApiExceptionHandler;
import com.duoc.notix.tramites.web.AvisoController;
import com.duoc.notix.tramites.web.TramiteController;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TramiteControllerTest {

    @Autowired
    private TramiteController tramiteController;

    @Autowired
    private AvisoController avisoController;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(tramiteController, avisoController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void creaYConsultaTramites() throws Exception {
        String id = crearTramite();

        mvc.perform(get("/api/tramites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].estado").value("ENVIADO"))
                .andExpect(jsonPath("$[0].legibilidadConfirmada").value(false))
                .andExpect(jsonPath("$[0].historial.length()").value(1));

        mvc.perform(get("/api/tramites/solicitante/{id}", "USR-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));

        mvc.perform(get("/api/tramites/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.antecedentes.comuna").value("Ñuñoa"));
    }

    @Test
    void apruebaSoloConLegibilidadYBloqueaFinalizado() throws Exception {
        String id = crearTramite();

        mvc.perform(put("/api/tramites/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"FINALIZADO","observacion":null,"legibilidadConfirmada":false,"rol":"NOTARIO","comentario":null}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Debes confirmar que los documentos son legibles antes de aprobar."));

        mvc.perform(put("/api/tramites/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"FINALIZADO","observacion":null,"legibilidadConfirmada":true,"rol":"NOTARIO","comentario":"Documentos legibles."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.legibilidadConfirmada").value(true))
                .andExpect(jsonPath("$.historial.length()").value(2));

        mvc.perform(put("/api/tramites/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"DEVUELTO","observacion":"Ilegible","legibilidadConfirmada":false,"rol":"NOTARIO"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El trámite ya está finalizado."));

        mvc.perform(delete("/api/tramites/{id}", id))
                .andExpect(status().isConflict());
    }

    @Test
    void devuelveYReenviaConDocumentoDeReemplazo() throws Exception {
        String id = crearTramite();

        mvc.perform(put("/api/tramites/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"DEVUELTO","observacion":"El anexo está ilegible.","legibilidadConfirmada":false,"rol":"NOTARIO"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTO"))
                .andExpect(jsonPath("$.observacion").value("El anexo está ilegible."));

        mvc.perform(put("/api/tramites/{id}/reenvio", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "antecedentes": {"nombreCompleto":"Ana Corregida","documentoIdentidad":"111111111","correoContacto":"ana.demo@notariax.test","comuna":"Providencia"},
                                  "documentos": [{"nombre":"anexo_corregido.pdf","tamanoKb":310,"estado":"ACEPTADO"}],
                                  "comentario": "Se reemplazó el anexo."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENVIADO"))
                .andExpect(jsonPath("$.observacion").doesNotExist())
                .andExpect(jsonPath("$.legibilidadConfirmada").value(false))
                .andExpect(jsonPath("$.documentos[0].nombre").value("anexo_corregido.pdf"))
                .andExpect(jsonPath("$.historial.length()").value(3));

        mvc.perform(put("/api/tramites/{id}/reenvio", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "antecedentes": {"nombreCompleto":"Ana Corregida","documentoIdentidad":"111111111","correoContacto":"ana.demo@notariax.test","comuna":"Providencia"},
                                  "documentos": [{"nombre":"otro.pdf","tamanoKb":100,"estado":"ACEPTADO"}]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Solo se pueden corregir trámites devueltos."));
    }

    @Test
    void eliminaTramiteNoFinalizadoYAvisos() throws Exception {
        String id = crearTramite();

        mvc.perform(get("/api/avisos/{usuarioId}", "USR-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tramiteId").value(id))
                .andExpect(jsonPath("$[0].leido").value(false));

        MvcResult avisos = mvc.perform(get("/api/avisos/{usuarioId}", "USR-001"))
                .andReturn();
        String avisoId = JsonPath.read(avisos.getResponse().getContentAsString(), "$[0].id");

        mvc.perform(put("/api/avisos/{id}/leido", avisoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leido").value(true));

        mvc.perform(delete("/api/tramites/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/tramites/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void rechazaCreacionSinFirmaYDevuelveNotFound() throws Exception {
        mvc.perform(post("/api/tramites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formatoId":"F01","formatoNombre":"Poder simple","formatoCategoria":"Poderes",
                                  "solicitanteId":"USR-001","solicitanteNombre":"Ana Demo Prueba",
                                  "tipoFirma":"SIMPLE","firmaEvidencia":"",
                                  "antecedentes":{"nombreCompleto":"Ana Demo Prueba","documentoIdentidad":"111111111","correoContacto":"ana.demo@notariax.test","comuna":"Ñuñoa"},
                                  "documentos":[{"nombre":"poder.pdf","tamanoKb":100,"estado":"ACEPTADO"}]
                                }
                                """))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/tramites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formatoId":"F01","formatoNombre":"Poder simple","formatoCategoria":"Poderes",
                                  "solicitanteId":"USR-001","solicitanteNombre":"Ana Demo Prueba",
                                  "tipoFirma":"SIMPLE","firmaEvidencia":"AVANZADA_TOKEN_SIMULADO",
                                  "antecedentes":{"nombreCompleto":"Ana Demo Prueba","documentoIdentidad":"111111111","correoContacto":"ana.demo@notariax.test","comuna":"Ñuñoa"},
                                  "documentos":[{"nombre":"poder.pdf","tamanoKb":100,"estado":"ACEPTADO"}]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La evidencia de firma no corresponde al tipo de firma."));

        mvc.perform(get("/api/tramites/{id}", "TX-2026-9999"))
                .andExpect(status().isNotFound());
    }

    private String crearTramite() throws Exception {
        MvcResult resultado = mvc.perform(post("/api/tramites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "formatoId":"F01",
                                  "formatoNombre":"Poder simple",
                                  "formatoCategoria":"Poderes",
                                  "solicitanteId":"USR-001",
                                  "solicitanteNombre":"Ana Demo Prueba",
                                  "actuaEnNombreDe":null,
                                  "tipoFirma":"SIMPLE",
                                  "firmaEvidencia":"SIMPLE_CANVAS",
                                  "antecedentes":{
                                    "nombreCompleto":"Ana Demo Prueba",
                                    "documentoIdentidad":"111111111",
                                    "correoContacto":"ana.demo@notariax.test",
                                    "comuna":"Ñuñoa"
                                  },
                                  "documentos":[{"nombre":"poder.pdf","tamanoKb":250,"estado":"ACEPTADO"}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ENVIADO"))
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
    }
}
