package com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository;

import com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.entity.OrdenSalidaEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface OrdenSalidaRepository extends ReactiveCrudRepository<OrdenSalidaEntity, Long> {

    @Query("INSERT INTO almacenes.ordensalida (id_motivo, id_almacen_origen, id_usuario, fec_entrega, entregado) "+
            " VALUES (:id_motivo, :id_almacen, :id_usuario, :fec_entrega, :entregado)")
    Mono<OrdenSalidaEntity> agregarOrdenSalida(Integer id_motivo, Integer id_almacen, Integer id_usuario, LocalDate fec_entrega, Integer entregado);
}
