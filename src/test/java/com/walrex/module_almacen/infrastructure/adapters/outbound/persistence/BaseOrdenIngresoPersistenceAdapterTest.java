package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence;

import com.walrex.module_almacen.common.Exception.OrdenIngresoException;
import com.walrex.module_almacen.domain.model.*;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.ArticuloEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.DetailsIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenIngresoEntity;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.ArticuloIngresoLogisticaMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.mapper.OrdenIngresoEntityMapper;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.ArticuloRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.DetailsIngresoRepository;
import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository.OrdenIngresoRepository;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BaseOrdenIngresoPersistenceAdapterTest {
    @Mock
    private OrdenIngresoRepository ordenIngresoRepository;

    @Mock
    private ArticuloRepository articuloRepository;

    @Mock
    private DetailsIngresoRepository detalleRepository;

    @Mock
    private OrdenIngresoEntityMapper mapper;

    @Mock
    private ArticuloIngresoLogisticaMapper articuloIngresoLogisticaMapper;

    // Implementación de prueba de la clase abstracta
    private TestOrdenIngresoPersistenceAdapter adapter;

    // Objetos de prueba comunes
    private OrdenIngreso ordenIngreso;
    private OrdenIngresoEntity ordenIngresoEntity;
    private DetalleOrdenIngreso detalle;
    private DetailsIngresoEntity detalleEntity;
    private ArticuloEntity articuloEntity;

    // Orden de ingreso base para las pruebas
    private OrdenIngreso crearOrdenIngresoBase() {
        return OrdenIngreso.builder()
                .idCliente(86)
                .motivo(Motivo.builder().idMotivo(4).descMotivo("COMPRAS").build())
                .comprobante(1)
                .codSerie("F001")
                .nroComprobante("1120")
                .fechaIngreso(LocalDate.parse("2025-03-31"))
                .fechaComprobante(LocalDate.parse("2025-03-31"))
                .observacion("")
                .almacen(Almacen.builder().idAlmacen(1).build())
                .idOrdenCompra(13232)
                .detalles(Collections.emptyList())
                .build();
    }

    private OrdenIngresoEntity crearOrdenIngresoEntity(){
        return OrdenIngresoEntity.builder()
                .id_ordeningreso(1L)
                .id_cliente(86)
                .id_motivo(4)
                .id_comprobante(1)
                .nu_serie("F001")
                .nu_comprobante("1120")
                .fec_ingreso(Date.from(LocalDate.parse("2025-03-31").atStartOfDay(ZoneId.of("America/Lima")).toInstant()))
                .fec_referencia(Date.from(LocalDate.parse("2025-03-31").atStartOfDay(ZoneId.of("America/Lima")).toInstant()))
                .observacion("")
                .id_almacen(1)
                .id_comprobante(13232)
                .build();
    }

    private DetailsIngresoEntity crearDetalleProducto(){
        return DetailsIngresoEntity.builder()
                .id_articulo(289)
                .lote("001120-1")
                .id_unidad(1)
                .cantidad(240.00)
                .costo_compra(2.15)
                .id_moneda(2)
                .build();
    }

    // Detalle de orden de prueba
    private DetalleOrdenIngreso crearDetalleOrdenIngreso() {
        return DetalleOrdenIngreso.builder()
                .articulo(Articulo.builder()
                    .id(289)
                    .build()
                )
                .idUnidad(1)
                .lote("001120-1")
                .cantidad(BigDecimal.valueOf(240.0000))
                .idTipoProducto(1)
                .costo(BigDecimal.valueOf(2.15))
                .idMoneda(2)
                .build();
    }

    @BeforeEach
    void setUp() {
        // Inicializar el adaptador con los mocks
        adapter = TestOrdenIngresoPersistenceAdapter.builder()
                .ordenIngresoRepository(ordenIngresoRepository)
                .articuloRepository(articuloRepository)
                .detalleRepository(detalleRepository)
                .mapper(mapper)
                .articuloIngresoLogisticaMapper(articuloIngresoLogisticaMapper)
                .build();

        // Inicializar objetos comunes de prueba
        Articulo articulo = Articulo.builder()
                .id(289)
                .build();

        detalle = DetalleOrdenIngreso.builder()
                .articulo(articulo)
                .idUnidad(1)
                .lote("001120-1")
                .cantidad(BigDecimal.valueOf(240.0000))
                .idTipoProducto(1)
                .costo(BigDecimal.valueOf(2.15))
                .idMoneda(2)
                .build();

        articuloEntity = ArticuloEntity.builder()
                .idArticulo(289)
                .idUnidad(1)
                .idUnidadConsumo(6)
                .valorConv(1)
                .isMultiplo("1")
                .stock(BigDecimal.valueOf(100.00))
                .build();

        List<DetalleOrdenIngreso> detalles = new ArrayList<>();
        detalles.add(detalle);

        ordenIngreso = crearOrdenIngresoBase();

        ordenIngresoEntity = crearOrdenIngresoEntity();
    }

    @Test
    void guardarOrdenIngresoLogistica_DeberiaRetornarError_CuandoNoHayDetalles() {
        // Arrange
        OrdenIngreso ordenSinDetalles = crearOrdenIngresoBase();

        // Act & Assert
        StepVerifier.create(adapter.guardarOrdenIngresoLogistica(ordenSinDetalles))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode() == HttpStatus.BAD_REQUEST &&
                                ((ResponseStatusException) throwable).getReason().contains("debe tener al menos un detalle")
                )
                .verify();

        // Verify
        verifyNoInteractions(ordenIngresoRepository);
    }

    // Clase interna para pruebas que implementa la clase abstracta
    @SuperBuilder
    private static class TestOrdenIngresoPersistenceAdapter extends BaseOrdenIngresoPersistenceAdapter {
        private DetalleOrdenIngreso returnDetalle;

        public void setReturnDetalle(DetalleOrdenIngreso detalle) {
            this.returnDetalle = detalle;
        }

        // Métodos para acceder a métodos privados de la clase base
        public Mono<DetalleOrdenIngreso> testProcesarDetalle(DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
            return procesarDetalle(detalle, ordenIngreso);
        }

        public Mono<ArticuloEntity> testBuscarInfoConversion(DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
            return buscarInfoConversion(detalle, ordenIngreso);
        }

        public Mono<DetalleOrdenIngreso> testAplicarConversion(DetalleOrdenIngreso detalle, ArticuloEntity infoConversion) {
            return aplicarConversion(detalle, infoConversion);
        }

        public Mono<DetalleOrdenIngreso> testGuardarDetalleOrdenIngreso(DetalleOrdenIngreso detalle, OrdenIngreso ordenIngreso) {
            return guardarDetalleOrdenIngreso(detalle, ordenIngreso);
        }

        @Override
        protected Mono<DetalleOrdenIngreso> procesarDetalleGuardado(
                DetalleOrdenIngreso detalle,
                DetailsIngresoEntity savedDetalleEntity,
                OrdenIngreso ordenIngreso) {
            // Implementación simple para pruebas
            detalle.setId(savedDetalleEntity.getId().intValue());
            return Mono.just(returnDetalle != null ? returnDetalle : detalle);
        }
    }
}
