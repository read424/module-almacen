package com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.mapper;

import com.walrex.module_almacen.domain.model.dto.ArticuloTransformacionDTO;
import com.walrex.module_almacen.domain.model.dto.ItemArticuloTransformacionDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoTransformacionDTO;
import com.walrex.module_almacen.domain.model.dto.UnidadMedidaDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.ItemArticuloRequest;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.OrdenIngresoTransformacionRequestDto;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.UnidadMedidaRequest;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdenIngresoTransformacionRequestMapper {

    @Mapping(source = "idArticulo", target = "articulo.idArticulo")
    @Mapping(source = "unidad", target = "unidad_ingreso", qualifiedByName = "mapUnidad")
    @Mapping(source = "fecIngreso", target = "fec_ingreso")
    @Mapping(source = "detalles", target = "detalles")
    OrdenIngresoTransformacionDTO toOrdenIngreso(OrdenIngresoTransformacionRequestDto dto);
    /*
    @Named("mapArticulo")
    default ArticuloTransformacionDTO mapArticulo(Integer idArticulo) {
        if (idArticulo == null) return null;
        return ArticuloTransformacionDTO.builder()
                .idArticulo(idArticulo)
                .build();
    }
     */

    @Named("mapUnidad")
    default UnidadMedidaDTO mapUnidad(UnidadMedidaRequest unidad) {
        if (unidad == null) return null;
        return UnidadMedidaDTO.builder()
                .value(unidad.getValue())
                .text(unidad.getText())
                .id_medida_si(unidad.getId_medida_si())
                .build();
    }

    // Mapeo para los detalles
    @Mapping(source = "id", target = "id_articulo")
    @Mapping(source = "codigo", target = "cod_articulo")
    @Mapping(source = "descripcion", target = "desc_articulo")
    @Mapping(source = "idUnidad", target = "id_unidad")
    @Mapping(source = "abrevUnidad", target = "abrev_unidad")
    @Mapping(source = "idUnidadConsumo", target = "id_unidad_consumo")
    ItemArticuloTransformacionDTO itemsToDetalles(ItemArticuloRequest item);
}
