package no.nav.pim.primbrukerstyring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;


@SpringBootApplication
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = ASSIGNABLE_TYPE, value = {PrimBrukerstyringApplication.class})})
public class PrimBrukerstyringApplicationLocal {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(PrimBrukerstyringApplicationLocal.class);
		app.setAdditionalProfiles("dev");
		app.run(args);
	}
}
