package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import jakarta.validation.Valid;
import no.nav.pim.primbrukerstyring.domain.*;
import no.nav.pim.primbrukerstyring.exceptions.AuthorizationException;
import no.nav.pim.primbrukerstyring.exceptions.NotFoundException;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.NomKobling;
import no.nav.pim.primbrukerstyring.nom.domain.NomLeder;
import no.nav.pim.primbrukerstyring.nom.domain.NomOrgEnhet;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;
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
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
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
                if (ressurs.getLederFor().size() > 0 || overstyrteAnsatte.size() > 0) {
                    Optional<Leder> finnesLeder = lederrepository.findByIdent(ressurs.getNavident());
                    Leder leder = finnesLeder.orElseGet(() -> Leder.fraNomRessurs(ressurs));
                    brukerrepository.save(Bruker.builder().ident(brukerIdent).navn(ressurs.getVisningsnavn()).sistAksessert(new Date()).ledere(Set.of(leder)).rolle(Rolle.LEDER).build());
                    return new BrukerDto(Rolle.LEDER, leder);
                } else {
                    return new BrukerDto(Rolle.MEDARBEIDER, null);
                }
            }
            log.error("###Kunne ikke hente bruker i NOM: {}", brukerIdent);
            return new BrukerDto(Rolle.UKJENT, null);
        } else {
            return new BrukerDto(bruker.get().getRolle(), null);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PostMapping(path = "/rolle", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Bruker leggTilBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @Valid @RequestBody BrukerRolleTilgangerDto brukerDto) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "leggTilBrukerRolle").increment();
        String ident = brukerDto.getIdent();
        Bruker bruker = new Bruker();
        bruker.setIdent(ident);
        bruker.setRolle(brukerDto.getRolle());
        bruker.setTilganger(brukerDto.getTilganger());
        bruker.setSistAksessert(new Date());

        Optional<Bruker> finnesBrukerRolle = brukerrepository.findByIdent(ident);
        if (finnesBrukerRolle.isEmpty()) {
            NomRessurs ressurs = nomGraphQLClient.getLedersResurser(authorization, ident);
            if (ressurs != null) {
                bruker.setNavn(ressurs.getVisningsnavn());
            }
        } else {
            bruker.setNavn(finnesBrukerRolle.get().getNavn());
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
            if (hrBruker.getLedere().size() == 0) {
                List<NomOrgEnhet> orgenheter = bruker.get().getTilganger().stream().map((id) -> nomGraphQLClient.hentOrganisasjoner(authorization, id)).toList();
                Set<Leder> ledere = orgenheter.stream().flatMap(this::hentOrgenhetsLedere).distinct().map(leder -> {
                    Optional<Leder> eksisterendeLeder = lederrepository.findByIdent(leder.getNavident());
                    Leder nyLeder = Leder.fraNomRessurs(leder);
                    if (eksisterendeLeder.isPresent()) {
                        return eksisterendeLeder.get().oppdaterMed(nyLeder);
                    } else {
                        return nyLeder;
                    }
                }).collect(Collectors.toSet());
                Optional<Leder> lederSelv = lederrepository.findByIdent(brukerIdent);
                if (lederSelv.isPresent() && ledere.stream().noneMatch(leder -> leder.getIdent().equals(lederSelv.get().getIdent()))) {
                    ledere.add(lederSelv.get());
                }
                hrBruker.setLedere(ledere);
                hrBruker.setSistAksessert(new Date());
                brukerrepository.save(hrBruker);
                return ledere.stream().map(LederDto::fraLeder).sorted().toList();
            } else {
                if (hrBruker.getSistAksessert().toInstant()
                        .isBefore(Instant.now().atZone(ZoneId.of("Europe/Paris")).minusHours(1).toInstant())) {
                    Set<Leder> ledere = hrBruker.getLedere();
                    List<NomOrgEnhet> orgenheter = bruker.get().getTilganger().stream().map((id) -> nomGraphQLClient.hentOrganisasjoner(authorization, id)).toList();
                    List<Leder> oppdaterteLedere = orgenheter.stream().flatMap(this::hentOrgenhetsLedere).distinct().map(Leder::fraNomRessurs).toList();
                    List<Leder> utdaterteLedere = ledere.stream().filter(leder -> oppdaterteLedere.stream().noneMatch(oppdatertLeder -> oppdatertLeder.getIdent().equals(leder.getIdent()))).toList();
                    utdaterteLedere.forEach(ledere::remove);
                    oppdaterteLedere.forEach(oppdatertLeder -> {
                        Optional<Leder> gammelLeder = ledere.stream().filter(leder -> oppdatertLeder.getIdent().equals(leder.getIdent())).findFirst();
                        if (gammelLeder.isPresent()) {
                            ledere.remove(gammelLeder.get());
                            ledere.add(gammelLeder.get().oppdaterMed(oppdatertLeder));
                        } else {
                            ledere.add(oppdatertLeder);
                        }
                    });
                    Optional<Leder> lederSelv = lederrepository.findByIdent(brukerIdent);
                    if (lederSelv.isPresent() && ledere.stream().noneMatch(leder -> leder.getIdent().equals(lederSelv.get().getIdent()))) {
                        NomRessurs ressurs = nomGraphQLClient.getRessurs(authorization, brukerIdent);
                        ledere.add(lederSelv.get().oppdaterMed(Leder.fraNomRessurs(ressurs)));
                    }
                    hrBruker.setLedere(ledere);
                    hrBruker.setSistAksessert(new Date());
                    brukerrepository.save(hrBruker);
                    return ledere.stream().map(LederDto::fraLeder).sorted().toList();
                } else {
                    return hrBruker.getLedere().stream().map(LederDto::fraLeder).sorted().toList();
                }
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
            List<Ansatt> ansatte = ledersRessurser.getLederFor().stream()
                    .flatMap((lederFor) -> {
                        Stream<NomRessurs> koblinger = lederFor.getOrgEnhet().getKoblinger().stream().map((NomKobling::getRessurs));
                        Stream<NomRessurs> organiseringer = lederFor.getOrgEnhet().getOrganiseringer().stream()
                                .flatMap(org -> org.getOrgEnhet().getLeder().stream().map(NomLeder::getRessurs));
                        return Stream.concat(koblinger, organiseringer);
                    })
                    .filter(ressurs -> !ressurs.getNavident().equals(lederIdent)
                            && ressurs.getLedere().stream().anyMatch(leder -> leder.getRessurs().getNavident().equals(lederIdent))
                    )
                    .distinct().map((ressurs -> {
                        Optional<OverstyrendeLeder> overstyrendeLeder = overstyrendelederrepository.findByAnsattIdentAndTilIsNull(ressurs.getNavident());
                        AnsattStillingsavtale ansattStillingsavtale = null;
                        if (overstyrendeLeder.isPresent()) {
                            ansattStillingsavtale = AnsattStillingsavtale.fraOverstyrendeLeder(overstyrendeLeder.get());
                        }
                        return Ansatt.fraNomRessurs(ressurs, ansattStillingsavtale);
                    })).toList();
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
                ((bruker.get().getRolle() == Rolle.LEDER && bruker.get().getIdent().equals(lederIdent)) ||
                bruker.get().getLedere().stream().anyMatch(leder -> leder.getIdent().equals(lederIdent)))) {
            return lederrepository.findByIdent(lederIdent).orElseThrow(() -> new NotFoundException("Leder med ident " + lederIdent + " finnes ikke i PRIM."));
        } else {
            throw new AuthorizationException("Bruker med ident " + brukerIdent + " har ikke tilgang til leder " + lederIdent);
        }
    }

    private Stream<NomRessurs> hentOrgenhetsLedere(NomOrgEnhet orgEnhet){
        if (orgEnhet == null) return Stream.empty();
        return Stream.concat(orgEnhet.getLeder().stream().map(NomLeder::getRessurs), orgEnhet.getOrganiseringer().stream().flatMap((organisering -> hentOrgenhetsLedere(organisering.getOrgEnhet()))));
    }
}
