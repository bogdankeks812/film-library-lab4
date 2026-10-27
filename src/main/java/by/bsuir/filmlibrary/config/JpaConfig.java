package by.bsuir.filmlibrary.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Включает JPA-аудит: автоматическое заполнение полей
 * createdAt (@CreatedDate) и updatedAt (@LastModifiedDate).
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
