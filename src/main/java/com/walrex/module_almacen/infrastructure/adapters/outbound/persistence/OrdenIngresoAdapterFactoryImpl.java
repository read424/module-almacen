package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.input.OrdenIngresoAdapterFactory;
import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenIngreso;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrdenIngresoAdapterFactoryImpl implements OrdenIngresoAdapterFactory {
    private final OrdenIngresoLogisticaPort ordenIngresoLogisticaAdapter;

    @Qualifier("telaCruda")
    private final OrdenIngresoLogisticaPort ordenIngresoTelaCrudaAdapter;

    @Qualifier("transformacion")
    private final OrdenIngresoLogisticaPort ordenIngresoTransformacionAdapter;

    @Override
    public Mono<OrdenIngresoLogisticaPort> getAdapter(TipoOrdenIngreso tipoOrden) {
        if (tipoOrden == null) {
            // Por defecto usar el adaptador general
            return Mono.just(ordenIngresoLogisticaAdapter);
        }

        switch (tipoOrden) {
            case TELA_CRUDA:
                return Mono.just(ordenIngresoTelaCrudaAdapter);
            case TRANSFORMACION:
                return Mono.just(ordenIngresoTransformacionAdapter);
            case LOGISTICA_GENERAL:
            default:
                return Mono.just(ordenIngresoLogisticaAdapter);
        }
    }
}
