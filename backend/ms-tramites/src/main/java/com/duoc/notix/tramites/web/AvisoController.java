package com.duoc.notix.tramites.web;

import com.duoc.notix.tramites.dto.AvisoResponse;
import com.duoc.notix.tramites.service.AvisoService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/avisos")
public class AvisoController {

    private final AvisoService service;

    public AvisoController(AvisoService service) {
        this.service = service;
    }

    @GetMapping("/{usuarioId}")
    public List<AvisoResponse> listar(@PathVariable String usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }

    @PutMapping("/{id}/leido")
    public AvisoResponse marcarLeido(@PathVariable String id) {
        return service.marcarLeido(id);
    }
}
