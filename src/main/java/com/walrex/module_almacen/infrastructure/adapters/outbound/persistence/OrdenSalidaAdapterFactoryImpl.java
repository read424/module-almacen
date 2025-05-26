package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenSalida;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrdenSalidaAdapterFactoryImpl implements OrdenSalidaAdapterFactory {

    private final OrdenSalidaLogisticaPort ordenSalidaLogisticaAdapter;

    @Qualifier("transformacionSalida")
    private final OrdenSalidaLogisticaPort ordenSalidaTransformacionAdapter;

    @Override
    public Mono<OrdenSalidaLogisticaPort> getAdapter(TipoOrdenSalida tipoOrden) {
        log.debug("Obteniendo adaptador para tipo de orden de salida: {}", tipoOrden);

        if (tipoOrden == null) {
            log.warn("Tipo de orden de salida es null, usando adaptador por defecto");
            return Mono.just(ordenSalidaLogisticaAdapter);
        }
        switch (tipoOrden) {
            case TRANSFORMACION:
                log.debug("Usando adaptador de transformación para salida");
                return Mono.just(ordenSalidaTransformacionAdapter);
            default:
                return Mono.just(ordenSalidaLogisticaAdapter);
        }
    }
}
