package com.duoc.notix.tramites.service;

import com.duoc.notix.tramites.dto.AntecedentesRequest;
import com.duoc.notix.tramites.dto.CambioEstadoRequest;
import com.duoc.notix.tramites.dto.CrearTramiteRequest;
import com.duoc.notix.tramites.dto.DocumentoRequest;
import com.duoc.notix.tramites.dto.ReenvioRequest;
import com.duoc.notix.tramites.dto.TramiteResponse;
import com.duoc.notix.tramites.model.Antecedentes;
import com.duoc.notix.tramites.model.Aviso;
import com.duoc.notix.tramites.model.Documento;
import com.duoc.notix.tramites.model.EstadoDocumento;
import com.duoc.notix.tramites.model.EstadoTramite;
import com.duoc.notix.tramites.model.EventoHistorial;
import com.duoc.notix.tramites.model.RolEvento;
import com.duoc.notix.tramites.model.Tramite;
import com.duoc.notix.tramites.repository.AvisoRepository;
import com.duoc.notix.tramites.repository.TramiteRepository;
import com.duoc.notix.tramites.web.ApiException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TramiteService {

    private final TramiteRepository tramiteRepository;
    private final AvisoRepository avisoRepository;

    public TramiteService(TramiteRepository tramiteRepository, AvisoRepository avisoRepository) {
        this.tramiteRepository = tramiteRepository;
        this.avisoRepository = avisoRepository;
    }

    @Transactional(readOnly = true)
    public List<TramiteResponse> listar() {
        return tramiteRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(TramiteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TramiteResponse> listarPorSolicitante(String solicitanteId) {
        return tramiteRepository.findBySolicitanteIdOrderByFechaCreacionDesc(solicitanteId)
                .stream()
                .map(TramiteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TramiteResponse obtener(String id) {
        return TramiteResponse.from(buscar(id));
    }

    public TramiteResponse crear(CrearTramiteRequest request) {
        validarEvidenciaFirma(request);
        LocalDateTime ahora = LocalDateTime.now();

        Tramite tramite = new Tramite();
        tramite.setId(generarId());
        tramite.setFormatoId(request.formatoId().trim());
        tramite.setFormatoNombre(request.formatoNombre().trim());
        tramite.setFormatoCategoria(request.formatoCategoria().trim());
        tramite.setSolicitanteId(request.solicitanteId().trim());
        tramite.setSolicitanteNombre(request.solicitanteNombre().trim());
        tramite.setActuaEnNombreDe(limpiar(request.actuaEnNombreDe()));
        tramite.setTipoFirma(request.tipoFirma());
        tramite.setEstado(EstadoTramite.ENVIADO);
        tramite.setAntecedentes(mapearAntecedentes(request.antecedentes()));
        tramite.setObservacion(null);
        tramite.setLegibilidadConfirmada(false);
        tramite.setFirmaEvidencia(request.firmaEvidencia().trim());
        tramite.setFechaCreacion(ahora);
        tramite.setFechaActualizacion(ahora);

        request.documentos().forEach(documento -> tramite.agregarDocumento(mapearDocumento(documento)));
        tramite.agregarEvento(new EventoHistorial(
                "Trámite ingresado correctamente",
                ahora,
                RolEvento.PERSONA
        ));

        tramiteRepository.save(tramite);
        registrarAviso(
                tramite,
                "Tu trámite " + tramite.getId() + " (" + tramite.getFormatoNombre()
                        + ") fue enviado al notario con éxito."
        );
        return TramiteResponse.from(tramite);
    }

    public TramiteResponse cambiarEstado(String id, CambioEstadoRequest request) {
        Tramite tramite = buscar(id);
        if (tramite.getEstado() == EstadoTramite.FINALIZADO) {
            throw new ApiException(HttpStatus.CONFLICT, "El trámite ya está finalizado.");
        }

        EstadoTramite nuevoEstado = request.estado();
        LocalDateTime ahora = LocalDateTime.now();
        String descripcion;
        String mensajeAviso;

        switch (nuevoEstado) {
            case FINALIZADO -> {
                if (tramite.getEstado() != EstadoTramite.ENVIADO) {
                    throw new ApiException(HttpStatus.CONFLICT, "Solo se pueden aprobar trámites en estado ENVIADO.");
                }
                if (!request.legibilidadConfirmada()) {
                    throw new ApiException(
                            HttpStatus.CONFLICT,
                            "Debes confirmar que los documentos son legibles antes de aprobar."
                    );
                }
                tramite.setLegibilidadConfirmada(true);
                tramite.setObservacion(null);
                descripcion = esBlanco(request.comentario())
                        ? "Aprobado y finalizado por el notario"
                        : request.comentario().trim();
                mensajeAviso = "Tu trámite " + tramite.getId() + " (" + tramite.getFormatoNombre()
                        + ") ha sido aprobado y finalizado por el notario.";
            }
            case DEVUELTO -> {
                if (tramite.getEstado() != EstadoTramite.ENVIADO) {
                    throw new ApiException(HttpStatus.CONFLICT, "Solo se pueden devolver trámites en estado ENVIADO.");
                }
                if (esBlanco(request.observacion())) {
                    throw new ApiException(HttpStatus.CONFLICT, "La devolución requiere una observación.");
                }
                tramite.setLegibilidadConfirmada(false);
                tramite.setObservacion(request.observacion().trim());
                descripcion = "Devuelto con observaciones: " + tramite.getObservacion();
                mensajeAviso = "Tu trámite " + tramite.getId() + " (" + tramite.getFormatoNombre()
                        + ") ha sido devuelto con observaciones: " + tramite.getObservacion();
            }
            default -> throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Para reenviar usa el endpoint /api/tramites/{id}/reenvio."
            );
        }

        tramite.setEstado(nuevoEstado);
        tramite.setFechaActualizacion(ahora);
        tramite.agregarEvento(new EventoHistorial(descripcion, ahora, request.rol()));

        tramiteRepository.save(tramite);
        registrarAviso(tramite, mensajeAviso);
        return TramiteResponse.from(tramite);
    }

    public TramiteResponse reenviar(String id, ReenvioRequest request) {
        Tramite tramite = buscar(id);
        if (tramite.getEstado() != EstadoTramite.DEVUELTO) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se pueden corregir trámites devueltos.");
        }

        boolean tieneObservados = tramite.getDocumentos().stream()
                .anyMatch(documento -> documento.getEstado() == EstadoDocumento.OBSERVADO);
        if (tieneObservados && request.documentos().isEmpty()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Adjunta un nuevo documento para reemplazar el observado."
            );
        }

        LocalDateTime ahora = LocalDateTime.now();
        tramite.setAntecedentes(mapearAntecedentes(request.antecedentes()));
        tramite.reemplazarDocumentos(
                request.documentos().stream().map(this::mapearDocumento).toList()
        );
        tramite.setEstado(EstadoTramite.ENVIADO);
        tramite.setObservacion(null);
        tramite.setLegibilidadConfirmada(false);
        tramite.setFechaActualizacion(ahora);

        String detalle = esBlanco(request.comentario())
                ? "sin comentario adicional"
                : request.comentario().trim();
        tramite.agregarEvento(new EventoHistorial(
                "Corregido y reenviado por el solicitante: " + detalle,
                ahora,
                RolEvento.PERSONA
        ));

        tramiteRepository.save(tramite);
        registrarAviso(
                tramite,
                "Tu trámite " + tramite.getId() + " (" + tramite.getFormatoNombre()
                        + ") fue corregido y reenviado al notario."
        );
        return TramiteResponse.from(tramite);
    }

    public void eliminar(String id) {
        Tramite tramite = buscar(id);
        if (tramite.getEstado() == EstadoTramite.FINALIZADO) {
            throw new ApiException(HttpStatus.CONFLICT, "No se puede eliminar un trámite finalizado.");
        }
        avisoRepository.deleteByTramiteId(id);
        tramiteRepository.delete(tramite);
    }

    private Tramite buscar(String id) {
        return tramiteRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No se encontró el trámite solicitado."));
    }

    private synchronized String generarId() {
        int maximo = tramiteRepository.findAllIds().stream()
                .map(this::sufijoNumerico)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(999);
        return String.format("TX-2026-%04d", maximo + 1);
    }

    private Integer sufijoNumerico(String id) {
        int separador = id.lastIndexOf('-');
        if (separador < 0 || separador == id.length() - 1) {
            return null;
        }
        try {
            return Integer.parseInt(id.substring(separador + 1));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void validarEvidenciaFirma(CrearTramiteRequest request) {
        String esperada = switch (request.tipoFirma()) {
            case SIMPLE -> "SIMPLE_CANVAS";
            case AVANZADA -> "AVANZADA_TOKEN_SIMULADO";
            case NOTARIADA -> "NOTARIADA_SIMULADA";
        };
        if (!esperada.equals(request.firmaEvidencia().trim())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "La evidencia de firma no corresponde al tipo de firma."
            );
        }
    }

    private Antecedentes mapearAntecedentes(AntecedentesRequest request) {
        return new Antecedentes(
                request.nombreCompleto().trim(),
                request.documentoIdentidad().trim(),
                request.correoContacto().trim().toLowerCase(),
                request.comuna().trim()
        );
    }

    private Documento mapearDocumento(DocumentoRequest request) {
        return new Documento(
                request.nombre().trim(),
                request.tamanoKb(),
                EstadoDocumento.ACEPTADO
        );
    }

    private void registrarAviso(Tramite tramite, String mensaje) {
        Aviso aviso = new Aviso();
        aviso.setId("AVS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        aviso.setDestinatarioId(tramite.getSolicitanteId());
        aviso.setTramiteId(tramite.getId());
        aviso.setMensaje(mensaje);
        aviso.setFechaHora(LocalDateTime.now());
        aviso.setLeido(false);
        avisoRepository.save(aviso);
    }

    private String limpiar(String valor) {
        return esBlanco(valor) ? null : valor.trim();
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}
