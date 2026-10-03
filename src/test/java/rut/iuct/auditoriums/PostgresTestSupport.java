package rut.iuct.auditoriums;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/** По умолчанию используется одноразовый контейнер; рабочие настройки не читаются. */
public abstract class PostgresTestSupport {
    private static PostgreSQLContainer container;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        String externalUrl = System.getenv("AUDITORIUMS_TEST_DB_URL");
        if (externalUrl != null && !externalUrl.isBlank()) {
            registry.add("spring.datasource.url", () -> externalUrl);
            registry.add("spring.datasource.username", () -> System.getenv("AUDITORIUMS_TEST_DB_USERNAME"));
            registry.add("spring.datasource.password", () -> System.getenv("AUDITORIUMS_TEST_DB_PASSWORD"));
        } else {
            synchronized (PostgresTestSupport.class) {
                if (container == null) {
                    container = new PostgreSQLContainer(DockerImageName.parse("postgres:17"));
                    container.start();
                }
            }
            registry.add("spring.datasource.url", container::getJdbcUrl);
            registry.add("spring.datasource.username", container::getUsername);
            registry.add("spring.datasource.password", container::getPassword);
        }
    }
}
