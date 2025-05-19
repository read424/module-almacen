package com.walrex.module_almacen.domain.service;

import com.walrex.avro.schemas.CreateOrdeningresoMessage;
import com.walrex.module_almacen.application.ports.input.CrearOrdenIngresoUseCase;
import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgregarOrdenIngresoLogisticaService implements CrearOrdenIngresoUseCase {
    private final OrdenIngresoLogisticaPort ordenIngresoLogisticaPort;

    @Override
    public Mono<OrdenIngreso> crearOrdenIngresoLogistica(OrdenIngreso ordenIngresoDTO) {
        log.info("Iniciando registro de orden de ingreso");
        return ordenIngresoLogisticaPort.guardarOrdenIngresoLogistica(ordenIngresoDTO);
    }

    @Override
    public Mono<OrdenIngresoResponseDTO> procesarMensajeOrdenIngreso(CreateOrdeningresoMessage message) {
        return null;
    }
}
