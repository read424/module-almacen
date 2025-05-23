package com.walrex.module_almacen.domain.model.mapper;

import com.walrex.module_almacen.domain.model.Almacen;
import com.walrex.module_almacen.domain.model.Articulo;
import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoTransformacionDTO;
import com.walrex.module_almacen.domain.model.enums.Almacenes;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenIngresoTransformacionMapper {

    @Mapping(source = "fec_ingreso", target = "fechaIngreso")
    @Mapping(target = "almacen", qualifiedByName = "mapAlmacenDefecto")
    @Mapping(target = "detalles", qualifiedByName = "mapDetallesTransformacion")
    OrdenIngreso toOrdenIngreso(OrdenIngresoTransformacionDTO dto);

    @Named("mapAlmacenDefecto")
    default Almacen mapAlmacenDefecto(OrdenIngresoTransformacionDTO dto) {
        // Si viene almacén en el DTO, usarlo. Si no, usar por defecto
        if (dto.getAlmacen() != null && dto.getAlmacen().getIdAlmacen() != null) {
            return dto.getAlmacen();
        }
        return Almacen.builder()
                .idAlmacen(Almacenes.INSUMOS.getId())
                .build(); // ✅ Valor por defecto
    }

    @Named("mapDetallesTransformacion")
    default List<DetalleOrdenIngreso> mapDetallesTransformacion(OrdenIngresoTransformacionDTO dto){
        if (dto == null || dto.getArticulo() == null) {
            return Collections.emptyList();
        }
        List<DetalleOrdenIngreso> detalles = new ArrayList<>();
        DetalleOrdenIngreso item = DetalleOrdenIngreso.builder()
            .articulo(Articulo.builder()
                .id(dto.getArticulo().getIdArticulo())
                .build()
            )
            .idUnidad(dto.getUnidad_ingreso().getValue())
            .cantidad(BigDecimal.valueOf(dto.getCantidad()))
            .costo(BigDecimal.valueOf(dto.getPrecio()))
            .build();
        detalles.add(item);
        return detalles;
    }
}