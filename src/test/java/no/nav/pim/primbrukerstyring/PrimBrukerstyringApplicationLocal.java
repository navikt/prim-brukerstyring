package no.nav.pim.primbrukerstyring;

import no.nav.pim.primbrukerstyring.config.BrukerstyringConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;


@SpringBootApplication
@EnableWebMvc
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = {PrimBrukerstyringApplication.class, BrukerstyringConfig.class})})
public class PrimBrukerstyringApplicationLocal {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(PrimBrukerstyringApplicationLocal.class);
		app.setAdditionalProfiles("dev");
		app.run(args);
	}
}
