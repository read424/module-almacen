package com.walrex.module_almacen.application.ports.output;

import com.walrex.module_almacen.domain.model.dto.GuiaRemisionGeneradaDataDTO;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para enviar eventos de guía de remisión
 * al módulo de comprobantes (E-comprobantes) a través de Kafka.
 */
public interface EnviarGuiaRemisionEventPort {

    /**
     * Envía un evento de guía de remisión al topic create-comprobante-guia-remision
     *
     * @param idOrdenSalida      ID de la orden de salida para la cual se generó la
     *                           guía
     * @param correlationId      ID de correlación para vincular con la solicitud
     * @param isComprobanteSUNAT Indica si el comprobante es SUNAT
     * @return Un Mono que completa cuando el evento ha sido enviado exitosamente
     */
    Mono<Void> enviarEventoGuiaRemision(GuiaRemisionGeneradaDataDTO guiaRemisionGenerada, String correlationId,
            Boolean isComprobanteSUNAT);
}