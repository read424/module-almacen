package com.walrex.module_almacen.domain.model.enums;

import lombok.Getter;

@Getter
public enum TypeMotivoEgreso {
    TRANSFORMACION(20, "TRANSFORMACION", 14),
    MOVIMIENTO_ALMACEN(36, "MOVIMIENTO ALM. INSUMO", 32),
    DEVOLUCION_ROLLOS(37, "DEVOLUCION DE SERVICIO", 33);

    private final int id;
    private final String descMotivo;
    private final int idRelacionMotivo;

    TypeMotivoEgreso(int id, String descMotivo, int idRelacionMotivo) {
        this.id = id;
        this.descMotivo = descMotivo;
        this.idRelacionMotivo = idRelacionMotivo;
    }

    public static TypeMotivoEgreso fromId(int id) {
        for (TypeMotivoEgreso motivo : values()) {
            if (motivo.id == id) {
                return motivo;
            }
        }
        throw new IllegalArgumentException("Motivo no válido: " + id);
    }
}
