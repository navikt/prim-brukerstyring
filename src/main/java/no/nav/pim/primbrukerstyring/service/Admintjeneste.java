package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import no.nav.pim.primbrukerstyring.domain.DriftOgVedlikehold;
import no.nav.pim.primbrukerstyring.repository.DriftOgVedlikeholdRepository;
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
@RequestMapping(value = "/admin")
public class Admintjeneste implements AdmintjenesteInterface {

    private static final Logger log = LoggerFactory.getLogger(Admintjeneste.class);

    @Autowired
    MeterRegistry metricsRegistry;

    @Autowired
    DriftOgVedlikeholdRepository driftogvedlikeholdrepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @GetMapping(path = "/driftOgVedlikehold")
    public DriftOgVedlikehold hentDriftOgVedlikehold(@RequestHeader(value = "Authorization") String authorization) {

        metricsRegistry.counter("tjenestekall", "tjeneste", "Admintjeneste", "metode", "hentDriftOgVedlikehold").increment();
        return driftogvedlikeholdrepository.getReferenceById(0L);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PutMapping(path = "/driftOgVedlikehold", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DriftOgVedlikehold settDriftOgVedlikehold(@RequestHeader(value = "Authorization") String authorization,
                                                     @Valid @RequestBody DriftOgVedlikehold driftOgVedlikehold) {

        metricsRegistry.counter("tjenestekall", "tjeneste", "Admintjeneste", "metode", "settDriftOgVedlikehold").increment();
        log.info("Oppdaterer drift og vedlikehold med vedlikeholdsmelding: {}: {}, og driftsmelding: {}, vedlikeholdsmodus er {}.", driftOgVedlikehold.getVedlikeholdOverskrift(), driftOgVedlikehold.getVedlikeholdMelding(), driftOgVedlikehold.getDriftsmelding(), driftOgVedlikehold.getVedlikeholdModus() ? "på" : "av");

        driftOgVedlikehold.setDriftOgVedlikeholdId(0L);
        return driftogvedlikeholdrepository.save(driftOgVedlikehold);
    }


}
