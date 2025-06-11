package com.utn.santafe.gestion_licencias.repository;

import com.utn.santafe.gestion_licencias.model.auditoria.AuditoriaUsuario;
import com.utn.santafe.gestion_licencias.model.auditoria.TipoOperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaUsuarioRepository extends JpaRepository<AuditoriaUsuario, Long> {

    List<AuditoriaUsuario> findByUsuarioModificadoIdOrderByFechaModificacionDesc(Long usuarioModificadoId);

    List<AuditoriaUsuario> findByUsuarioQueModificaIdOrderByFechaModificacionDesc(Long usuarioQueModificaId);

    List<AuditoriaUsuario> findByTipoOperacionOrderByFechaModificacionDesc(TipoOperacion tipoOperacion);

    @Query("SELECT a FROM AuditoriaUsuario a WHERE a.fechaModificacion BETWEEN :fechaInicio AND :fechaFin ORDER BY a.fechaModificacion DESC")
    List<AuditoriaUsuario> findByFechaModificacionBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                          @Param("fechaFin") LocalDateTime fechaFin);

    @Query("SELECT a FROM AuditoriaUsuario a WHERE a.usuarioModificadoDni = :dni ORDER BY a.fechaModificacion DESC")
    List<AuditoriaUsuario> findByUsuarioModificadoDni(@Param("dni") String dni);
}
