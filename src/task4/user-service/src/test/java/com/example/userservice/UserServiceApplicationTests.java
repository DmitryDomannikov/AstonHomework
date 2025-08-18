package com.example.userservice;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
@Slf4j
class UserServiceApplicationTests {
	@Test
	void contextLoads() {
		log.debug("Контекст Spring успешно загружен");
	}
}
