package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AprobarSalidaRequestDTO {
    private Integer id_ordensalida;
    private String cod_salida;
    private Integer id_requerimiento;
    private Integer id_tipo_comprobante;
    private String desc_comprobante;
    private Integer id_almacen_origen;
    private Integer id_almacen_destino;
    private String cod_vale;
    private String no_motivo;
    private Integer id_usuario_entrega;
    private String entregado;
    private Integer id_personal_supervisor;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private OffsetDateTime fec_entrega;
    private String cod_partida;
    private List<ProductoSalidaDTO> productos;
}
