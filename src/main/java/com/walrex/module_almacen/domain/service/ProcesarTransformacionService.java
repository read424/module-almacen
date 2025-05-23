package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.OrdenIngresoAdapterFactory;
import com.walrex.module_almacen.application.ports.input.ProcesarTransformacionUseCase;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.domain.model.dto.OrdenIngresoTransformacionDTO;
import com.walrex.module_almacen.domain.model.dto.TransformacionResponseDTO;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenIngreso;
import com.walrex.module_almacen.domain.model.mapper.OrdenIngresoTransformacionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcesarTransformacionService implements ProcesarTransformacionUseCase {
    private final OrdenIngresoAdapterFactory adapterFactory;
    private final OrdenIngresoTransformacionMapper ordenIngresoTransformacionMapper;

    @Override
    @Transactional
    public Mono<TransformacionResponseDTO> procesarTransformacion(OrdenIngresoTransformacionDTO request) {
        log.info("Iniciando proceso de transformación para artículo: {}",
                request.getArticulo().getIdArticulo());
        return procesarIngreso(request)
                .map(response -> TransformacionResponseDTO.builder()
                        .id_transformacion(1L)
                        .build());
                //.flatMap(ingresoCreado->procesarSalidas(request, ingresoCreado));
    }

    private Mono<OrdenIngreso> procesarIngreso(OrdenIngresoTransformacionDTO request) {
        // Mapear OrdenIngresoTransformacionDTO → OrdenIngreso
        OrdenIngreso ordenIngreso = ordenIngresoTransformacionMapper.toOrdenIngreso(request);

        return adapterFactory.getAdapter(TipoOrdenIngreso.TRANSFORMACION)
                .flatMap(adapter -> adapter.guardarOrdenIngresoLogistica(ordenIngreso));
    }

    /*
    private Mono<List<OrdenSalida>> procesarSalidas(OrdenIngresoTransformacionDTO request, OrdenIngreso ingresoCreado) {
        // Mapear List<ItemArticuloTransformacionDTO> → List<OrdenSalida>
        // Para cada insumo, crear una orden de salida
    }
     */
}
