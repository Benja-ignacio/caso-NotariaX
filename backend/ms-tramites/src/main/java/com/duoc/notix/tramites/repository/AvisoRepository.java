package com.duoc.notix.tramites.repository;

import com.duoc.notix.tramites.model.Aviso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvisoRepository extends JpaRepository<Aviso, String> {

    List<Aviso> findByDestinatarioIdOrderByFechaHoraDesc(String destinatarioId);

    void deleteByTramiteId(String tramiteId);
}
