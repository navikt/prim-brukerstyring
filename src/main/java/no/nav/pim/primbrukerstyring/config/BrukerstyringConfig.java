package no.nav.pim.primbrukerstyring.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestClient;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class BrukerstyringConfig {

        @Bean
        public RestClient restClient() {
            return RestClient.builder().build();
        }

        @Bean
        public LockProvider lockProvider(DataSource dataSource) {
            return new JdbcTemplateLockProvider(dataSource);
        }

}
