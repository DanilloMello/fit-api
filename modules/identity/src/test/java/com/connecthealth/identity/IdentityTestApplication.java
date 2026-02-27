package com.connecthealth.identity;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Minimal Spring Boot application used as the @SpringBootConfiguration
 * anchor for @WebMvcTest slices in the identity module.
 */
@SpringBootApplication(
        scanBasePackages = "com.connecthealth.identity",
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class}
)
class IdentityTestApplication {
}
