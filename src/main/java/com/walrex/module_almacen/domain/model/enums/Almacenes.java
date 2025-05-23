package com.walrex.module_almacen.domain.model.enums;

public enum Almacenes {
    INSUMOS(1),
    TELA_CRUDA(2);

    private final int id;

    Almacenes(int id){
        this.id=id;
    }

    public int getId(){
        return id;
    }

    public static Almacenes fromId(int id) {
        for (Almacenes almacen : values()) {
            if (almacen.id == id) {
                return almacen;
            }
        }
        throw new IllegalArgumentException("Almacén no válido: " + id);
    }
}
