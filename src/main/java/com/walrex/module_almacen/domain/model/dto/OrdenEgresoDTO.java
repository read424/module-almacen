package com.walrex.module_almacen.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenEgresoDTO {
    private Long id;
    private Integer idMotivo;
    private Integer idAlmacen;
    private String observacion;
    private LocalDate fechaEgreso;
    private List<DetalleEgresoDTO> detalles;
}
