package com.walrex.module_almacen.domain.model.enums;

public enum Almacen {
    INSUMOS(1),
    TELA_CRUDA(2);

    private final int id;

    Almacen(int id){
        this.id=id;
    }

    public int getId(){
        return id;
    }

    public static Almacen fromId(int id) {
        for (Almacen almacen : values()) {
            if (almacen.id == id) {
                return almacen;
            }
        }
        throw new IllegalArgumentException("Almacén no válido: " + id);
    }
}
