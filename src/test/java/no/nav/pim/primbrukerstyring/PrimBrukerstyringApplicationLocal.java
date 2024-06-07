package no.nav.pim.primbrukerstyring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;


@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableWebMvc
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = {PrimBrukerstyringApplication.class})})
public class PrimBrukerstyringApplicationLocal {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(PrimBrukerstyringApplicationLocal.class);
		app.setAdditionalProfiles("dev");
		app.run(args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.build();
	}
}
