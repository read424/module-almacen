package com.walrex.module_almacen.infrastructure.config;

import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenSalidaLogisticaPersistenceAdapter;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.OrdenSalidaTransformacionPersistenceAdapter;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DetailSalidaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenSalidaEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SalidaPersistenceAdapterConfig {

    @Bean
    @Primary
    public OrdenSalidaLogisticaPort ordenSalidaLogisticaPort(
            OrdenSalidaRepository ordenSalidaRepository,
            DetailSalidaRepository detalleSalidaRepository,
            OrdenSalidaEntityMapper ordenSalidaEntityMapper,
            DetailSalidaMapper detailSalidaMapper
            ) {

        return new OrdenSalidaLogisticaPersistenceAdapter(
                ordenSalidaRepository,
                detalleSalidaRepository,
                ordenSalidaEntityMapper,
                detailSalidaMapper
        );
    }

    @Bean
    @Qualifier("transformacionSalida")
    public OrdenSalidaLogisticaPort ordenSalidaTransformacionPort(
            OrdenSalidaRepository ordenSalidaRepository,
            DetailSalidaRepository detalleSalidaRepository,
            DetailSalidaLoteRepository detalleSalidaLoteRepository,
            DetalleInventoryRespository detalleInventoryRespository,
            InventoryRepository inventoryRepository,
            OrdenSalidaEntityMapper ordenSalidaEntityMapper,
            DetailSalidaMapper detailSalidaMapper,
            KardexRepository kardexRepository) {

        return new OrdenSalidaTransformacionPersistenceAdapter(
                ordenSalidaRepository,
                detalleSalidaRepository,
                detalleSalidaLoteRepository,
                detalleInventoryRespository,
                inventoryRepository,
                ordenSalidaEntityMapper,
                detailSalidaMapper,
                kardexRepository
        );
    }
}