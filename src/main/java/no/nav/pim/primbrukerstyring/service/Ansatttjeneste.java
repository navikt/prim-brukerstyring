package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.pim.primbrukerstyring.domain.Ansatt;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@Protected
@RequestMapping(value = "/ansatt")
public class Ansatttjeneste implements AnsatttjenesteInterface{

    private static final Logger log = LoggerFactory.getLogger(Ansatttjeneste.class);

    @Autowired
    MeterRegistry metricsRegistry;

    @Autowired
    NomGraphQLClient nomGraphQLClient;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @GetMapping(path = "/{ident}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Ansatt hentAnsatt(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Ansatttjeneste", "metode", "hentAnsatt").increment();

        NomRessurs ressurs = nomGraphQLClient.getRessurs(authorization, ident);
        if (ressurs != null) {
            return Ansatt.fraNomRessurs(ressurs);
        } else {
            log.error("###Kunne ikke hente ansatt i NOM: {}", ident);
            return null;
        }
    }
}
