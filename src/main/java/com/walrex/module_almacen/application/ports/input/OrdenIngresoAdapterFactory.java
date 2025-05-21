package com.walrex.module_almacen.application.ports.input;

import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenIngreso;
import reactor.core.publisher.Mono;

public interface OrdenIngresoAdapterFactory {
    Mono<OrdenIngresoLogisticaPort> getAdapter(TipoOrdenIngreso tipoOrden);
}
