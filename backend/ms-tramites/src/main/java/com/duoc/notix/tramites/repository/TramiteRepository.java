package com.duoc.notix.tramites.repository;

import com.duoc.notix.tramites.model.Tramite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TramiteRepository extends JpaRepository<Tramite, String> {

    List<Tramite> findAllByOrderByFechaCreacionDesc();

    List<Tramite> findBySolicitanteIdOrderByFechaCreacionDesc(String solicitanteId);

    @Query("select t.id from Tramite t")
    List<String> findAllIds();
}
