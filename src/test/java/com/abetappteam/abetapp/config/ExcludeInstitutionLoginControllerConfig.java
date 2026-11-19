package com.abetappteam.abetapp.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test-only workaround for duplicate GET /api/institution/health
 * (InstitutionController and InstitutionLoginController both inherit BaseController#health).
 */
@TestConfiguration
public class ExcludeInstitutionLoginControllerConfig {

    @Bean
    public static BeanFactoryPostProcessor excludeInstitutionLoginController() {
        return beanFactory -> {
            if (beanFactory instanceof BeanDefinitionRegistry registry
                    && registry.containsBeanDefinition("institutionLoginController")) {
                registry.removeBeanDefinition("institutionLoginController");
            }
        };
    }
}
