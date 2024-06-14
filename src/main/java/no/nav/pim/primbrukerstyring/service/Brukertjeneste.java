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
import no.nav.pim.primbrukerstyring.util.OIDCUtil;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
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

    @Value("${representertLeder.synkronisering.intervall:8}")
    private int representertLederSynkroniseringsIntervall;

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
                    brukerrepository.save(Bruker.builder().ident(brukerIdent).navn(ressurs.getVisningsnavn()).sistAksessert(new Date()).representertLeder(leder).rolle(Rolle.LEDER).build());
                    return new BrukerDto(Rolle.LEDER, leder);
                } else {
                    return new BrukerDto(Rolle.MEDARBEIDER, null);
                }
            }
            log.error("###Kunne ikke hente bruker i NOM: {}", brukerIdent);
            return new BrukerDto(Rolle.UKJENT, null);
        } else {
            Bruker oppdatertBruker = bruker.get();
            if (oppdatertBruker.getRolle() != Rolle.LEDER) {
                if (oppdatertBruker.getSistAksessert().toInstant()
                        .isBefore(Instant.now().atZone(ZoneId.of("Europe/Paris")).minusHours(representertLederSynkroniseringsIntervall).toInstant())) {
                    oppdatertBruker.setRepresentertLeder(null);
                }
                oppdatertBruker.setSistAksessert(new Date());
                brukerrepository.save(oppdatertBruker);
            }
            return new BrukerDto(oppdatertBruker.getRolle(), oppdatertBruker.getRepresentertLeder());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PostMapping(path = "/rolle", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Bruker leggTilBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @Valid @RequestBody Bruker bruker) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "leggTilBrukerRolle").increment();
        Optional<Bruker> finnesBrukerRolle = brukerrepository.findByIdent(bruker.getIdent());

        if (finnesBrukerRolle.isEmpty()) {
            NomRessurs ressurs = nomGraphQLClient.getLedersResurser(authorization, bruker.getIdent());
            if (ressurs != null) {
                bruker.setNavn(ressurs.getVisningsnavn());
            }
        } else {
            bruker.setNavn(finnesBrukerRolle.get().getNavn());
        }
        bruker.setSistAksessert(new Date());
        return brukerrepository.save(bruker);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PutMapping(path = "/rolle/{ident}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Bruker endreBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident, @Valid @RequestBody Bruker bruker) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "endreBrukerRolle").increment();
        Optional<Bruker> eksisterendeBrukerRolle = brukerrepository.findByIdent(ident);
        if (eksisterendeBrukerRolle.isPresent()) {
            Bruker oppdatertBruker = eksisterendeBrukerRolle.get();
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
    public Bruker endreBrukerTilganger(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident, @Valid @RequestBody Bruker bruker) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "endreBrukerRolle").increment();
        Optional<Bruker> eksisterendeBrukerRolle = brukerrepository.findByIdent(ident);
        if (eksisterendeBrukerRolle.isPresent()) {
            Bruker oppdatertBruker = eksisterendeBrukerRolle.get();
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
    @GetMapping(path = "/ressurser")
    public List<Ansatt> hentLedersRessurser(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentLedersRessurser").increment();
        String brukerIdent = oidcUtil.finnClaimFraOIDCToken(authorization, "NAVident").orElseThrow(() -> new AuthorizationException("Ikke gyldig OIDC-token"));
        Optional<Bruker> bruker = brukerrepository.findByIdent(brukerIdent);
        boolean erHR = bruker.isPresent() && List.of(Rolle.HR_MEDARBEIDER, Rolle.HR_MEDARBEIDER_BEMANNING).contains(bruker.get().getRolle());
        String lederIdent;
        if (erHR) {
            lederIdent = bruker.get().getRepresentertLeder().getIdent();
        } else {
            lederIdent = brukerIdent;
        }
        if (!Objects.isNull(lederIdent)) {
            NomRessurs ledersRessurser = nomGraphQLClient.getLedersResurser(authorization, lederIdent);
            System.out.println(ledersRessurser.getLederFor().size());
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
                    Ansatt ansatt = Ansatt.fraNomRessurs(ressurs, ansattStillingsavtale);
                    log.info("Oppretter ressurs {} med {} stillingsavtaler", ansatt.getIdent(), ansatt.getStillingsavtaler().size());
                    return ansatt;
                })).toList();
            Stream<Ansatt> overstyrteAnsatte = overstyrendelederrepository.findByOverstyrendeLeder_IdentAndTilIsNull(lederIdent).stream()
                    .filter(overstyrtLeder -> ansatte.stream().noneMatch(ansatt -> ansatt.getIdent().equals(overstyrtLeder.getAnsattIdent())))
                    .map(overstyrtLeder ->  ansatttjeneste.hentAnsatt(authorization, overstyrtLeder.getAnsattIdent()));
            return Stream.concat(ansatte.stream(), overstyrteAnsatte).toList();
        } else {
            throw new AuthorizationException("Representert leder er ikke satt for bruker med ident " + brukerIdent);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @GetMapping(path = "/ledere")
    public List<Leder> hentLedere(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentLedere").increment();
        String brukerIdent = oidcUtil.finnClaimFraOIDCToken(authorization, "NAVident").orElseThrow(() -> new AuthorizationException("Ikke gyldig OIDC-token"));
        Optional<Bruker> bruker = brukerrepository.findByIdent(brukerIdent);
        boolean erHR = bruker.isPresent() && List.of(Rolle.HR_MEDARBEIDER, Rolle.HR_MEDARBEIDER_BEMANNING).contains(bruker.get().getRolle());
        if (erHR) {
            List<NomOrgEnhet> orgenheter = bruker.get().getTilganger().stream().map((id) -> nomGraphQLClient.hentOrganisasjoner(authorization, id)).toList();
            List<Leder> ledere = orgenheter.stream().flatMap(this::hentOrgenhetsLedere).distinct().map(Leder::fraNomRessurs).sorted().toList();
            Optional<Leder> lederSelv = lederrepository.findByIdent(brukerIdent);
            if (lederSelv.isPresent() && ledere.stream().noneMatch(leder -> leder.getIdent().equals(lederSelv.get().getIdent()))) {
                List<Leder> ledereMedLederSelv = new ArrayList<>(ledere);
                ledereMedLederSelv.add(lederSelv.get());
                return ledereMedLederSelv;
            }
            return ledere;

        } else {
            throw new AuthorizationException("Bruker med ident "+ brukerIdent + " er ikke HR ansatt");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PutMapping(path = "/leder", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Leder settRepresentertLeder(@RequestHeader(value = "Authorization") String authorization, @RequestBody Leder representertLeder) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "settRepresentertLeder").increment();
        String brukerIdent = oidcUtil.finnClaimFraOIDCToken(authorization, "NAVident").orElseThrow(() -> new AuthorizationException("Ikke gyldig OIDC-token"));
        Optional<Bruker> bruker = brukerrepository.findByIdent(brukerIdent);
        boolean erHR = bruker.isPresent() && List.of(Rolle.HR_MEDARBEIDER, Rolle.HR_MEDARBEIDER_BEMANNING).contains(bruker.get().getRolle());
        if (bruker.isPresent() && erHR) {
            List<NomOrgEnhet> orgenheter = bruker.get().getTilganger().stream().map((id) -> nomGraphQLClient.hentOrganisasjoner(authorization, id)).toList();
            Optional<NomRessurs> lederRessurs = orgenheter.stream().flatMap(this::hentOrgenhetsLedere).filter((ressurs) -> ressurs.getNavident().equals(representertLeder.getIdent())).findFirst();
            boolean erLederSelv = bruker.get().getIdent().equals(representertLeder.getIdent());
            if (lederRessurs.isPresent() || erLederSelv) {
                Optional<Leder> eksisterendeLeder = lederrepository.findByIdent(representertLeder.getIdent());
                if (erLederSelv && eksisterendeLeder.isEmpty()) throw new AuthorizationException("Bruker med ident " + brukerIdent + " er ikke mulig å velge som leder.");
                Leder leder = eksisterendeLeder.orElseGet(() -> lederrepository.save(Leder.fraNomRessurs(lederRessurs.get())));

                Bruker brukerMedLeder = bruker.get();
                brukerMedLeder.setSistAksessert(new Date());
                brukerMedLeder.setRepresentertLeder(leder);
                brukerrepository.save(brukerMedLeder);
                return leder;
            } else {
                throw new AuthorizationException("Bruker med ident " + brukerIdent + " har ikke tilgang til leder " + representertLeder.getIdent());
            }
        } else {
            throw new AuthorizationException("Bruker med ident "+ brukerIdent + " er ikke HR ansatt");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @DeleteMapping(path = "/leder")
    public void fjernRepresentertLeder(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "fjernRepresentertLeder").increment();
        String brukerIdent = oidcUtil.finnClaimFraOIDCToken(authorization, "NAVident").orElseThrow(() -> new AuthorizationException("Ikke gyldig OIDC-token"));
        Optional<Bruker> bruker = brukerrepository.findByIdent(brukerIdent);
        if (bruker.isPresent()) {
            Bruker brukerUtenLeder = bruker.get();
            brukerUtenLeder.setRepresentertLeder(null);
            brukerUtenLeder.setSistAksessert(new Date());
            brukerrepository.save(brukerUtenLeder);
        } else {
            throw new NotFoundException("Kunne ikke finne bruker " + brukerIdent + " i PRIM.");
        }
    }

    private Stream<NomRessurs> hentOrgenhetsLedere(NomOrgEnhet orgEnhet){
        if (orgEnhet == null) return Stream.empty();
        return Stream.concat(orgEnhet.getLeder().stream().map(NomLeder::getRessurs), orgEnhet.getOrganiseringer().stream().flatMap((organisering -> hentOrgenhetsLedere(organisering.getOrgEnhet()))));
    }
}
