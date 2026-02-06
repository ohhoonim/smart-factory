package dev.ohhoonim;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class FactoryApplicationTests {

	@Test
	void contextLoads() {
		var modules = ApplicationModules.of(FactoryApplication.class);
		modules.forEach(System.out::print);
	}

}
