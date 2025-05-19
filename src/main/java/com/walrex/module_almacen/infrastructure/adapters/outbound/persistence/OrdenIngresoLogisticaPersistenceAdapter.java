package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.common.Exception.OrdenIngresoException;
import com.walrex.module_almacen.domain.model.DetalleOrdenIngreso;
import com.walrex.module_almacen.domain.model.DetalleRollo;
import com.walrex.module_almacen.domain.model.OrdenIngreso;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetalleRolloEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.KardexEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ArticuloIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenIngresoEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.*;
import io.r2dbc.spi.R2dbcBadGrammarException;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import io.r2dbc.spi.R2dbcException;
import io.r2dbc.spi.R2dbcTransientResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrdenIngresoLogisticaPersistenceAdapter implements OrdenIngresoLogisticaPort {
    private final OrdenIngresoRepository ordenIngresoRepository;
    private final ArticuloRepository articuloRepository;
    private final DetailsIngresoRepository detalleRepository;
    private final DetalleRolloRepository detalleRolloRepository;
    private final KardexRepository kardexRepository;
    private final OrdenIngresoEntityMapper mapper;
    private final ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper;

    @Override
    @Transactional
    public Mono<OrdenIngreso> guardarOrdenIngresoLogistica(OrdenIngreso ordenIngreso) {
        log.info("Guardando orden de ingreso en la base de datos");
        // Validar que existan detalles
        if (ordenIngreso.getDetalles() == null || ordenIngreso.getDetalles().isEmpty()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La orden de ingreso debe tener al menos un detalle"));
        }

        // Convertir modelo de dominio a entidad usando el mapper
        OrdenIngresoEntity entity = mapper.toEntity(ordenIngreso);

        return ordenIngresoRepository.save(entity)
                .doOnSuccess(savedEntity ->
                    log.info("Orden de ingreso guardada con ID: {}", savedEntity.getId_ordeningreso())
                )
                .onErrorResume(R2dbcException.class, ex -> {
                    String prefix;
                    if (ex instanceof R2dbcDataIntegrityViolationException) {
                        prefix = "Error de integridad de datos";
                    } else if (ex instanceof R2dbcBadGrammarException) {
                        prefix = "Error de sintaxis SQL";
                    } else if (ex instanceof R2dbcTransientResourceException) {
                        prefix = "Error transitorio de recursos";
                    } else {
                        prefix = "Error de base de datos";
                    }
                    String errorMsg = prefix + " al guardar la orden: " + ex.getMessage();
                    log.error(errorMsg, ex);
                    return Mono.error(new OrdenIngresoException(errorMsg, ex));
                })
                .onErrorResume(Exception.class, ex->{
                    String errorMsg = "Error no esperado al guardar la orden: " + ex.getMessage();
                    log.error(errorMsg, ex);
                    return Mono.error(new OrdenIngresoException(errorMsg, ex));
                })
                .flatMap(savedEntity ->{
                    Long idOrdenIngreso = savedEntity.getId_ordeningreso();
                    // Procesar cada detalle, calculando la conversión cuando sea necesario
                    List<Mono<DetalleOrdenIngreso>> detallesMonos = ordenIngreso.getDetalles().stream()
                            .map(detalle -> {
                                // Verificar si necesitamos obtener datos de conversión
                                ordenIngreso.setId(savedEntity.getId_ordeningreso().intValue());
                                ordenIngreso.setCod_ingreso(savedEntity.getCod_ingreso());
                                if(detalle.getIdUnidadSalida()==null){
                                    return articuloRepository.getInfoConversionArticulo(
                                                ordenIngreso.getAlmacen().getIdAlmacen(),
                                                detalle.getArticulo().getId()
                                            )
                                            .doOnNext(info -> log.info("✅ Información de conversión encontrada: {}", info))
                                            .flatMap(infoConversion -> {
                                                // Aplicar conversión si las unidades son diferentes
                                                if(!detalle.getIdUnidad().equals(infoConversion.getIdUnidadConsumo())){
                                                    detalle.setIdUnidadSalida(infoConversion.getIdUnidadConsumo());
                                                    detalle.getArticulo().setIs_multiplo(infoConversion.getIsMultiplo());
                                                    detalle.getArticulo().setValor_conv(infoConversion.getValorConv());
                                                    detalle.getArticulo().setStock(infoConversion.getStock());
                                                }else{
                                                    detalle.setIdUnidadSalida(detalle.getIdUnidad());
                                                }
                                                return guardarDetalleOrdenIngreso(detalle, ordenIngreso);
                                            })
                                            .switchIfEmpty(Mono.error(new ResponseStatusException(
                                                    HttpStatus.BAD_REQUEST,
                                                    "No se encontró información de conversión para el artículo: " +
                                                            detalle.getArticulo().getId()
                                                    ))
                                            );
                                }else{
                                    return guardarDetalleOrdenIngreso(detalle, ordenIngreso);
                                }
                            })
                            .collect(Collectors.toList());
                    // Guardar todos los detalles y mapear el resultado
                    return Flux.merge(detallesMonos)
                            .collectList()
                            .map(detallesGuardados -> {
                                // Construir y retornar la orden completa con sus detalles
                                OrdenIngreso ordenCompleta = mapper.toDomain(savedEntity);
                                ordenCompleta.setDetalles(detallesGuardados);
                                return ordenCompleta;
                            });
                });
    }

    // Método auxiliar para encapsular la lógica de guardar un detalle
    private Mono<DetalleOrdenIngreso> guardarDetalleOrdenIngreso(DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
        // Usar el mapper para convertir de dominio a entidad
        DetailsIngresoEntity detalleEntity = articuloIngresoLogisticaMapper.toEntity(detalle);
        // Establecer el id de la orden de ingreso (no estaba en el mapeo)
        detalleEntity.setId_ordeningreso(ordenIngreso.getId().longValue());

        return detalleRepository.save(detalleEntity)
                .doOnSuccess(info ->
                    log.debug("✅ Información de detalle articulo guarado: {}", info)
                )
                .onErrorResume(ex ->{
                    log.error("Error al guardar detalle para artículo {}: Tipo: {}, Mensaje: {}",
                        detalle.getArticulo().getId(),
                        ex.getClass().getName(),
                        ex.getMessage(),
                        ex
                    );
                    return Mono.error(new OrdenIngresoException(
                        "Error al registrar detalle de ingreso",
                        ex)
                    );
                })
                .flatMap(savedDetalleEntity -> {
                    String str_detalle = String.format("(%s) - %s", ordenIngreso.getCod_ingreso(), ordenIngreso.getMotivo().getDescMotivo());
                    BigDecimal mto_total= BigDecimal.valueOf(savedDetalleEntity.getCosto_compra()*savedDetalleEntity.getCantidad());
                    BigDecimal cantidadConvertida;
                    BigDecimal total_stock;
                    // Aplicar conversión si las unidades son diferentes
                    if(!detalle.getIdUnidad().equals(detalle.getIdUnidadSalida())){
                        BigDecimal factorConversion = BigDecimal.valueOf(Math.pow(10, detalle.getArticulo().getValor_conv()));
                        cantidadConvertida = detalle.getCantidad().multiply(factorConversion).setScale(5, RoundingMode.HALF_UP);
                    }else{
                        detalle.setIdUnidadSalida(detalle.getIdUnidad());
                        cantidadConvertida = detalle.getCantidad();
                    }
                    total_stock = cantidadConvertida.add(detalle.getArticulo().getStock()).setScale(6, RoundingMode.HALF_UP);
                    KardexEntity kardexEntity = KardexEntity.builder()
                        .tipo_movimiento(1)//1 - ingreso 2 - salida
                        .detalle(str_detalle)
                        .cantidad(BigDecimal.valueOf(savedDetalleEntity.getCantidad()))
                        .costo(BigDecimal.valueOf(savedDetalleEntity.getCosto_compra()))
                        .valorTotal(mto_total)
                        .fecha_movimiento(ordenIngreso.getFechaIngreso())
                        .id_articulo(savedDetalleEntity.getId_articulo())
                        .id_unidad(savedDetalleEntity.getId_unidad())
                        .id_unidad_salida(detalle.getIdUnidadSalida())
                        .id_almacen(ordenIngreso.getAlmacen().getIdAlmacen())
                        .id_documento(ordenIngreso.getId())
                        .id_detalle_documento(savedDetalleEntity.getId().intValue())
                        .saldo_actual(total_stock)//
                        .saldoLote(cantidadConvertida.setScale(6, RoundingMode.HALF_UP))//
                        .build();

                    return kardexRepository.save(kardexEntity)
                            .doOnSuccess(info -> log.info("✅ Información de kardex guardado: {}", info))
                            .onErrorResume(R2dbcException.class, ex -> {
                                String errorMsg = "Error de base de datos al guardar el kardex: " + ex.getMessage();
                                log.error(errorMsg, ex);
                                return Mono.error(new RuntimeException(errorMsg, ex));
                            })
                            .onErrorResume(Exception.class, ex -> {
                                String errorMsg = "Error no esperado al guardar el kardex: " + ex.getMessage();
                                log.error(errorMsg, ex);
                                return Mono.error(new RuntimeException(errorMsg, ex));
                            }).then(procesarDetallesRollos(detalle, savedDetalleEntity));
                });
    }

    // Método auxiliar para procesar los detalles de rollos
    private Mono<DetalleOrdenIngreso> procesarDetallesRollos(DetalleOrdenIngreso detalle, DetailsIngresoEntity savedDetalleEntity) {
        // Si tiene detalles de rollos, guardarlos también
        if (detalle.getDetallesRollos() != null && !detalle.getDetallesRollos().isEmpty()) {
            return guardarDetallesRollos(detalle.getDetallesRollos(), savedDetalleEntity.getId(), savedDetalleEntity.getId_ordeningreso())
                    .collectList()
                    .map(rollosGuardados -> {
                        // Actualizar el id del detalle guardado
                        detalle.setId(savedDetalleEntity.getId().intValue());
                        // Actualizar los rollos con sus ids generados
                        detalle.setDetallesRollos(rollosGuardados);
                        return detalle;
                    });
        } else {
            // Si no tiene rollos, simplemente devolver el detalle actualizado
            detalle.setId(savedDetalleEntity.getId().intValue());
            return Mono.just(detalle);
        }
    }

    // Método para guardar los detalles de rollos
    private Flux<DetalleRollo> guardarDetallesRollos(List<DetalleRollo> rollos, Long idDetalle, Long idOrdenIngreso) {
        return Flux.fromIterable(rollos)
                .flatMap(rollo -> {
                    // Mapear el rollo a entidad (puedes crear un mapper específico o hacerlo manualmente)
                    DetalleRolloEntity rolloEntity = DetalleRolloEntity.builder()
                        .codRollo(rollo.getCodRollo())
                        .pesoRollo(rollo.getPesoRollo())
                        .idDetOrdenIngreso(idDetalle.intValue())
                        .ordenIngreso(idOrdenIngreso.intValue())
                        .build();

                    return detalleRolloRepository.save(rolloEntity)
                            .doOnSuccess(savedEntity ->
                                log.debug("Rollo guardado con ID: {}", savedEntity.getId())
                            )
                            .map(savedRolloEntity -> {
                                rollo.setId(savedRolloEntity.getId().intValue());
                                rollo.setIdDetOrdenIngreso(idDetalle.intValue());
                                rollo.setOrdenIngreso(idOrdenIngreso.intValue());
                                return rollo;
                            }).onErrorResume(ex -> {
                                String errorMsg = String.format("Error al guardar rollo %s para detalle %d: %s", rollo.getCodRollo(), idDetalle, ex.getMessage());
                                log.error(errorMsg, ex);
                                // Determinar el tipo de error para una excepción específica
                                if (ex instanceof DataIntegrityViolationException) {
                                    return Mono.error(new OrdenIngresoException("Error de integridad de datos al guardar rollo: " +rollo.getCodRollo() + ". El código puede estar duplicado.", ex));
                                } else if (ex instanceof R2dbcException) {
                                    return Mono.error(new OrdenIngresoException("Error de base de datos al guardar rollo: " +rollo.getCodRollo(), ex));
                                }
                                // Error genérico
                                return Mono.error(new OrdenIngresoException(errorMsg, ex));
                            });
                });
    }
}
