package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import no.nav.pim.primbrukerstyring.domain.*;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;
import no.nav.pim.primbrukerstyring.repository.LederRepository;
import no.nav.pim.primbrukerstyring.repository.OverstyrendeLederRepository;
import no.nav.pim.primbrukerstyring.service.dto.OverstyrendeLederDto;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@Protected
@RequestMapping(value = "/ansatt")
public class Ansatttjeneste implements AnsatttjenesteInterface{

    private static final Logger log = LoggerFactory.getLogger(Ansatttjeneste.class);

    @Autowired
    MeterRegistry metricsRegistry;

    @Autowired
    NomGraphQLClient nomGraphQLClient;

    @Autowired
    LederRepository lederrepository;

    @Autowired
    OverstyrendeLederRepository overstyrendelederrepository;

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

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PostMapping(path = "/overstyrendeleder", consumes = MediaType.APPLICATION_JSON_VALUE)
    public OverstyrendeLeder leggTilOverstyrendeLeder(@RequestHeader(value = "Authorization") String authorization, @Valid @RequestBody OverstyrendeLederDto overstyrendeLederDto) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Ansatttjeneste", "metode", "leggTilOverstyrendeLeder").increment();
        Optional<Leder> finnesLeder = lederrepository.findByIdent(overstyrendeLederDto.getLederIdent());
        Optional<OverstyrendeLeder> finnesOverstyrendeLeder = overstyrendelederrepository.findByAnsattIdentAndTil(overstyrendeLederDto.getAnsattIdent(), null);
        Leder leder;
        if (finnesLeder.isPresent()) {
            leder = finnesLeder.get();
        } else {
            NomRessurs ressurs = nomGraphQLClient.getRessurs(authorization, overstyrendeLederDto.getLederIdent());
            if (ressurs != null) {
                leder = lederrepository.save(Leder.fraNomRessurs(ressurs));
            } else {
                log.error("###Kunne ikke finne leder i NOM: {}", overstyrendeLederDto.getLederIdent());
                return null;
            }
        }
        if (finnesOverstyrendeLeder.isPresent()) {
            log.error("###Ansatt {} har allerede en overstyrende leder i PRIM.", overstyrendeLederDto.getAnsattIdent());
            return null;
        } else {
            NomRessurs ressurs = nomGraphQLClient.getRessurs(authorization, overstyrendeLederDto.getAnsattIdent());
            if (ressurs != null) {
                return overstyrendelederrepository.save(OverstyrendeLeder.builder().ansattIdent(ressurs.getNavident()).ansattNavn(ressurs.getVisningsnavn()).overstyrendeLeder(leder).fra(new Date()).build());
            } else {
                log.error("###Kunne ikke finne ansatt i NOM: {}", overstyrendeLederDto.getAnsattIdent());
                return null;
            }
        }
    }

    @Override
    @GetMapping(path = "/overstyrendeledere")
    public List<OverstyrendeLeder> hentAlleOverstyrendeLedere(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Ansatttjeneste", "metode", "hentAlleOverstyrendeLedere").increment();

        return overstyrendelederrepository.findAll();
    }
}
