package com.walrex.module_almacen.domain.service;

import com.walrex.module_almacen.application.ports.input.AprobarSalidaInsumosUseCase;
import com.walrex.module_almacen.application.ports.input.OrdenSalidaAdapterFactory;
import com.walrex.module_almacen.application.ports.output.OrdenSalidaLogisticaPort;
import com.walrex.module_almacen.domain.model.dto.AprobarSalidaRequerimiento;
import com.walrex.module_almacen.domain.model.dto.OrdenEgresoDTO;
import com.walrex.module_almacen.domain.model.enums.TipoOrdenSalida;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.AprobarSalidaRequestDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.AprobarSalidaResponseDTO;
import com.walrex.module_almacen.infrastructure.adapters.inbound.reactiveweb.dto.ProductoSalidaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AprobarSalidaInsumosService implements AprobarSalidaInsumosUseCase {
    private final OrdenSalidaAdapterFactory salidaAdapterFactory;

    @Override
    @Transactional
    public Mono<AprobarSalidaRequerimiento> aprobarSalidaInsumos(AprobarSalidaRequerimiento request) {
        log.info("Iniciando aprobación de salida de insumos para orden: {}",
                request.getIdOrdenSalida());
        return null;
        // ✅ Filtrar productos seleccionados
        //List<ProductoSalidaDTO> productosSeleccionados = filtrarProductosSeleccionados(request.getProductos());
        //List<ProductoSalidaDTO> productosOmitidos = filtrarProductosOmitidos(request.getProductos());

        //log.info("Productos a aprobar: {}, Productos omitidos: {}",
        //        productosSeleccionados.size(), productosOmitidos.size());

        //if (productosSeleccionados.isEmpty()) {
        //    return Mono.just(crearRespuestaSinAprobaciones(request, productosOmitidos));
        //}
        /*
        return salidaAdapterFactory.getAdapter(TipoOrdenSalida.TRANSFORMACION)
                .flatMap(adapter -> procesarAprobacion(adapter, request, productosSeleccionados, productosOmitidos))
                .doOnSuccess(response ->
                        log.info("✅ Aprobación completada para orden: {} - Productos aprobados: {}",
                                request.getId_ordensalida(), response.getProductosAprobados()))
                .doOnError(error ->
                        log.error("❌ Error en aprobación de salida para orden: {} - Error: {}",
                                request.getId_ordensalida(), error.getMessage(), error));
         */
    }
    /*
    private List<ProductoSalidaDTO> filtrarProductosSeleccionados(List<ProductoSalidaDTO> productos) {
        if (productos == null) {
            return Collections.emptyList();
        }

        return productos.stream()
                .filter(producto -> Boolean.TRUE.equals(producto.getSelected()))
                .collect(Collectors.toList());
    }

    private List<ProductoSalidaDTO> filtrarProductosOmitidos(List<ProductoSalidaDTO> productos) {
        if (productos == null) {
            return Collections.emptyList();
        }

        return productos.stream()
                .filter(producto -> !Boolean.TRUE.equals(producto.getSelected()))
                .collect(Collectors.toList());
    }
    private Mono<AprobarSalidaResponseDTO> procesarAprobacion(
            OrdenSalidaLogisticaPort adapter,
            AprobarSalidaRequestDTO request,
            List<ProductoSalidaDTO> productosSeleccionados,
            List<ProductoSalidaDTO> productosOmitidos) {

        Integer idOrden = Integer.valueOf(request.getId_ordensalida());

        // ✅ Ejecutar actualizarEstadoEntrega para la orden
        return adapter.actualizarEstadoEntrega(idOrden, true)
                .map(ordenActualizada -> construirRespuestaExitosa(
                        request,
                        productosSeleccionados,
                        productosOmitidos,
                        ordenActualizada))
                .onErrorResume(error -> {
                    log.error("Error al actualizar estado de entrega para orden: {}", idOrden, error);
                    return Mono.just(construirRespuestaError(request, error.getMessage()));
                });
    }
     */
    /*
    private AprobarSalidaResponseDTO crearRespuestaSinAprobaciones(
            AprobarSalidaRequestDTO request,
            List<ProductoSalidaDTO> productosOmitidos) {

        return AprobarSalidaResponseDTO.builder()
                .idOrdenSalida(request.getId_ordensalida())
                .codigoSalida(request.getCod_salida())
                .mensaje("No hay productos seleccionados para aprobar")
                .productosAprobados(0)
                .productosOmitidos(productosOmitidos.size())
                .detalleAprobacion(mapearProductosOmitidos(productosOmitidos))
                .fechaAprobacion(OffsetDateTime.now())
                .build();
    }
     */
    /*
    private AprobarSalidaResponseDTO construirRespuestaExitosa(
            AprobarSalidaRequestDTO request,
            List<ProductoSalidaDTO> productosSeleccionados,
            List<ProductoSalidaDTO> productosOmitidos,
            OrdenEgresoDTO ordenActualizada) {

        List<ProductoAprobadoDTO> detalleCompleto = new ArrayList<>();
        detalleCompleto.addAll(mapearProductosAprobados(productosSeleccionados));
        detalleCompleto.addAll(mapearProductosOmitidos(productosOmitidos));

        return AprobarSalidaResponseDTO.builder()
                .idOrdenSalida(request.getId_ordensalida())
                .codigoSalida(ordenActualizada.getCodEgreso())
                .mensaje("Salida de insumos aprobada exitosamente")
                .productosAprobados(productosSeleccionados.size())
                .productosOmitidos(productosOmitidos.size())
                .detalleAprobacion(detalleCompleto)
                .fechaAprobacion(OffsetDateTime.now())
                .build();
    }
     */
    /*
    private AprobarSalidaResponseDTO construirRespuestaError(
            AprobarSalidaRequestDTO request,
            String mensajeError) {

        return AprobarSalidaResponseDTO.builder()
                .idOrdenSalida(request.getId_ordensalida())
                .codigoSalida(request.getCod_salida())
                .mensaje("Error al aprobar salida: " + mensajeError)
                .productosAprobados(0)
                .productosOmitidos(0)
                .detalleAprobacion(Collections.emptyList())
                .fechaAprobacion(OffsetDateTime.now())
                .build();
    }
     */
    /*
    private List<ProductoAprobadoDTO> mapearProductosAprobados(List<ProductoSalidaDTO> productos) {
        return productos.stream()
                .map(producto -> ProductoAprobadoDTO.builder()
                        .idDetalleOrden(producto.getId_detalle_orden())
                        .idArticulo(producto.getId_articulo())
                        .descripcionArticulo(producto.getDesc_articulo())
                        .cantidad(producto.getCantidad())
                        .unidad(producto.getAbrev_unidad())
                        .estado("APROBADO")
                        .motivo("Producto seleccionado para salida")
                        .build())
                .collect(Collectors.toList());
    }
     */
    /*
    private List<ProductoAprobadoDTO> mapearProductosOmitidos(List<ProductoSalidaDTO> productos) {
        return productos.stream()
                .map(producto -> ProductoAprobadoDTO.builder()
                        .idDetalleOrden(producto.getId_detalle_orden())
                        .idArticulo(producto.getId_articulo())
                        .descripcionArticulo(producto.getDesc_articulo())
                        .cantidad(producto.getCantidad())
                        .unidad(producto.getAbrev_unidad())
                        .estado("OMITIDO")
                        .motivo("Producto no seleccionado")
                        .build())
                .collect(Collectors.toList());
    }
     */
}
