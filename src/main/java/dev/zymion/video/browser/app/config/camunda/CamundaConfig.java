package dev.zymion.video.browser.app.config.camunda;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;

@Configuration
public class CamundaConfig {

    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration(
            @Qualifier("camundaDataSource") DataSource camundaDataSource,
            PlatformTransactionManager transactionManager) {

        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
        config.setDataSource(camundaDataSource);
        config.setTransactionManager(transactionManager);

        config.setDatabaseSchema("camunda");
        config.setDatabaseTablePrefix("camunda.");
        config.setDatabaseSchemaUpdate(SpringProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        return config;
    }
}




