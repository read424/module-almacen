package com.walrex.module_almacen.infrastructure.config;

import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenIngresoLogisticaPersistenceAdapter;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenIngresoTelaCrudaPersistenceAdapter;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ArticuloIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenIngresoEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PersistenceAdapterConfig {
    @Bean
    @Primary
    public OrdenIngresoLogisticaPort ordenIngresoLogisticaPort(
            OrdenIngresoRepository ordenIngresoRepository,
            ArticuloRepository articuloRepository,
            DetailsIngresoRepository detalleRepository,
            OrdenIngresoEntityMapper mapper,
            ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper,
            KardexRepository kardexRepository) {

        return OrdenIngresoLogisticaPersistenceAdapter.builder()
                .ordenIngresoRepository(ordenIngresoRepository)
                .articuloRepository(articuloRepository)
                .detalleRepository(detalleRepository)
                .mapper(mapper)
                .articuloIngresoLogisticaMapper(articuloIngresoLogisticaMapper)
                .kardexRepository(kardexRepository)
                .build();
    }

    @Bean
    @Qualifier("telaCruda")
    public OrdenIngresoLogisticaPort ordenIngresoTelaCrudaPort(
            OrdenIngresoRepository ordenIngresoRepository,
            ArticuloRepository articuloRepository,
            DetailsIngresoRepository detalleRepository,
            OrdenIngresoEntityMapper mapper,
            ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper,
            DetalleRolloRepository detalleRolloRepository) {

        return OrdenIngresoTelaCrudaPersistenceAdapter.builder()
                .ordenIngresoRepository(ordenIngresoRepository)
                .articuloRepository(articuloRepository)
                .detalleRepository(detalleRepository)
                .mapper(mapper)
                .articuloIngresoLogisticaMapper(articuloIngresoLogisticaMapper)
                .detalleRolloRepository(detalleRolloRepository)
                .build();
    }
}
