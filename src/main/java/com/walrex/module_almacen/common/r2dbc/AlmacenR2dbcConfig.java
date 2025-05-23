package com.walrex.module_almacen.common.r2dbc;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages =
        "com.walrex.module_almacen.infrastructure.adapters.outbound.persistence.repository"
)
public class AlmacenR2dbcConfig {
}
