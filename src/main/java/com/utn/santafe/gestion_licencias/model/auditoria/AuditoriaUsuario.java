package com.utn.santafe.gestion_licencias.model.auditoria;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class AuditoriaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_modificado_id", nullable = false)
    private Long usuarioModificadoId;

    @Column(name = "usuario_modificado_dni", nullable = false, length = 8)
    private String usuarioModificadoDni;

    @Column(name = "usuario_que_modifica_id", nullable = false)
    private Long usuarioQueModificaId;

    @Column(name = "usuario_que_modifica_dni", nullable = false, length = 8)
    private String usuarioQueModificaDni;

    @Column(name = "campo_modificado", nullable = false, length = 50)
    private String campoModificado;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false)
    private TipoOperacion tipoOperacion;

    // Constructores
    public AuditoriaUsuario() {
        this.fechaModificacion = LocalDateTime.now();
    }

    public AuditoriaUsuario(Long usuarioModificadoId, String usuarioModificadoDni,
                            Long usuarioQueModificaId, String usuarioQueModificaDni,
                            String campoModificado, String valorAnterior, String valorNuevo,
                            TipoOperacion tipoOperacion, String ipOrigen) {
        this();
        this.usuarioModificadoId = usuarioModificadoId;
        this.usuarioModificadoDni = usuarioModificadoDni;
        this.usuarioQueModificaId = usuarioQueModificaId;
        this.usuarioQueModificaDni = usuarioQueModificaDni;
        this.campoModificado = campoModificado;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.tipoOperacion = tipoOperacion;
        this.ipOrigen = ipOrigen;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioModificadoId() { return usuarioModificadoId; }
    public void setUsuarioModificadoId(Long usuarioModificadoId) { this.usuarioModificadoId = usuarioModificadoId; }

    public String getUsuarioModificadoDni() { return usuarioModificadoDni; }
    public void setUsuarioModificadoDni(String usuarioModificadoDni) { this.usuarioModificadoDni = usuarioModificadoDni; }

    public Long getUsuarioQueModificaId() { return usuarioQueModificaId; }
    public void setUsuarioQueModificaId(Long usuarioQueModificaId) { this.usuarioQueModificaId = usuarioQueModificaId; }

    public String getUsuarioQueModificaDni() { return usuarioQueModificaDni; }
    public void setUsuarioQueModificaDni(String usuarioQueModificaDni) { this.usuarioQueModificaDni = usuarioQueModificaDni; }

    public String getCampoModificado() { return campoModificado; }
    public void setCampoModificado(String campoModificado) { this.campoModificado = campoModificado; }

    public String getValorAnterior() { return valorAnterior; }
    public void setValorAnterior(String valorAnterior) { this.valorAnterior = valorAnterior; }

    public String getValorNuevo() { return valorNuevo; }
    public void setValorNuevo(String valorNuevo) { this.valorNuevo = valorNuevo; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String ipOrigen) { this.ipOrigen = ipOrigen; }

    public TipoOperacion getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(TipoOperacion tipoOperacion) { this.tipoOperacion = tipoOperacion; }
}
