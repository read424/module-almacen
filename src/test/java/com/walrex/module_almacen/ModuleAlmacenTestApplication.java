package com.walrex.module_almacen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.walrex.module_almacen")
public class ModuleAlmacenTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModuleAlmacenApplication.class, args);
    }
}
