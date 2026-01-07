package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import no.nav.pim.primbrukerstyring.domain.*;
import no.nav.pim.primbrukerstyring.exceptions.ForbiddenException;
import no.nav.pim.primbrukerstyring.exceptions.NotFoundException;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;
import no.nav.pim.primbrukerstyring.repository.LederRepository;
import no.nav.pim.primbrukerstyring.repository.OverstyrendeLederRepository;
import no.nav.pim.primbrukerstyring.service.dto.OverstyrendeLederDto;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

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
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @GetMapping(path = "/info/{ident}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EnkelAnsattInfo hentEnkelAnsattInfo(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Ansatttjeneste", "metode", "hentEnkelAnsattInfo").increment();

        NomRessurs ressurs = nomGraphQLClient.getRessurs(authorization, ident);
        if (ressurs != null) {
            return EnkelAnsattInfo.fraNomRessurs(ressurs);
        } else {
            log.error("###Kunne ikke hente ansatt informasjon {} i NOM.", ident);
            throw new NotFoundException("Kunne ikke finne ansatt " + ident + " i NOM.");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @GetMapping(path = "/{ident}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Ansatt hentAnsatt(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Ansatttjeneste", "metode", "hentAnsatt").increment();

        Optional<OverstyrendeLeder> overstyrendeLeder = overstyrendelederrepository.findByAnsattIdentAndTilIsGreaterThanEqualOrTilIsNull(ident, LocalDate.now());
        NomRessurs ressurs = nomGraphQLClient.getRessurs(authorization, ident);
        if (ressurs != null) {
            AnsattStillingsavtale ansattStillingsavtale = null;
            if (overstyrendeLeder.isPresent()) {
                ansattStillingsavtale = AnsattStillingsavtale.fraOverstyrendeLeder(overstyrendeLeder.get());
            }
            return Ansatt.fraNomRessurs(ressurs, ansattStillingsavtale);
        } else {
            log.error("###Kunne ikke hente ansatt {} i NOM.", ident);
            throw new NotFoundException("Kunne ikke finne ansatt " + ident + " i NOM.");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PostMapping(path = "/overstyrendeleder", consumes = MediaType.APPLICATION_JSON_VALUE)
    public OverstyrendeLeder leggTilOverstyrendeLeder(@RequestHeader(value = "Authorization") String authorization, @Valid @RequestBody OverstyrendeLederDto overstyrendeLederDto) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Ansatttjeneste", "metode", "leggTilOverstyrendeLeder").increment();
        if (nonNull(overstyrendeLederDto.getOverstyringTom()) && overstyrendeLederDto.getOverstyringTom().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Til dato kan ikke være før dagens dato");
        }
        Optional<Leder> finnesLeder = lederrepository.findByIdent(overstyrendeLederDto.getLederIdent());
        Optional<OverstyrendeLeder> finnesOverstyrendeLeder = overstyrendelederrepository.findByAnsattIdentAndTilIsGreaterThanEqualOrTilIsNull(overstyrendeLederDto.getAnsattIdent(), LocalDate.now());
        Leder leder;
        if (finnesLeder.isPresent()) {
            leder = finnesLeder.get();
        } else {
            NomRessurs ressurs = nomGraphQLClient.getRessurs(authorization, overstyrendeLederDto.getLederIdent());
            if (ressurs != null) {
                leder = lederrepository.save(Leder.fraNomRessurs(ressurs));
            } else {
                log.error("###Kunne ikke finne leder '{}' i NOM.", overstyrendeLederDto.getLederIdent());
                throw new NotFoundException("Kunne ikke finne leder " + overstyrendeLederDto.getLederIdent() + " i NOM.");
            }
        }
        if (finnesOverstyrendeLeder.isPresent()) {
            log.error("###Ansatt {} har allerede en overstyrende leder i PRIM.", overstyrendeLederDto.getAnsattIdent());
            throw new ForbiddenException("Ansatt " + overstyrendeLederDto.getAnsattIdent() + " har allerede en overstyrende leder i PRIM.");
        } else {
            NomRessurs ressurs = nomGraphQLClient.getRessurs(authorization, overstyrendeLederDto.getAnsattIdent());
            if (ressurs != null) {
                return overstyrendelederrepository.save(
                        OverstyrendeLeder.builder()
                                .ansattIdent(ressurs.getNavident())
                                .ansattNavn(ressurs.getVisningsnavn())
                                .overstyrendeLeder(leder)
                                .fra(overstyrendeLederDto.getOverstyringFom())
                                .til(overstyrendeLederDto.getOverstyringTom())
                                .build());
            } else {
                log.error("###Kunne ikke finne ansatt '{}' i NOM.", overstyrendeLederDto.getAnsattIdent());
                throw new NotFoundException("Kunne ikke finne ansatt " + overstyrendeLederDto.getAnsattIdent() + " i NOM.");
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @DeleteMapping(path = "/overstyrendeleder/{ansattIdent}")
    public OverstyrendeLeder fjernOverstyrendeLeder(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ansattIdent) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Ansatttjeneste", "metode", "fjernOverstyrendeLeder").increment();
        Optional<OverstyrendeLeder> finnesOverstyrendeLeder = overstyrendelederrepository.findByAnsattIdentAndTilIsGreaterThanEqualOrTilIsNull(ansattIdent, LocalDate.now());
        if (finnesOverstyrendeLeder.isPresent()) {
            OverstyrendeLeder overstyrendeLeder = finnesOverstyrendeLeder.get();
            overstyrendeLeder.setTil(LocalDate.now().minusDays(1));
            return overstyrendelederrepository.save(overstyrendeLeder);
        } else {
            log.error("###Ansatt {} har ikke en overstyrende leder i PRIM.", ansattIdent);
            throw new NotFoundException("Ansatt " + ansattIdent + " har ikke en overstyrende leder i PRIM.");
        }
    }

    @Override
    @GetMapping(path = "/overstyrendeledere")
    public List<OverstyrendeLeder> hentAlleOverstyrendeLedere(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Ansatttjeneste", "metode", "hentAlleOverstyrendeLedere").increment();
        return overstyrendelederrepository.findAllByTilIsGreaterThanEqualOrTilIsNull(LocalDate.now(), Sort.by(Sort.Direction.DESC, "til"));
    }

    @Override
    @GetMapping(path = "/inaktiveOverstyrendeledere")
    public List<OverstyrendeLeder> hentAlleInaktiveOverstyrendeLedere(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Ansatttjeneste", "metode", "hentAlleInaktiveOverstyrendeLedere").increment();

        return overstyrendelederrepository.findAllByTilIsBefore(LocalDate.now(), Sort.by(Sort.Direction.DESC, "til"));
    }
}
