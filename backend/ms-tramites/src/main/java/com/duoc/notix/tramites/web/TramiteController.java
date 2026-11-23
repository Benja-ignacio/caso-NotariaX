package com.duoc.notix.tramites.web;

import com.duoc.notix.tramites.dto.CambioEstadoRequest;
import com.duoc.notix.tramites.dto.CrearTramiteRequest;
import com.duoc.notix.tramites.dto.ReenvioRequest;
import com.duoc.notix.tramites.dto.TramiteResponse;
import com.duoc.notix.tramites.service.TramiteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tramites")
public class TramiteController {

    private final TramiteService service;

    public TramiteController(TramiteService service) {
        this.service = service;
    }

    @GetMapping
    public List<TramiteResponse> listar() {
        return service.listar();
    }

    @GetMapping("/solicitante/{solicitanteId}")
    public List<TramiteResponse> listarPorSolicitante(@PathVariable String solicitanteId) {
        return service.listarPorSolicitante(solicitanteId);
    }

    @GetMapping("/{id}")
    public TramiteResponse obtener(@PathVariable String id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TramiteResponse crear(@Valid @RequestBody CrearTramiteRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}/estado")
    public TramiteResponse cambiarEstado(
            @PathVariable String id,
            @Valid @RequestBody CambioEstadoRequest request
    ) {
        return service.cambiarEstado(id, request);
    }

    @PutMapping("/{id}/reenvio")
    public TramiteResponse reenviar(
            @PathVariable String id,
            @Valid @RequestBody ReenvioRequest request
    ) {
        return service.reenviar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}
