package integration.com.walrex.module_almacen.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public class AbstractPostgreSQLContainerTest {
    private static final PostgreSQLContainer<?> postgreSQLContainer;

    static {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:13-alpine")
                .withDatabaseName("test_db")
                .withUsername("test_user")
                .withPassword("test_password")
                .withReuse(true)  // Habilitar reutilización entre ejecuciones de pruebas
                .withExposedPorts(5432)
                .withInitScript("db/init-test-db.sql");

        // Iniciar el contenedor al cargar la clase
        postgreSQLContainer.start();

        // Registro de información al iniciar
        System.out.println("🚀 PostgreSQL Container started at: " + postgreSQLContainer.getJdbcUrl());

        // Opcional: registrar un hook de apagado para detener el contenedor al finalizar la JVM
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (postgreSQLContainer.isRunning()) {
                System.out.println("⏹️ Stopping PostgreSQL Container");
                postgreSQLContainer.stop();
            }
        }));
    }

    // Configurar dinámicamente las propiedades de R2DBC para Spring
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Configurar la URL de conexión R2DBC
        registry.add("spring.r2dbc.url", () ->
                String.format("r2dbc:postgresql://%s:%d/%s",
                        postgreSQLContainer.getHost(),
                        postgreSQLContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                        postgreSQLContainer.getDatabaseName())
        );

        // Configurar credenciales para R2DBC
        registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername);
        registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword);

        // Configurar Flyway para migraciones (si se usa)
        registry.add("spring.flyway.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgreSQLContainer::getUsername);
        registry.add("spring.flyway.password", postgreSQLContainer::getPassword);

        // Opcional: configuraciones adicionales de la base de datos
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    /**
     * Proporciona acceso al contenedor para que las clases derivadas puedan utilizarlo
     * directamente si necesitan realizar operaciones específicas con él.
     */
    protected static PostgreSQLContainer<?> getPostgreSQLContainer() {
        return postgreSQLContainer;
    }

    /**
     * Método de utilidad para obtener una conexión JDBC al contenedor
     * (útil para operaciones de setup/teardown si es necesario)
     */
    protected static String getJdbcUrl() {
        return postgreSQLContainer.getJdbcUrl();
    }
}
