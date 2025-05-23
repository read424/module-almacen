package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailSalidaRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.KardexRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenSalidaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrdenSalidaTransformacionPersistenceAdapter implements OrdenSalidaLogisticaPort {
    private final OrdenSalidaRepository ordenSalidaRepository;
    private final DetailSalidaRepository detalleSalidaRepository;
    private final KardexRepository kardexRepository;

    @Override
    @Transactional
    public Mono<OrdenEgresoDTO> guardarOrdenSalida(OrdenEgresoDTO ordenSalida) {
        log.info("Guardando orden de salida por transformación: {}", ordenSalida.getMotivo().getIdMotivo());
        return null;
    }

    @Override
    public Mono<OrdenEgresoDTO> actualizarEstadoEntrega(Integer idOrden, boolean entregado) {
        return null;
    }

    @Override
    public Mono<OrdenEgresoDTO> procesarSalidaPorLotes(OrdenEgresoDTO ordenSalida) {
        return null;
    }
}
