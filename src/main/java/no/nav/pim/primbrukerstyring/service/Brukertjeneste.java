package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import jakarta.validation.Valid;
import no.nav.pim.primbrukerstyring.domain.*;
import no.nav.pim.primbrukerstyring.exceptions.AuthorizationException;
import no.nav.pim.primbrukerstyring.exceptions.NotFoundException;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.*;
import no.nav.pim.primbrukerstyring.repository.BrukerRepository;
import no.nav.pim.primbrukerstyring.repository.LederRepository;
import no.nav.pim.primbrukerstyring.repository.OverstyrendeLederRepository;
import no.nav.pim.primbrukerstyring.service.dto.BrukerDto;
import no.nav.pim.primbrukerstyring.service.dto.BrukerRolleTilgangerDto;
import no.nav.pim.primbrukerstyring.service.dto.LederDto;
import no.nav.pim.primbrukerstyring.util.OIDCUtil;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

@RestController
@Protected
@RequestMapping(value = "/bruker")
public class Brukertjeneste implements BrukertjenesteInterface {

    private static final Logger log = LoggerFactory.getLogger(Brukertjeneste.class);

    @Autowired
    MeterRegistry metricsRegistry;

    @Autowired
    Ansatttjeneste ansatttjeneste;

    @Autowired
    BrukerRepository brukerrepository;

    @Autowired
    LederRepository lederrepository;

    @Autowired
    OverstyrendeLederRepository overstyrendelederrepository;

    @Autowired
    OIDCUtil oidcUtil;

    @Autowired
    NomGraphQLClient nomGraphQLClient;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @GetMapping()
    public BrukerDto hentBruker(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentBruker").increment();
        String brukerIdent = oidcUtil.finnClaimFraOIDCToken(authorization, "NAVident").orElseThrow(() -> new AuthorizationException("Ikke gyldig OIDC-token"));
        Optional<Bruker> bruker = brukerrepository.findByIdent(brukerIdent);

        if (bruker.isEmpty()) {
            NomRessurs ressurs = nomGraphQLClient.getLedersResurser(authorization, brukerIdent);
            if (ressurs != null) {
                List<OverstyrendeLeder> overstyrteAnsatte = overstyrendelederrepository.findByOverstyrendeLeder_IdentAndTilIsNull(brukerIdent);
                if (!ressurs.getLederFor().isEmpty() || !overstyrteAnsatte.isEmpty()) {
                    Optional<Leder> finnesLeder = lederrepository.findByIdent(ressurs.getNavident());
                    Leder leder = finnesLeder.orElseGet(() -> Leder.fraNomRessurs(ressurs));
                    brukerrepository.save(Bruker.builder().ident(brukerIdent).navn(ressurs.getVisningsnavn()).sistAksessert(new Date()).ledere(Set.of(leder)).rolle(Rolle.LEDER).build());
                    return new BrukerDto(Rolle.LEDER, LederDto.fraLeder(leder));
                } else {
                    return new BrukerDto(Rolle.MEDARBEIDER, null);
                }
            }
            log.error("###Kunne ikke hente bruker i NOM: {}", brukerIdent);
            return new BrukerDto(Rolle.UKJENT, null);
        } else {
            if (bruker.get().getRolle().equals(Rolle.LEDER)) {
                Bruker oppdatertBruker = bruker.get();
                if (oppdatertBruker.getSistAksessert() == null || oppdatertBruker.getSistAksessert().toInstant()
                        .isBefore(Instant.now().atZone(ZoneId.of("Europe/Paris")).minusHours(1).toInstant())) {
                    NomRessurs ressurs = nomGraphQLClient.getLedersResurser(authorization, brukerIdent);
                    oppdatertBruker.getLedere().stream()
                            .filter(leder -> leder.getIdent().equals(ressurs.getNavident()))
                            .forEach(leder -> leder.oppdaterMed(Leder.fraNomRessurs(ressurs)));
                    oppdatertBruker.setNavn(ressurs.getVisningsnavn());
                    oppdatertBruker.setSistAksessert(new Date());
                    brukerrepository.save(oppdatertBruker);
                }
                Optional<Leder> representertLeder = oppdatertBruker.getLedere().stream()
                        .filter(leder -> leder.getIdent().equals(brukerIdent))
                        .findFirst();
                if (representertLeder.isPresent()) {
                    return new BrukerDto(Rolle.LEDER, LederDto.fraLeder(representertLeder.get()));
                }
                log.error("###Kunne ikke finne leder på bruker: {}", brukerIdent);
                return new BrukerDto(Rolle.LEDER, null);
            }
            return new BrukerDto(bruker.get().getRolle(), null);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PostMapping(path = "/rolle", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Bruker leggTilBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @Valid @RequestBody BrukerRolleTilgangerDto brukerDto) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "leggTilBrukerRolle").increment();
        Bruker bruker = brukerrepository.findByIdent(brukerDto.getIdent()).orElse(new Bruker());
        bruker.setIdent(brukerDto.getIdent());
        bruker.setRolle(brukerDto.getRolle());
        bruker.setSistEndret(new Date());
        Set<Leder> ledere = hentLedere(authorization, bruker);
        bruker.setLedere(ledere);

        NomRessurs ressurs = nomGraphQLClient.getRessurs(authorization, brukerDto.getIdent());
        if (ressurs != null) {
            bruker.setNavn(ressurs.getVisningsnavn());
            bruker.setSluttet(ressurs.getSluttdato() != null && ressurs.getSluttdato().before(new Date()));
            ressurs.getOrgTilknytninger().stream()
                    .filter(orgTilknytning -> orgTilknytning.getErDagligOppfolging() && (orgTilknytning.getGyldigTom() == null || orgTilknytning.getGyldigTom().after(new Date())))
                    .findFirst()
                    .ifPresent(orgTilknytning -> bruker.setEnhet(orgTilknytning.getOrgEnhet().getId()));
        }
        return brukerrepository.save(bruker);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PutMapping(path = "/rolle/{ident}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Bruker endreBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident, @Valid @RequestBody BrukerRolleTilgangerDto bruker) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "endreBruker").increment();
        Optional<Bruker> eksisterendeBruker = brukerrepository.findByIdent(ident);
        if (eksisterendeBruker.isPresent()) {
            Bruker oppdatertBruker = eksisterendeBruker.get();
            oppdatertBruker.setRolle(bruker.getRolle());
            oppdatertBruker.setSistEndret(new Date());
            oppdatertBruker.setEndretEnhet(false);
            return brukerrepository.save(oppdatertBruker);
        } else {
            Metrics.counter("prim_error", "exception", "UserDoesntExistException").increment();
            throw new RuntimeException("Bruker med ident " + ident + " eksisterer ikke i PRIM");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PutMapping(path = "/tilganger/{ident}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Bruker endreBrukerTilganger(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident, @Valid @RequestBody BrukerRolleTilgangerDto bruker) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "endreBrukerTilganger").increment();
        Optional<Bruker> eksisterendeBruker = brukerrepository.findByIdent(ident);
        if (eksisterendeBruker.isPresent()) {
            Bruker oppdatertBruker = eksisterendeBruker.get();
            oppdatertBruker.setTilganger(bruker.getTilganger());
            oppdatertBruker.setUkjentTilgang(
                oppdatertBruker.getUkjentTilgang().stream().filter((id) -> bruker.getTilganger().contains(id)).toList()
            );
            Set<Leder> ledere = hentLedere(authorization, oppdatertBruker);
            oppdatertBruker.setLedere(ledere);
            oppdatertBruker.setSistEndret(new Date());
            oppdatertBruker.setEndretEnhet(false);
            return brukerrepository.save(oppdatertBruker);
        } else {
            Metrics.counter("prim_error", "exception", "UserDoesntExistException").increment();
            throw new RuntimeException("Bruker med ident " + ident + " eksisterer ikke i PRIM");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @DeleteMapping(path = "/rolle/{ident}")
    public void slettBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "slettBrukerRolle").increment();
        brukerrepository.deleteByIdent(ident);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @GetMapping(path = "/hr")
    public List<Bruker> hentAlleHRMedarbeidere(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentAlleHRMedarbeidere").increment();
        return brukerrepository.findAllByRolleIn(List.of(Rolle.HR_MEDARBEIDER, Rolle.HR_MEDARBEIDER_BEMANNING));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @GetMapping(path = "/ledere")
    public List<LederDto> hentLedere(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentLedere").increment();
        String brukerIdent = oidcUtil.finnClaimFraOIDCToken(authorization, "NAVident").orElseThrow(() -> new AuthorizationException("Ikke gyldig OIDC-token"));
        Optional<Bruker> bruker = brukerrepository.findByIdent(brukerIdent);
        boolean erHR = bruker.isPresent() && List.of(Rolle.HR_MEDARBEIDER, Rolle.HR_MEDARBEIDER_BEMANNING).contains(bruker.get().getRolle());
        if (erHR) {
            Bruker hrBruker = bruker.get();
            if (hrBruker.getLedere().isEmpty() || hrBruker.getSistAksessert() == null || hrBruker.getSistAksessert().toInstant()
                    .isBefore(Instant.now().atZone(ZoneId.of("Europe/Paris")).minusHours(4).toInstant())) {
                Set<Leder> ledere = hentLedere(authorization, hrBruker);
                hrBruker.setLedere(ledere);
                hrBruker.setSistAksessert(new Date());
                brukerrepository.save(hrBruker);

                return ledere.stream().map(LederDto::fraLeder).sorted().toList();
            } else {
                return hrBruker.getLedere().stream().map(LederDto::fraLeder).sorted().toList();
            }
        } else {
            throw new AuthorizationException("Bruker med ident "+ brukerIdent + " er ikke HR ansatt");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @GetMapping(path = "/leder/{lederIdent}/ressurser")
    public List<Ansatt> hentLedersRessurser(@RequestHeader(value = "Authorization") String authorization, @PathVariable String lederIdent) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentLedersRessurser").increment();
        Leder validertLeder = validerLeder(authorization, lederIdent);
        if (validertLeder != null) {
            NomRessurs ledersRessurser = nomGraphQLClient.getLedersResurser(authorization, lederIdent);
            log.info("Lederressurs TEST: {}", ledersRessurser);
            List<Ansatt> ansatte = ledersRessurser.getLederFor().stream()
                    .flatMap((lederFor) -> {
                        Stream<NomRessurs> orgTilknytninger = lederFor.getOrgEnhet().getOrgTilknytninger().stream()
                                .filter(ot -> ot.getGyldigTom() == null || ot.getGyldigTom().after(new Date()))
                                .map((NomKobling::getRessurs));
                        Stream<NomRessurs> organiseringer = lederFor.getOrgEnhet().getOrganiseringer().stream()
                                .flatMap(org -> org.getOrgEnhet().getLedere().stream()
                                        .filter(leder -> leder.getGyldigTom() == null || leder.getGyldigTom().after(new Date()))
                                        .map(NomLeder::getRessurs));
                        return Stream.concat(orgTilknytninger, organiseringer);
                    })
                    .filter(ressurs -> ressurs.getIdentType() != NomIdentType.ROBOT
                                       && !ressurs.getNavident().equals(lederIdent)
                                       && ressurs.getLedere().stream()
                                               .filter(leder -> leder.getGyldigTom() == null || leder.getGyldigTom().after(new Date()))
                                               .anyMatch(leder -> leder.getRessurs().getNavident().equals(lederIdent))
                                       && ressurs.getOrgTilknytninger().stream().anyMatch(ot -> ot.getGyldigTom() == null || ot.getGyldigTom().after(new Date())
                            )
                    )
                    .distinct().map(ressurs -> {
                        Optional<OverstyrendeLeder> overstyrendeLeder = overstyrendelederrepository.findByAnsattIdentAndTilIsGreaterThanEqualOrTilIsNull(ressurs.getNavident(), LocalDate.now());
                        AnsattStillingsavtale ansattStillingsavtale = null;
                        if (overstyrendeLeder.isPresent()) {
                            ansattStillingsavtale = AnsattStillingsavtale.fraOverstyrendeLeder(overstyrendeLeder.get());
                        }

                        return Ansatt.fraNomRessurs(ressurs, ansattStillingsavtale);
                    }).toList();
            Stream<Ansatt> overstyrteAnsatte = overstyrendelederrepository.findByOverstyrendeLeder_IdentAndTilIsNull(lederIdent).stream()
                    .filter(overstyrtLeder -> ansatte.stream().noneMatch(ansatt -> ansatt.getIdent().equals(overstyrtLeder.getAnsattIdent())))
                    .map(overstyrtLeder -> ansatttjeneste.hentAnsatt(authorization, overstyrtLeder.getAnsattIdent()));
            return Stream.concat(ansatte.stream(), overstyrteAnsatte).toList();
        } else {
            throw new NotFoundException("Leder med ident " + lederIdent + " finnes ikke i PRIM.");
        }
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @GetMapping(path = "/leder/{lederIdent}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Leder validerLeder(@RequestHeader(value = "Authorization") String authorization, @PathVariable String lederIdent) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "validerLeder").increment();
        String brukerIdent = oidcUtil.finnClaimFraOIDCToken(authorization, "NAVident").orElseThrow(() -> new AuthorizationException("Ikke gyldig OIDC-token"));
        Optional<Bruker> bruker = brukerrepository.findByIdent(brukerIdent);
        if (bruker.isPresent() &&
                ((bruker.get().getRolle().equals(Rolle.LEDER) && bruker.get().getIdent().equals(lederIdent)) ||
                bruker.get().getLedere().stream().anyMatch(leder -> leder.getIdent().equals(lederIdent)))) {
            return lederrepository.findByIdent(lederIdent).orElseThrow(() -> new NotFoundException("Leder med ident " + lederIdent + " finnes ikke i PRIM."));
        } else {
            throw new AuthorizationException("Bruker med ident " + brukerIdent + " har ikke tilgang til leder " + lederIdent);
        }
    }

    private Stream<NomRessurs> hentOrgenhetsLedere(NomOrgEnhet orgEnhet){
        if (orgEnhet == null) return Stream.empty();
        return Stream.concat(
                orgEnhet.getLedere().stream().map(NomLeder::getRessurs),
                orgEnhet.getOrganiseringer().stream().flatMap((organisering -> hentOrgenhetsLedere(organisering.getOrgEnhet())))
        );
    }

    private Set<Leder> hentLedere(String authorization, Bruker bruker) {
        Set<Leder> ledere = new HashSet<>();
        if (bruker.getTilganger() != null) {
            bruker.getTilganger().stream()
                .map((id) -> nomGraphQLClient.hentOrganisasjoner(authorization, id))
                .flatMap(this::hentOrgenhetsLedere)
                .distinct()
                .map(Leder::fraNomRessurs)
                .forEach(oppdatertLeder -> {
                    Optional<Leder> eksisterendeLeder = lederrepository.findByIdent(oppdatertLeder.getIdent());
                    if (eksisterendeLeder.isPresent()) {
                        ledere.add(eksisterendeLeder.get().oppdaterMed(oppdatertLeder));
                    } else {
                        ledere.add(oppdatertLeder);
                    }
                });
        }

        Optional<Leder> lederSelv = lederrepository.findByIdent(bruker.getIdent());
        if (lederSelv.isPresent() && ledere.stream().noneMatch(leder -> leder.getIdent().equals(lederSelv.get().getIdent()))) {
            NomRessurs ressurs = nomGraphQLClient.getRessurs(authorization, bruker.getIdent());
            ledere.add(lederSelv.get().oppdaterMed(Leder.fraNomRessurs(ressurs)));
        }
        return ledere;
    }
}
