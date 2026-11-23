package com.duoc.notix.usuarios.config;

import com.duoc.notix.usuarios.model.TipoCuenta;
import com.duoc.notix.usuarios.model.Usuario;
import com.duoc.notix.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "notix.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository repository;

    public DataSeeder(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        Usuario persona = new Usuario();
        persona.setId("USR-001");
        persona.setTipoCuenta(TipoCuenta.PERSONA);
        persona.setNombreCompleto("Ana Demo Prueba");
        persona.setCorreo("ana.demo@notariax.test");
        persona.setContrasena("demo123");
        persona.setRun("111111111");
        persona.setNumeroDocumento("123456789");
        persona.setTelefono("912345678");
        persona.setComuna("Ñuñoa");
        repository.save(persona);

        Usuario notario = new Usuario();
        notario.setId("USR-NOTARIO-01");
        notario.setTipoCuenta(TipoCuenta.NOTARIO);
        notario.setNombreCompleto("Carlos Notario Silva");
        notario.setCorreo("notario.demo@notariax.test");
        notario.setContrasena("notario123");
        notario.setRun("222222222");
        notario.setNumeroDocumento("987654321");
        notario.setTelefono("987654321");
        notario.setComuna("Santiago");
        notario.setNumeroNotaria(1);
        repository.save(notario);
    }
}
