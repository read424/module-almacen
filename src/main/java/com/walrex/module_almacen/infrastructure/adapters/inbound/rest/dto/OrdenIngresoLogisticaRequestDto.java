package com.walrex.module_almacen.infrastructure.adapters.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrdenIngresoLogisticaRequestDto {

    @Valid
    private AlmacenTipoIngresoLogisticaRequestDto id_tipo_almacen;

    @Valid
    private MotivoIngresoLogisticaRequestDto motivo;

    @NotNull
    private Integer id_orden;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @NotBlank(message = "Fecha de ingreso es obligatorio ")
    private LocalDateTime fec_ingreso;

    @NotNull(message = "Tipo de comprobante es obligatorio")
    private Integer id_compro;

    @NotBlank(message = "Codigo de serie documento es obligatorio")
    private String nu_serie;

    @NotBlank(message = "Numero de documento es obligatorio")
    private String nu_comprobante;

    @NotBlank(message = "Fecha de Referencia documento es obligatorio")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime fec_ref;

    private String comprobanteRef;

    @NotNull(message = "Cliente/Proveedor es obligatorio")
    private Integer id_cliente;

    private String observacion;

    @Valid
    private List<ItemArticuloLogisticaRequestDto> detalles;
}