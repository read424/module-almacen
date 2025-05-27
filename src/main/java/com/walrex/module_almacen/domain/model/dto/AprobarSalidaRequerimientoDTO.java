package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AprobarSalidaRequerimientoDTO {
    private Integer idOrdenSalida;
    private String codOrdenSalida;
    private Integer idRequerimiento;
    private Integer idTipoComprobante;
    private Integer idAlmacenOrigen;
    private Integer idAlmacenDestino;
    private Integer idUsuarioEntrega;
    private String entregado;
    private Integer idUsuarioSupervisor;
    private Date fecEntrega;
    private List<ArticuloRequerimientoDTO> detalles;
}
