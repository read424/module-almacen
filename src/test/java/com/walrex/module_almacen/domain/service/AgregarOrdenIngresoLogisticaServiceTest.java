package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.output.OrdenIngresoLogisticaPort;
import com.walrex.module_almacen.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AgregarOrdenIngresoLogisticaServiceTest {
    @Mock
    private OrdenIngresoLogisticaPort ordenIngresoLogisticaPort;

    @InjectMocks
    private AgregarOrdenIngresoLogisticaService service;

    private OrdenIngreso ordenIngreso;

    @BeforeEach
    void setup() {
        // Crear una orden de ingreso para los tests
        ordenIngreso = OrdenIngreso.builder()
                .id(1)
                .cod_ingreso("TEST-001")
                .almacen(Almacen.builder().idAlmacen(1).tipoAlmacen(1).build())
                .motivo(Motivo.builder().idMotivo(4).descMotivo("Test Motivo").build())
                .fechaIngreso(LocalDate.now())
                .comprobante(1)
                .codSerie("S001")
                .nroComprobante("001-00001")
                .fechaComprobante(LocalDate.now())
                .idCliente(1)
                .observacion("Test observación")
                .detalles(List.of(
                        DetalleOrdenIngreso.builder()
                                .articulo(Articulo.builder().id(1).build())
                                .lote("LOTE-001")
                                .idTipoProducto(1)
                                .idTipoProductoFamilia(1)
                                .idUnidad(1)
                                .cantidad(new BigDecimal("10.0"))
                                .costo(new BigDecimal("100.0"))
                                .montoTotal(new BigDecimal("1000.0"))
                                .build()
                ))
                .build();
    }

    @Test
    void crearOrdenIngresoLogistica_DebeRetornarOrdenIngreso_CuandoGuardadoExitoso() {
        // Arrange
        when(ordenIngresoLogisticaPort.guardarOrdenIngresoLogistica(any(OrdenIngreso.class)))
                .thenReturn(Mono.just(ordenIngreso));

        // Act & Assert
        StepVerifier.create(service.crearOrdenIngresoLogistica(ordenIngreso))
                .expectNext(ordenIngreso)
                .verifyComplete();

        // Verify
        verify(ordenIngresoLogisticaPort).guardarOrdenIngresoLogistica(ordenIngreso);
    }

    @Test
    void crearOrdenIngresoLogistica_DebePropagaError_CuandoFallaGuardado() {
        // Arrange
        RuntimeException expectedException = new RuntimeException("Error al guardar");
        when(ordenIngresoLogisticaPort.guardarOrdenIngresoLogistica(any(OrdenIngreso.class)))
                .thenReturn(Mono.error(expectedException));

        // Act & Assert
        StepVerifier.create(service.crearOrdenIngresoLogistica(ordenIngreso))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Error al guardar")
                )
                .verify();

        // Verify
        verify(ordenIngresoLogisticaPort).guardarOrdenIngresoLogistica(ordenIngreso);
    }
}
