package com.duoc.notix.tramites.config;

import com.duoc.notix.tramites.model.Antecedentes;
import com.duoc.notix.tramites.model.Aviso;
import com.duoc.notix.tramites.model.Documento;
import com.duoc.notix.tramites.model.EstadoDocumento;
import com.duoc.notix.tramites.model.EstadoTramite;
import com.duoc.notix.tramites.model.EventoHistorial;
import com.duoc.notix.tramites.model.RolEvento;
import com.duoc.notix.tramites.model.TipoFirma;
import com.duoc.notix.tramites.model.Tramite;
import com.duoc.notix.tramites.repository.AvisoRepository;
import com.duoc.notix.tramites.repository.TramiteRepository;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "notix.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final String SOLICITANTE_ID = "USR-001";
    private static final String SOLICITANTE_NOMBRE = "Ana Demo Prueba";
    private static final String SOLICITANTE_RUN = "111111111";
    private static final String SOLICITANTE_CORREO = "ana.demo@notariax.test";

    private final TramiteRepository tramiteRepository;
    private final AvisoRepository avisoRepository;

    public DataSeeder(TramiteRepository tramiteRepository, AvisoRepository avisoRepository) {
        this.tramiteRepository = tramiteRepository;
        this.avisoRepository = avisoRepository;
    }

    @Override
    public void run(String... args) {
        if (tramiteRepository.count() > 0) {
            return;
        }

        Tramite devuelto = tramite(
                "TX-2026-0042",
                "F01", "Poder simple", "Poderes",
                TipoFirma.SIMPLE,
                EstadoTramite.DEVUELTO,
                "SIMPLE_CANVAS",
                "Falta indicar la comuna del domicilio. El anexo está ilegible, vuelve a cargarlo.",
                false,
                LocalDateTime.of(2026, 10, 12, 10, 0),
                LocalDateTime.of(2026, 10, 13, 9, 30)
        );
        devuelto.agregarDocumento(new Documento("poder_demo.pdf", 90, EstadoDocumento.ACEPTADO));
        devuelto.agregarDocumento(new Documento("anexo_demo.jpg", 300, EstadoDocumento.OBSERVADO));
        devuelto.agregarEvento(new EventoHistorial(
                "Enviado al notario", LocalDateTime.of(2026, 10, 12, 10, 0), RolEvento.PERSONA));
        devuelto.agregarEvento(new EventoHistorial(
                "Devuelto con observaciones", LocalDateTime.of(2026, 10, 13, 9, 30), RolEvento.NOTARIO));
        tramiteRepository.save(devuelto);

        Tramite enviadoAvanzado = tramite(
                "TX-2026-0039",
                "F02", "Declaración jurada simple", "Declaraciones",
                TipoFirma.AVANZADA,
                EstadoTramite.ENVIADO,
                "AVANZADA_TOKEN_SIMULADO",
                null,
                false,
                LocalDateTime.of(2026, 10, 10, 16, 20),
                LocalDateTime.of(2026, 10, 10, 16, 20)
        );
        enviadoAvanzado.agregarDocumento(new Documento("declaracion_demo.pdf", 120, EstadoDocumento.ACEPTADO));
        enviadoAvanzado.agregarEvento(new EventoHistorial(
                "Enviado al notario", LocalDateTime.of(2026, 10, 10, 16, 20), RolEvento.PERSONA));
        tramiteRepository.save(enviadoAvanzado);

        Tramite finalizado = tramite(
                "TX-2026-0031",
                "F03", "Autorización de viaje", "Familia",
                TipoFirma.NOTARIADA,
                EstadoTramite.FINALIZADO,
                "NOTARIADA_SIMULADA",
                null,
                true,
                LocalDateTime.of(2026, 10, 2, 11, 0),
                LocalDateTime.of(2026, 10, 3, 12, 0)
        );
        finalizado.agregarDocumento(new Documento("viaje_demo.pdf", 150, EstadoDocumento.ACEPTADO));
        finalizado.agregarEvento(new EventoHistorial(
                "Enviado al notario", LocalDateTime.of(2026, 10, 2, 11, 0), RolEvento.PERSONA));
        finalizado.agregarEvento(new EventoHistorial(
                "Aprobado y finalizado por el notario", LocalDateTime.of(2026, 10, 3, 12, 0), RolEvento.NOTARIO));
        tramiteRepository.save(finalizado);

        Tramite enviadoSimple = tramite(
                "TX-2026-0001",
                "F01", "Poder simple", "Poderes",
                TipoFirma.SIMPLE,
                EstadoTramite.ENVIADO,
                "SIMPLE_CANVAS",
                null,
                false,
                LocalDateTime.of(2026, 10, 28, 15, 0),
                LocalDateTime.of(2026, 10, 28, 15, 0)
        );
        enviadoSimple.agregarDocumento(new Documento("poder_simple_demo.pdf", 250, EstadoDocumento.ACEPTADO));
        enviadoSimple.agregarEvento(new EventoHistorial(
                "Trámite ingresado correctamente", LocalDateTime.of(2026, 10, 28, 15, 0), RolEvento.PERSONA));
        tramiteRepository.save(enviadoSimple);

        aviso("AVS-001", "TX-2026-0001",
                "Tu trámite TX-2026-0001 ha sido enviado al notario con éxito.",
                LocalDateTime.of(2026, 10, 28, 15, 1), false);
        aviso("AVS-002", "TX-2026-0042",
                "Tu trámite TX-2026-0042 ha sido devuelto con observaciones.",
                LocalDateTime.of(2026, 10, 13, 9, 31), false);
        aviso("AVS-003", "TX-2026-0031",
                "Tu trámite TX-2026-0031 ha sido aprobado y finalizado por el notario.",
                LocalDateTime.of(2026, 10, 3, 12, 1), true);
    }

    private Tramite tramite(
            String id,
            String formatoId,
            String formatoNombre,
            String formatoCategoria,
            TipoFirma tipoFirma,
            EstadoTramite estado,
            String firmaEvidencia,
            String observacion,
            boolean legibilidadConfirmada,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaActualizacion
    ) {
        Tramite tramite = new Tramite();
        tramite.setId(id);
        tramite.setFormatoId(formatoId);
        tramite.setFormatoNombre(formatoNombre);
        tramite.setFormatoCategoria(formatoCategoria);
        tramite.setSolicitanteId(SOLICITANTE_ID);
        tramite.setSolicitanteNombre(SOLICITANTE_NOMBRE);
        tramite.setActuaEnNombreDe(null);
        tramite.setTipoFirma(tipoFirma);
        tramite.setEstado(estado);
        tramite.setAntecedentes(new Antecedentes(
                SOLICITANTE_NOMBRE,
                SOLICITANTE_RUN,
                SOLICITANTE_CORREO,
                "Ñuñoa"
        ));
        tramite.setObservacion(observacion);
        tramite.setLegibilidadConfirmada(legibilidadConfirmada);
        tramite.setFirmaEvidencia(firmaEvidencia);
        tramite.setFechaCreacion(fechaCreacion);
        tramite.setFechaActualizacion(fechaActualizacion);
        return tramite;
    }

    private void aviso(
            String id,
            String tramiteId,
            String mensaje,
            LocalDateTime fechaHora,
            boolean leido
    ) {
        Aviso aviso = new Aviso();
        aviso.setId(id);
        aviso.setDestinatarioId(SOLICITANTE_ID);
        aviso.setTramiteId(tramiteId);
        aviso.setMensaje(mensaje);
        aviso.setFechaHora(fechaHora);
        aviso.setLeido(leido);
        avisoRepository.save(aviso);
    }
}
