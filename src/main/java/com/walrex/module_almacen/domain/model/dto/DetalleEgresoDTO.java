package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleEgresoDTO {
    private Long id;
    private Long idOrdenEgreso;
    private Integer idArticulo;
    private Integer idUnidad;
    private Double cantidad;
    private Double valorSalida;
    private String observacion;
    private List<LoteDTO> a_lotes;
}
