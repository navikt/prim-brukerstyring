package no.nav.pim.primbrukerstyring;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
@EnableJwtTokenValidation(ignore = {"org.springdoc", "org.springframework"})
public class PrimBrukerstyringApplication {
	static void main(String[] args) {
		SpringApplication.run(PrimBrukerstyringApplication.class, args);
	}
}
