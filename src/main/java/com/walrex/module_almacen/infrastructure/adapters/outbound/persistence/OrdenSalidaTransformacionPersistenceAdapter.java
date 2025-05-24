package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.dto.DetalleEgresoDTO;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.DetailSalidaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenSalidaEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;

@Component
@Qualifier("transformacionSalida")
@RequiredArgsConstructor
@Slf4j
public class OrdenSalidaTransformacionPersistenceAdapter implements OrdenSalidaLogisticaPort {
    private final OrdenSalidaRepository ordenSalidaRepository;
    private final DetailSalidaRepository detalleSalidaRepository;
    private final DetailSalidaLoteRepository detalleSalidaLoteRepository;
    private final DetalleInventoryRespository detalleInventoryRespository;
    private final InventoryRepository inventoryRepository;
    private final OrdenSalidaEntityMapper ordenSalidaEntityMapper;
    private final DetailSalidaMapper detailSalidaMapper;
    private final KardexRepository kardexRepository;

    @Override
    @Transactional
    public Mono<OrdenEgresoDTO> guardarOrdenSalida(OrdenEgresoDTO ordenSalida) {
        log.info("Guardando orden de salida por transformación: {}", ordenSalida.getMotivo().getIdMotivo());
        return guardarOrdenPrincipal(ordenSalida)
                .flatMap(this::guardarDetalles)
                .flatMap(this::procesarEntregaYLotes)
                .flatMap(this::registrarKardexConLotes)
                .flatMap(orden -> actualizarEstadoEntrega(orden.getId().intValue(), true))
                .doOnSuccess(orden ->
                        log.info("✅ Orden de salida por transformación completada: {}", orden.getId()));
    }

    private Mono<OrdenEgresoDTO> guardarOrdenPrincipal(OrdenEgresoDTO ordenSalida) {
        log.debug("Guardando orden principal de salida");
        Integer id_usuario = 18;
        OrdenSalidaEntity entity = ordenSalidaEntityMapper.toEntity(ordenSalida);
        entity.setEntregado(1); // Inicialmente entregado
        entity.setCreate_at(OffsetDateTime.now());
        entity.setFec_entrega(Date.from(Instant.now()));
        entity.setId_user_entrega(id_usuario);
        entity.setId_usuario(id_usuario);
        entity.setStatus(1); // Activa

        return ordenSalidaRepository.save(entity)
                .map(savedEntity -> {
                    ordenSalida.setId(savedEntity.getId_ordensalida());
                    ordenSalida.setCodEgreso(savedEntity.getCod_salida());
                    return ordenSalida;
                })
                .doOnSuccess(orden ->
                        log.debug("Orden principal guardada con ID: {}", orden.getId()));
    }

    private Mono<OrdenEgresoDTO> guardarDetalles(OrdenEgresoDTO ordenSalida) {
        log.debug("Guardando {} detalles de salida", ordenSalida.getDetalles().size());

        return Flux.fromIterable(ordenSalida.getDetalles())
                .flatMap(detalle -> guardarDetalle(detalle, ordenSalida.getId()))
                .collectList()
                .map(detallesGuardados -> {
                    ordenSalida.setDetalles(detallesGuardados);
                    return ordenSalida;
                });
    }

    private Mono<DetalleEgresoDTO> guardarDetalle(DetalleEgresoDTO detalle, Long idOrdenSalida) {
        detalle.setIdOrdenEgreso(idOrdenSalida);
        DetailSalidaEntity detalleEntity = detailSalidaMapper.toEntity(detalle);

        return detalleSalidaRepository.save(detalleEntity)
                .map(savedEntity -> {
                    detalle.setId(savedEntity.getId_detalle_orden());
                    detalle.setIdOrdenEgreso(idOrdenSalida);
                    return detalle;
                })
                .doOnSuccess(savedDetalle ->
                        log.debug("Detalle guardado: artículo {} con ID {}",
                                savedDetalle.getArticulo().getId(), savedDetalle.getId()));
    }

    // ✅ Nuevo método para procesar entrega y activar triggers
    private Mono<OrdenEgresoDTO> procesarEntregaYLotes(OrdenEgresoDTO ordenSalida) {
        log.debug("Procesando entrega y activando triggers de lotes");

        return Flux.fromIterable(ordenSalida.getDetalles())
                .flatMap(detalle ->
                        detalleSalidaRepository.assignedDelivered(detalle.getId().intValue())
                                .doOnSuccess(updated ->
                                        log.debug("Detalle {} marcado como entregado, trigger de lotes ejecutado",
                                                detalle.getId()))
                )
                .then(Mono.just(ordenSalida));
    }

    // ✅ Registrar kardex usando datos de detalle_salida_lote
    private Mono<OrdenEgresoDTO> registrarKardexConLotes(OrdenEgresoDTO ordenSalida) {
        log.debug("Registrando kardex con información de lotes");

        return Flux.fromIterable(ordenSalida.getDetalles())
                .flatMap(detalle -> registrarKardexPorDetalle(detalle, ordenSalida))
                .then(Mono.just(ordenSalida));
    }

    private Mono<Void> registrarKardexPorDetalle(DetalleEgresoDTO detalle, OrdenEgresoDTO ordenSalida) {
        // ✅ Obtener los lotes creados por el trigger
        return detalleSalidaLoteRepository.findByIdDetalleOrden(detalle.getId())
                .flatMap(salidaLote -> registrarKardexPorLote(salidaLote, detalle, ordenSalida))
                .then();
    }

    private Mono<Void> registrarKardexPorLote(DetailSalidaLoteEntity salidaLote,
                                              DetalleEgresoDTO detalle,
                                              OrdenEgresoDTO ordenSalida) {

        // ✅ Consultar saldo actual del artículo
        Mono<BigDecimal> saldoStockMono = inventoryRepository.getStockInStorage(detalle.getArticulo().getId(), ordenSalida.getAlmacenOrigen().getIdAlmacen())
                .map(articulo -> articulo.getStock())
                .switchIfEmpty(Mono.just(BigDecimal.ZERO));

        // ✅ Consultar saldo actual del lote
        Mono<BigDecimal> saldoLoteMono = detalleInventoryRespository.getStockLote(salidaLote.getId_lote())
                .map(lote -> BigDecimal.valueOf(lote.getCantidadDisponible()))
                .switchIfEmpty(Mono.just(BigDecimal.ZERO));

        return Mono.zip(saldoStockMono, saldoLoteMono)
                .flatMap(tuple -> {
                    BigDecimal saldoStockActual = tuple.getT1();
                    BigDecimal saldoLoteActual = tuple.getT2();

                    // ✅ Calcular nuevos saldos (restar cantidad salida)
                    BigDecimal cantidadSalida = BigDecimal.valueOf(salidaLote.getCantidad());
                    BigDecimal nuevoSaldoStock = saldoStockActual.subtract(cantidadSalida);
                    BigDecimal nuevoSaldoLote = saldoLoteActual.subtract(cantidadSalida);

                    // ✅ Crear registro de kardex
                    KardexEntity kardexEntity = KardexEntity.builder()
                            .tipo_movimiento(4) // Transformación
                            .detalle(String.format("SALIDA TRANSFORMACIÓN - %s", ordenSalida.getCodEgreso()))
                            .cantidad(cantidadSalida.negate()) // Negativo para salida
                            .costo(BigDecimal.valueOf(salidaLote.getMonto_consumo()))
                            .valorTotal(BigDecimal.valueOf(salidaLote.getTotal_monto()).negate())
                            .fecha_movimiento(LocalDate.now())
                            .id_articulo(detalle.getArticulo().getId())
                            .id_unidad(detalle.getIdUnidad())
                            .id_unidad_salida(detalle.getIdUnidad())
                            .id_almacen(ordenSalida.getAlmacenOrigen().getIdAlmacen())
                            .saldo_actual(nuevoSaldoStock) // ✅ Saldo actualizado
                            .id_documento(ordenSalida.getId().intValue())
                            .id_lote(salidaLote.getId_lote())
                            .id_detalle_documento(detalle.getId().intValue())
                            .saldoLote(nuevoSaldoLote) // ✅ Saldo de lote actualizado
                            .build();

                    return kardexRepository.save(kardexEntity)
                            .doOnSuccess(kardex ->
                                    log.info("✅ Kardex registrado: artículo {} lote {} cantidad {}",
                                            detalle.getArticulo().getId(),
                                            salidaLote.getId_lote(),
                                            cantidadSalida))
                            .then();
                });
    }

    @Override
    public Mono<OrdenEgresoDTO> actualizarEstadoEntrega(Integer idOrden, boolean entregado) {
        log.info("Actualizando estado de entrega para orden: {} a {}", idOrden, entregado);

        Date fechaEntrega = entregado ? new Date() : null;

        return ordenSalidaRepository.asignarEntregado(fechaEntrega, 1, 1, 1, idOrden)
                .flatMap(entity ->
                        // Actualizar detalles como entregados
                        actualizarDetallesEntregados(idOrden.longValue())
                                .then(Mono.just(ordenSalidaEntityMapper.toDomain(entity)))
                )
                .doOnSuccess(orden ->
                        log.info("Estado de entrega actualizado para orden: {}", idOrden));
    }

    private Mono<Void> actualizarDetallesEntregados(Long idOrdenSalida) {
        // Aquí podrías actualizar los detalles individuales si es necesario
        // Por ahora, el trigger de BD se encarga de esto
        return Mono.empty();
    }

    @Override
    public Mono<OrdenEgresoDTO> procesarSalidaPorLotes(OrdenEgresoDTO ordenSalida) {
        log.info("Procesando salida por lotes para orden: {}", ordenSalida.getId());

        // La lógica de lotes la manejan los triggers de BD por ahora
        // Aquí podrías implementar la lógica FIFO si decides migrarla a Java

        return Mono.just(ordenSalida)
                .doOnSuccess(orden ->
                        log.info("Salida por lotes procesada para orden: {}", orden.getId()));
    }
}