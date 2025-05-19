package com.walrex.module_almacen.domain.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class OrdenSalidaDto {
    private Long id_ordensalida;
    private Integer id_motivo;
    private Integer is_interno;
    private Integer id_tipo_comprobante;
    private String num_comprobante;
    private Integer id_origen;
    private Integer id_destino;
    private Date fec_registro;
    private Integer id_usuario;
    private Date fec_entrega;
    private Integer id_usuario_entrega;
    private Integer id_cliente;
    private Integer id_supervisor;
    private String observacion;
}
