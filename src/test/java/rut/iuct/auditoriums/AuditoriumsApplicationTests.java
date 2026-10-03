package rut.iuct.auditoriums;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.config.import=", "spring.profiles.active=default", "app.storage.root=target/test-photos"})
class AuditoriumsApplicationTests extends PostgresTestSupport {

	@Test
	void contextLoads() {
	}

}
