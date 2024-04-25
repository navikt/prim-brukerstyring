package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import no.nav.pim.primbrukerstyring.domain.*;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;
import no.nav.pim.primbrukerstyring.repository.AnsattStillingsavtalerepository;
import no.nav.pim.primbrukerstyring.repository.Ansattrepository;
import no.nav.pim.primbrukerstyring.repository.Lederrepository;
import no.nav.pim.primbrukerstyring.service.dto.OverstyrendeLederDto;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
    Lederrepository lederrepository;

    @Autowired
    Ansattrepository ansattrepository;

    @Autowired
    AnsattStillingsavtalerepository ansattstillingsavtalerepository;

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
    public AnsattStillingsavtale leggTilOverstyrendeLeder(@RequestHeader(value = "Authorization") String authorization, @Valid @RequestBody OverstyrendeLederDto overstyrendeLederDto) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Ansatttjeneste", "metode", "leggTilOverstyrendeLeder").increment();
        Optional<Leder> finnesLeder = lederrepository.findByIdent(overstyrendeLederDto.getLederIdent());
        Optional<Ansatt> finnesAnsatt = ansattrepository.findByIdent(overstyrendeLederDto.getAnsattIdent());
        Leder leder;
        Ansatt ansatt;

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
        if (finnesAnsatt.isPresent()) {
            ansatt = finnesAnsatt.get();
        } else {
            NomRessurs ressurs = nomGraphQLClient.getRessurs(authorization, overstyrendeLederDto.getAnsattIdent());
            if (ressurs != null) {
                ansatt = ansattrepository.save(Ansatt.fraNomRessurs(ressurs));
            } else {
                log.error("###Kunne ikke finne ansatt i NOM: {}", overstyrendeLederDto.getAnsattIdent());
                return null;
            }
        }

        AnsattStillingsavtale avtale = AnsattStillingsavtale.builder().leder(leder).ansatt(ansatt).stillingsavtale(Stillingsavtale.DR).ansattType(AnsattType.F).build();
        return ansattstillingsavtalerepository.save(avtale);
    }

    @Override
    @GetMapping(path = "/overstyrendeledere")
    public List<AnsattStillingsavtale> hentAlleOverstyrendeLedere(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Ansatttjeneste", "metode", "hentAlleOverstyrendeLedere").increment();

        return ansattstillingsavtalerepository.findAll();
    }
}
