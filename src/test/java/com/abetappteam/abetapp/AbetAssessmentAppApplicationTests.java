package com.abetappteam.abetapp;

import com.abetappteam.abetapp.config.ExcludeInstitutionLoginControllerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(ExcludeInstitutionLoginControllerConfig.class)
class AbetAssessmentAppApplicationTests {

	@Test
	void contextLoads() {
	}

}
