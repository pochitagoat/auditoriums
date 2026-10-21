package rut.iuct.auditoriums;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.config.import=", "spring.profiles.active=default", "app.storage.root=target/test-photos"})
class MigrationTests extends PostgresTestSupport {
    @Autowired DataSource dataSource;

    @Test
    void developmentSeedPopulatesOnlyAnEmptySchemaWithRepresentativeData() {
        String schema = "seed_test_" + UUID.randomUUID().toString().replace("-", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        try {
            var flyway = Flyway.configure().dataSource(dataSource).schemas(schema).defaultSchema(schema)
                    .locations("classpath:db/migration", "classpath:db/seed").load();

            assertEquals(3, flyway.migrate().migrationsExecuted);
            assertEquals(27, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".auditorium", Integer.class));
            assertEquals(7, jdbc.queryForObject("SELECT count(DISTINCT type) FROM " + schema + ".auditorium", Integer.class));
            assertEquals(5, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".department", Integer.class));
            assertEquals(7, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".additional_equipment", Integer.class));
            assertEquals(56, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".workstation", Integer.class));
            assertEquals(14, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".projector", Integer.class));
            assertEquals(10, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".printer", Integer.class));
            int writtenOff = jdbc.queryForObject(
                    "SELECT count(*) FROM " + schema + ".workstation WHERE status = 'WRITTEN_OFF'", Integer.class)
                    + jdbc.queryForObject(
                    "SELECT count(*) FROM " + schema + ".projector WHERE status = 'WRITTEN_OFF'", Integer.class)
                    + jdbc.queryForObject(
                    "SELECT count(*) FROM " + schema + ".printer WHERE status = 'WRITTEN_OFF'", Integer.class);
            assertEquals(7, writtenOff);

            flyway.migrate();
            assertEquals(27, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".auditorium", Integer.class));
            assertEquals(56, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".workstation", Integer.class));
        } finally { jdbc.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE"); }
    }

    @Test
    void upgradesPopulatedV1WithoutLosingHistoryTypesOrPhotoPathsAndRunsOnlyOnce() {
        String schema = "migration_test_" + UUID.randomUUID().toString().replace("-", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        try {
            Flyway.configure().dataSource(dataSource).schemas(schema).defaultSchema(schema)
                    .locations("classpath:db/migration").target("1").load().migrate();
            jdbc.execute("INSERT INTO " + schema + ".auditorium(id,name,type,schedule_link,has_projector,pc_count_manual) VALUES (1,'old','CLASSROOM','preserve',true,4)");
            jdbc.execute("INSERT INTO " + schema + ".department(id,name) VALUES (1,'old department')");
            jdbc.execute("INSERT INTO " + schema + ".auditorium_department VALUES (1,1)");
            jdbc.execute("INSERT INTO " + schema + ".additional_equipment(id,name) VALUES (1,'old type'),(2,'unused')");
            jdbc.execute("INSERT INTO " + schema + ".auditorium_additional_equipment VALUES (1,1)");
            jdbc.execute("INSERT INTO " + schema + ".workstation(auditorium_id,pc_name) VALUES (1,'old-inventory')");
            jdbc.execute("INSERT INTO " + schema + ".auditorium_photo(auditorium_id,file_path,original_filename) VALUES (1,'legacy/photo.png','photo.png')");
            var flyway = Flyway.configure().dataSource(dataSource).schemas(schema).defaultSchema(schema).locations("classpath:db/migration").load();
            assertEquals(2, flyway.migrate().migrationsExecuted);
            assertEquals(0, flyway.migrate().migrationsExecuted);
            assertEquals(1L, jdbc.queryForObject("SELECT department_id FROM " + schema + ".auditorium", Long.class));
            assertEquals("preserve", jdbc.queryForObject("SELECT schedule_link FROM " + schema + ".auditorium", String.class));
            assertEquals("legacy/photo.png", jdbc.queryForObject("SELECT file_path FROM " + schema + ".auditorium_photo", String.class));
            assertEquals("ACTIVE", jdbc.queryForObject("SELECT status FROM " + schema + ".workstation", String.class));
            assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".additional_equipment", Integer.class));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".auditorium_department", Integer.class));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".auditorium_additional_equipment", Integer.class));
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".file_cleanup_task", Integer.class));
        } finally { jdbc.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE"); }
    }

    @Test
    void conflictingLegacyLinksStopMigrationAndKeepAllSourceData() {
        String schema = "migration_test_" + UUID.randomUUID().toString().replace("-", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        try {
            Flyway.configure().dataSource(dataSource).schemas(schema).defaultSchema(schema).locations("classpath:db/migration").target("1").load().migrate();
            jdbc.execute("INSERT INTO " + schema + ".auditorium(id,name,type) VALUES (1,'old','CLASSROOM')");
            jdbc.execute("INSERT INTO " + schema + ".department(id,name) VALUES (1,'first'),(2,'second')");
            jdbc.execute("INSERT INTO " + schema + ".auditorium_department VALUES (1,1),(1,2)");
            assertThrows(org.flywaydb.core.api.FlywayException.class, () -> Flyway.configure().dataSource(dataSource)
                    .schemas(schema).defaultSchema(schema).locations("classpath:db/migration").load().migrate());
            assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".auditorium_department", Integer.class));
            assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM " + schema + ".auditorium", Integer.class));
        } finally { jdbc.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE"); }
    }
}
