package com.duoc.notix.tramites.service;

import com.duoc.notix.tramites.dto.AvisoResponse;
import com.duoc.notix.tramites.model.Aviso;
import com.duoc.notix.tramites.repository.AvisoRepository;
import com.duoc.notix.tramites.web.ApiException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AvisoService {

    private final AvisoRepository repository;

    public AvisoService(AvisoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AvisoResponse> listarPorUsuario(String usuarioId) {
        return repository.findByDestinatarioIdOrderByFechaHoraDesc(usuarioId)
                .stream()
                .map(AvisoResponse::from)
                .toList();
    }

    public AvisoResponse marcarLeido(String id) {
        Aviso aviso = repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No se encontró el aviso solicitado."));
        aviso.setLeido(true);
        return AvisoResponse.from(repository.save(aviso));
    }
}
