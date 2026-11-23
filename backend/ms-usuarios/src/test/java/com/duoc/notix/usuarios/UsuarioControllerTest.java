package com.duoc.notix.usuarios;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.duoc.notix.usuarios.model.TipoCuenta;
import com.duoc.notix.usuarios.model.Usuario;
import com.duoc.notix.usuarios.repository.UsuarioRepository;
import com.duoc.notix.usuarios.web.ApiExceptionHandler;
import com.duoc.notix.usuarios.web.UsuarioController;
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
class UsuarioControllerTest {

    @Autowired
    private UsuarioController controller;

    @Autowired
    private UsuarioRepository repository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void registraNormalizaCorreoYConsultaPorId() throws Exception {
        MvcResult resultado = mvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson("Nueva.Persona@NotariaX.test", "111111111", "123456789", "PERSONA", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("nueva.persona@notariax.test"))
                .andExpect(jsonPath("$.tipoCuenta").value("PERSONA"))
                .andReturn();

        String id = JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");

        mvc.perform(get("/api/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void rechazaCorreoDuplicadoYRunInvalido() throws Exception {
        mvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson("duplicado@notariax.test", "111111111", "123456789", "PERSONA", null)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson("DUPLICADO@notariax.test", "111111111", "123456789", "PERSONA", null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ya existe una cuenta con ese correo."));

        mvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson("run.invalido@notariax.test", "111111112", "123456789", "PERSONA", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El dígito verificador del RUN no coincide."));
    }

    @Test
    void validaLoginPorTipoYClave() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId("USR-LOGIN-01");
        usuario.setTipoCuenta(TipoCuenta.PERSONA);
        usuario.setNombreCompleto("Ana Login");
        usuario.setCorreo("login.demo@notariax.test");
        usuario.setContrasena("demo123");
        usuario.setRun("111111111");
        usuario.setNumeroDocumento("123456789");
        usuario.setTelefono("912345678");
        usuario.setComuna("Santiago");
        repository.save(usuario);

        mvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoCuenta":"PERSONA","correo":"LOGIN.DEMO@notariax.test","contrasena":"demo123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("USR-LOGIN-01"));

        mvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoCuenta":"PERSONA","correo":"login.demo@notariax.test","contrasena":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoCuenta":"NOTARIO","correo":"login.demo@notariax.test","contrasena":"demo123"}
                                """))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoCuenta":"PERSONA","correo":"nadie@notariax.test","contrasena":"demo123"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void exigeDatosDeEmpresaYNotaria() throws Exception {
        mvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson("empresa@notariax.test", "111111111", "123456789", "EMPRESA", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La cuenta de empresa requiere razón social, RUT y giro."));

        mvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registroJson("notario@notariax.test", "111111111", "123456789", "NOTARIO", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La cuenta de notario requiere número de notaría."));
    }

    private String registroJson(
            String correo,
            String run,
            String documento,
            String tipoCuenta,
            Integer numeroNotaria
    ) {
        String notaria = numeroNotaria == null ? "null" : numeroNotaria.toString();
        return """
                {
                  "tipoCuenta": "%s",
                  "nombreCompleto": "Persona de Prueba",
                  "correo": "%s",
                  "contrasena": "demo123",
                  "run": "%s",
                  "numeroDocumento": "%s",
                  "telefono": "912345678",
                  "comuna": "Santiago",
                  "razonSocialEmpresa": null,
                  "rutEmpresa": null,
                  "giroEmpresa": null,
                  "numeroNotaria": %s
                }
                """.formatted(tipoCuenta, correo, run, documento, notaria);
    }
}
