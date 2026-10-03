package rut.iuct.auditoriums;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import rut.iuct.auditoriums.mapper.PhotoMapper;
import rut.iuct.auditoriums.model.AuditoriumPhoto;
import rut.iuct.auditoriums.service.PhotoService;
import rut.iuct.auditoriums.storage.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.config.import=", "spring.profiles.active=default",
        "app.storage.root=target/scenario-photos", "app.storage.cleanup-delay-ms=3600000",
        "spring.jpa.properties.hibernate.generate_statistics=true"})
@AutoConfigureMockMvc
class ScenarioTests extends PostgresTestSupport {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @Autowired PhotoService photos;
    @Autowired FileCleanupService cleanup;
    @Autowired PlatformTransactionManager transactions;
    @Autowired jakarta.persistence.EntityManagerFactory entityManagerFactory;
    @MockitoSpyBean LocalFileStorageService storage;
    @MockitoSpyBean PhotoMapper photoMapper;

    @BeforeEach
    void clearDisposableDatabase() throws Exception {
        jdbc.execute("TRUNCATE auditorium, department, additional_equipment, auditorium_department, file_cleanup_task RESTART IDENTITY CASCADE");
        Path root = Path.of("target/scenario-photos").toAbsolutePath();
        if (Files.exists(root)) {
            try (var files = Files.walk(root)) {
                for (Path file : files.filter(Files::isRegularFile).toList()) Files.delete(file);
            }
        }
    }

    private JsonNode postJson(String url, String body) throws Exception {
        return json.readTree(mvc.perform(post(url).contentType("application/json").content(body)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private JsonNode getJson(String url) throws Exception {
        return json.readTree(mvc.perform(get(java.net.URI.create(url))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private long room() throws Exception {
        return postJson("/api/auditoriums", "{\"name\":\"305а\",\"type\":\"CLASSROOM\",\"changedBy\":\"Иванов Иван\"}").get("id").asLong();
    }

    private long version(long id) throws Exception { return getJson("/api/auditoriums/" + id).get("version").asLong(); }

    private MockMultipartFile image() throws Exception {
        return new MockMultipartFile("file", "photo.PNG", "image/png", PhotoValidatorTests.image("png"));
    }

    private JsonNode upload(long id) throws Exception {
        return json.readTree(mvc.perform(multipart("/api/auditoriums/" + id + "/photos").file(image())
                .param("changedBy", "Петров Петр").param("auditoriumVersion", Long.toString(version(id))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    @Test
    void rendersAllPagesWithPopulatedEquipmentAndSafeReturnLinks() throws Exception {
        long department = postJson("/api/departments", "{\"name\":\"Кафедра\",\"changedBy\":\"Автор\"}").get("id").asLong();
        long type = postJson("/api/additional-equipment", "{\"name\":\"Колонки\",\"changedBy\":\"Автор\"}").get("id").asLong();
        long id = room();
        for (String resource : List.of("workstations", "projectors", "printers")) {
            postJson("/api/auditoriums/" + id + "/" + resource, "{\"auditoriumVersion\":" + version(id) + ",\"changedBy\":\"Автор\"}");
        }
        postJson("/api/auditoriums/" + id + "/additional-equipment", "{\"additionalEquipmentId\":" + type + ",\"auditoriumVersion\":" + version(id) + ",\"changedBy\":\"Автор\"}");
        upload(id);
        for (String url : List.of("/", "/auditoriums?q=305&size=20", "/auditoriums/new", "/auditoriums/" + id,
                "/auditoriums/" + id + "/edit", "/departments", "/departments/new", "/departments/" + department + "/edit",
                "/additional-equipment", "/additional-equipment/new", "/additional-equipment/" + type + "/edit", "/requests", "/statistics")) {
            mvc.perform(get(url)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/html"));
        }
        for (String url : List.of("/auditoriums/new", "/departments/new")) {
            String formHtml = mvc.perform(get(url)).andReturn().getResponse().getContentAsString();
            assertEquals(1, formHtml.split("name=\"changedBy\"", -1).length - 1, "One author field per form");
        }
        String html = mvc.perform(get("/auditoriums/" + id).param("returnTo", "/auditoriums?q=305&page=2&size=50"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("/auditoriums?q=305&amp;page=2&amp;size=50"));
        html = mvc.perform(get("/auditoriums/" + id).param("returnTo", "//evil.example")).andReturn().getResponse().getContentAsString();
        assertFalse(html.contains("evil.example"));
        mvc.perform(get("/auditoriums/999999")).andExpect(status().isNotFound()).andExpect(content().contentTypeCompatibleWith("text/html"));
        mvc.perform(get("/missing-page")).andExpect(status().isNotFound()).andExpect(content().contentTypeCompatibleWith("text/html"));
        mvc.perform(get("/api/missing-endpoint")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        mvc.perform(get("/auditoriums?size=19")).andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith("text/html"));
        mvc.perform(post("/auditoriums/new")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void validatesAuthorNumbersAndConflictsAndPreservesHiddenHistoricalFields() throws Exception {
        mvc.perform(post("/api/auditoriums").contentType("application/json").content("{\"name\":\" \",\"type\":\"CLASSROOM\",\"changedBy\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.name").exists()).andExpect(jsonPath("$.fieldErrors.changedBy").exists());
        long id = room();
        jdbc.update("UPDATE auditorium SET schedule_link='original', actual_schedule_link='other', has_projector=true, pc_count_manual=4 WHERE id=?", id);
        mvc.perform(put("/api/auditoriums/" + id).contentType("application/json").content("{\"name\":\"306\",\"type\":\"CLASSROOM\",\"version\":0,\"changedBy\":\"Новый автор\",\"capacityStudents\":0,\"capacityStudentsExtra\":null,\"hasAc\":null}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.capacityStudents").value(0)).andExpect(jsonPath("$.totalCapacityStudents").isEmpty());
        assertEquals("original", jdbc.queryForObject("SELECT schedule_link FROM auditorium WHERE id=?", String.class, id));
        assertEquals("other", jdbc.queryForObject("SELECT actual_schedule_link FROM auditorium WHERE id=?", String.class, id));
        assertEquals(4, jdbc.queryForObject("SELECT pc_count_manual FROM auditorium WHERE id=?", Integer.class, id));
        mvc.perform(put("/api/auditoriums/" + id).contentType("application/json").content("{\"name\":\"lost\",\"type\":\"CLASSROOM\",\"version\":0,\"changedBy\":\"Автор\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.fieldErrors.version").exists());
        mvc.perform(post("/api/auditoriums").contentType("application/json").content("{\"name\":\"bad\",\"type\":\"CLASSROOM\",\"changedBy\":\"Автор\",\"capacityStudents\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void equipmentWriteOffRestoreAndRareDeletionUpdateCountsAndAudit() throws Exception {
        long id = room();
        for (String resource : List.of("workstations", "projectors", "printers")) {
            JsonNode item = postJson("/api/auditoriums/" + id + "/" + resource, "{\"auditoriumVersion\":" + version(id) + ",\"changedBy\":\"Автор\"}");
            long equipmentId = item.get("id").asLong();
            mvc.perform(delete("/api/auditoriums/" + id).param("changedBy", "Автор").param("version", Long.toString(version(id))))
                    .andExpect(status().isConflict());
            String statusBody = "{\"version\":0,\"changedBy\":\"Списавший\"}";
            mvc.perform(patch("/api/" + resource + "/" + equipmentId + "/write-off").contentType("application/json").content(statusBody))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("WRITTEN_OFF"));
            long parentVersion = version(id);
            mvc.perform(patch("/api/" + resource + "/" + equipmentId + "/write-off").contentType("application/json").content(statusBody)).andExpect(status().isOk());
            assertEquals(parentVersion, version(id));
            assertEquals(0, getJson("/api/auditoriums/" + id + "/" + resource).size());
            assertEquals(1, getJson("/api/auditoriums/" + id + "/" + resource + "?includeWrittenOff=true").size());
            JsonNode card = getJson("/api/auditoriums/" + id);
            if (resource.equals("workstations")) assertEquals(0, card.get("activeWorkstationCount").asInt());
            if (resource.equals("projectors")) assertFalse(card.get("hasActiveProjector").asBoolean());
            if (resource.equals("printers")) assertFalse(card.get("hasActivePrinter").asBoolean());
            String field = resource.equals("workstations") ? "pcName" : "model";
            mvc.perform(put("/api/" + resource + "/" + equipmentId).contentType("application/json").content("{\"version\":1,\"changedBy\":\"Редактор\",\"" + field + "\":\"Обновлено\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("WRITTEN_OFF"))
                    .andExpect(jsonPath("$.writtenOffBy").value("Списавший"));
            mvc.perform(patch("/api/" + resource + "/" + equipmentId + "/restore").contentType("application/json").content("{\"version\":2,\"changedBy\":\"Восстановивший\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.writtenOffAt").isEmpty());
            assertEquals(1, getJson("/api/auditoriums/" + id + "/" + resource).size());
            long restoredVersion = version(id);
            mvc.perform(patch("/api/" + resource + "/" + equipmentId + "/restore").contentType("application/json").content("{\"version\":0,\"changedBy\":\"Автор\"}"))
                    .andExpect(status().isOk());
            assertEquals(restoredVersion, version(id));
            mvc.perform(delete("/api/" + resource + "/" + equipmentId).param("changedBy", "Автор").param("version", "3")).andExpect(status().isNoContent());
        }
        mvc.perform(delete("/api/auditoriums/" + id).param("changedBy", "Автор").param("version", Long.toString(version(id)))).andExpect(status().isNoContent());
    }

    @Test
    void additionalTypesArePreservedAndUsedDepartmentsCannotBeDeleted() throws Exception {
        long id = room();
        long type = postJson("/api/additional-equipment", "{\"name\":\"Колонки\",\"changedBy\":\"Автор\"}").get("id").asLong();
        long department = postJson("/api/departments", "{\"name\":\"Кафедра\",\"changedBy\":\"Автор\"}").get("id").asLong();
        jdbc.update("UPDATE auditorium SET department_id=? WHERE id=?", department, id);
        mvc.perform(delete("/api/departments/" + department).param("changedBy", "Автор").param("version", "0")).andExpect(status().isConflict());
        postJson("/api/auditoriums/" + id + "/additional-equipment", "{\"additionalEquipmentId\":" + type + ",\"auditoriumVersion\":" + version(id) + ",\"changedBy\":\"Автор\"}");
        mvc.perform(delete("/api/additional-equipment/" + type).param("changedBy", "Автор").param("version", "0")).andExpect(status().isConflict());
        mvc.perform(delete("/api/auditoriums/" + id + "/additional-equipment/" + type).param("changedBy", "Автор").param("version", Long.toString(version(id)))).andExpect(status().isNoContent());
        assertEquals("Колонки", getJson("/api/additional-equipment/" + type).get("name").asText());
        mvc.perform(delete("/api/auditoriums/" + id).param("changedBy", "Автор").param("version", Long.toString(version(id)))).andExpect(status().isNoContent());
        mvc.perform(delete("/api/departments/" + department).param("changedBy", "Автор").param("version", "0")).andExpect(status().isNoContent());
    }

    @Test
    void combinesAllFiltersHandlesNullsLiteralSearchAndPaginationWithoutNPlusOne() throws Exception {
        long department = postJson("/api/departments", "{\"name\":\"Кафедра\",\"changedBy\":\"Автор\"}").get("id").asLong();
        long id = postJson("/api/auditoriums", "{\"name\":\"Комната%_\",\"type\":\"COMPUTER_CLASSROOM\",\"building\":\"1\",\"floor\":-1,\"boardType\":\"MARKER\",\"departmentId\":" + department + ",\"internet\":true,\"hasAc\":false,\"changedBy\":\"Автор\"}").get("id").asLong();
        for (String resource : List.of("workstations", "projectors", "printers")) {
            postJson("/api/auditoriums/" + id + "/" + resource, "{\"auditoriumVersion\":" + version(id) + ",\"changedBy\":\"Автор\"}");
        }
        room();
        String query = "/api/auditoriums?q=%25_&type=COMPUTER_CLASSROOM&building=1&floor=-1&departmentId=" + department + "&boardType=MARKER&internet=true&hasAc=false&hasProjector=true&hasPrinter=true&hasWorkstations=true";
        assertEquals(1, getJson(query).get("totalElements").asInt());
        assertEquals(0, getJson(query.replace("internet=true", "internet=false")).get("totalElements").asInt());
        assertEquals(1, getJson("/api/auditoriums?hasAc=false").get("totalElements").asInt());
        assertEquals(1, getJson("/api/auditoriums?hasWorkstations=false").get("totalElements").asInt());
        assertEquals(0, getJson("/api/auditoriums?q=INVENTORY-ONLY").get("totalElements").asInt());
        for (int n = 0; n < 21; n++) jdbc.update("INSERT INTO auditorium(name,type,department_id) VALUES (?, 'CLASSROOM', ?)", "extra" + n, department);
        var statistics = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        statistics.clear();
        JsonNode firstPage = getJson("/api/auditoriums?size=20");
        assertEquals(20, firstPage.get("content").size());
        assertTrue(statistics.getPrepareStatementCount() <= 2, "Search should use one content query and one count query");
        assertEquals(3, getJson("/api/auditoriums?size=20&page=1").get("content").size());
        assertEquals(0, getJson("/api/auditoriums?page=50").get("content").size());
        mvc.perform(get("/api/auditoriums?size=19")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/auditoriums?page=-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/auditoriums?pcName=foo")).andExpect(status().isBadRequest());
    }

    @Test
    void photoUploadAndDeletionUpdateAuditAndDoNotExposePaths() throws Exception {
        long id = room();
        JsonNode photo = upload(id);
        assertFalse(photo.has("filePath"));
        assertEquals("Петров Петр", photo.get("changedBy").asText());
        assertEquals(1, version(id));
        mvc.perform(get(photo.get("url").asText())).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
        mvc.perform(delete("/api/photos/" + photo.get("id").asLong()).param("changedBy", "Автор").param("version", "8")).andExpect(status().isConflict());
        mvc.perform(delete("/api/photos/" + photo.get("id").asLong()).param("changedBy", "Удаливший").param("version", "0")).andExpect(status().isNoContent());
        assertEquals(2, version(id));
        assertEquals(0, getJson("/api/auditoriums/" + id + "/photos").size());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM file_cleanup_task", Integer.class));
        mvc.perform(multipart("/api/auditoriums/" + id + "/photos").file(new MockMultipartFile("file", "x.webp", "image/webp", new byte[]{1}))
                .param("changedBy", "Автор").param("auditoriumVersion", Long.toString(version(id)))).andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void databaseRollbackAndPartialWriteFailureCleanUpNewFiles() throws Exception {
        long id = room();
        doThrow(new IllegalStateException("Simulated failure after flush")).when(photoMapper).toResponse(any(AuditoriumPhoto.class));
        mvc.perform(multipart("/api/auditoriums/" + id + "/photos").file(image()).param("changedBy", "Автор").param("auditoriumVersion", "0"))
                .andExpect(status().isInternalServerError());
        assertEquals(0, version(id));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM auditorium_photo", Integer.class));
        try (var files = Files.list(Path.of("target/scenario-photos"))) { assertEquals(0, files.count()); }
        reset(photoMapper);
        doAnswer(invocation -> {
            invocation.callRealMethod();
            throw new StorageException("Simulated disk failure", new java.io.IOException());
        }).when(storage).write(anyString(), any(byte[].class));
        mvc.perform(multipart("/api/auditoriums/" + id + "/photos").file(image()).param("changedBy", "Автор").param("auditoriumVersion", "0"))
                .andExpect(status().isInternalServerError());
        try (var files = Files.list(Path.of("target/scenario-photos"))) { assertEquals(0, files.count()); }
    }

    @Test
    void deleteRollbackPreservesFileAndFailedDeleteIsDurablyRetried() throws Exception {
        long id = room();
        JsonNode photo = upload(id);
        String key = jdbc.queryForObject("SELECT file_path FROM auditorium_photo WHERE id=?", String.class, photo.get("id").asLong());
        new TransactionTemplate(transactions).executeWithoutResult(tx -> {
            photos.delete(photo.get("id").asLong(), "Автор", 0L);
            tx.setRollbackOnly();
        });
        assertNotNull(storage.read(key));
        assertEquals(1, getJson("/api/auditoriums/" + id + "/photos").size());
        doThrow(new StorageException("Simulated deletion failure", new java.io.IOException())).when(storage).delete(key);
        mvc.perform(delete("/api/photos/" + photo.get("id").asLong()).param("changedBy", "Автор").param("version", "0")).andExpect(status().isNoContent());
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM file_cleanup_task", Integer.class));
        assertNotNull(storage.read(key));
        doCallRealMethod().when(storage).delete(key);
        jdbc.update("UPDATE file_cleanup_task SET next_attempt_at=CURRENT_TIMESTAMP");
        cleanup.processPending();
        assertThrows(StorageException.class, () -> storage.read(key));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM file_cleanup_task", Integer.class));
    }

    @Test
    void simultaneousUploadsCommitOnePhotoAndCleanUpTheLosingTransaction() throws Exception {
        long id = room();
        var barrier = new CyclicBarrier(2);
        doAnswer(invocation -> {
            invocation.callRealMethod();
            barrier.await(15, TimeUnit.SECONDS);
            return null;
        }).when(storage).write(anyString(), any(byte[].class));
        try (var executor = Executors.newFixedThreadPool(2)) {
            var call = (java.util.concurrent.Callable<Integer>) () -> mvc.perform(multipart("/api/auditoriums/" + id + "/photos").file(image())
                    .param("changedBy", "Автор").param("auditoriumVersion", "0")).andReturn().getResponse().getStatus();
            var first = executor.submit(call);
            var second = executor.submit(call);
            List<Integer> statuses = List.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));
            assertTrue(statuses.contains(201));
            assertTrue(statuses.contains(409));
        }
        assertEquals(1, getJson("/api/auditoriums/" + id + "/photos").size());
        try (var files = Files.list(Path.of("target/scenario-photos"))) { assertEquals(1, files.count()); }
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM file_cleanup_task", Integer.class));
    }

    @Test
    void deletingEmptyAuditoriumRemovesPhotosButPreservesTypeCatalogue() throws Exception {
        long id = room();
        upload(id);
        long type = postJson("/api/additional-equipment", "{\"name\":\"Колонки\",\"changedBy\":\"Автор\"}").get("id").asLong();
        postJson("/api/auditoriums/" + id + "/additional-equipment", "{\"additionalEquipmentId\":" + type + ",\"auditoriumVersion\":" + version(id) + ",\"changedBy\":\"Автор\"}");
        mvc.perform(delete("/api/auditoriums/" + id).param("changedBy", "Автор").param("version", Long.toString(version(id)))).andExpect(status().isNoContent());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM auditorium_photo", Integer.class));
        assertEquals(1, getJson("/api/additional-equipment").size());
        try (var files = Files.list(Path.of("target/scenario-photos"))) { assertEquals(0, files.count()); }
    }

    @Test
    void legacyAbsoluteAliasKeepsSharedFileUntilLastPhotoIsDeleted() throws Exception {
        long id = room();
        JsonNode photo = upload(id);
        String key = jdbc.queryForObject("SELECT file_path FROM auditorium_photo WHERE id=?", String.class, photo.get("id").asLong());
        String absolute = Path.of("target/scenario-photos").resolve(key).toAbsolutePath().toString();
        long legacyId = jdbc.queryForObject("INSERT INTO auditorium_photo(auditorium_id,file_path,original_filename,content_type) VALUES (?,?,?,?) RETURNING id", Long.class,
                id, absolute, "legacy.png", "image/png");
        mvc.perform(delete("/api/photos/" + photo.get("id").asLong()).param("changedBy", "Автор").param("version", "0")).andExpect(status().isNoContent());
        mvc.perform(get("/api/photos/" + legacyId + "/content")).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
        mvc.perform(delete("/api/photos/" + legacyId).param("changedBy", "Автор").param("version", "0")).andExpect(status().isNoContent());
        assertThrows(StorageException.class, () -> storage.read(key));
    }
}
