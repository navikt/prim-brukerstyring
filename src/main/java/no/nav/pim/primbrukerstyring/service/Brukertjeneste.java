package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import jakarta.validation.Valid;
import no.nav.pim.primbrukerstyring.domain.*;
import no.nav.pim.primbrukerstyring.exceptions.AuthorizationException;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.NomKobling;
import no.nav.pim.primbrukerstyring.nom.domain.NomLeder;
import no.nav.pim.primbrukerstyring.nom.domain.NomOrgEnhet;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;
import no.nav.pim.primbrukerstyring.repository.Brukerrepository;
import no.nav.pim.primbrukerstyring.repository.Lederrepository;
import no.nav.pim.primbrukerstyring.service.dto.BrukerDto;
import no.nav.pim.primbrukerstyring.util.OIDCUtil;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@Protected
@RequestMapping(value = "/bruker")
public class Brukertjeneste implements BrukertjenesteInterface {

    private static final Logger log = LoggerFactory.getLogger(Brukertjeneste.class);

    @Autowired
    MeterRegistry metricsRegistry;

    @Autowired
    Brukerrepository brukerrepository;

    @Autowired
    Lederrepository lederrepository;

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
                if (ressurs.getLederFor().size() > 0) {
                    Leder leder = Leder.builder().ident(brukerIdent).navn(ressurs.getVisningsnavn()).build();
                    brukerrepository.save(Bruker.builder().ident(brukerIdent).navn(ressurs.getVisningsnavn()).representertLeder(leder).rolle(Rolle.LEDER).build());
                    return new BrukerDto(Rolle.LEDER, leder);
                } else {
                    return new BrukerDto(Rolle.MEDARBEIDER, null);
                }
            }
            log.error("###Kunne ikke hente bruker i NOM: {}", brukerIdent);
            return new BrukerDto(Rolle.UKJENT, null);
        } else {
            return new BrukerDto(bruker.get().getRolle(), bruker.get().getRepresentertLeder());
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
            return ledersRessurser.getLederFor().stream()
                    .flatMap((lederFor) -> {
                        Stream<NomRessurs> koblinger = lederFor.getOrgEnhet().getKoblinger().stream().map((NomKobling::getRessurs));
                        Stream<NomRessurs> organiseringer = lederFor.getOrgEnhet().getOrganiseringer().stream()
                                .flatMap(org -> org.getOrgEnhet().getLeder().stream().map(NomLeder::getRessurs));
                        return Stream.concat(koblinger, organiseringer);
                    }).filter(ressurs -> !ressurs.getNavident().equals(lederIdent) && ressurs.getLedere().stream().anyMatch(leder -> leder.getRessurs().getNavident().equals(lederIdent)))
                    .distinct().map((ressurs -> {
                        Ansatt ansatt = Ansatt.fraNomRessurs(ressurs);
                        log.info("Oppretter ressurs {} med {} stillingsavtaler", ansatt.getIdent(), ansatt.getStillingsavtaler().size());
                        return ansatt;
                    })).toList();
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
            return orgenheter.stream().flatMap(this::hentOrgenhetsLedere).distinct().map(Leder::fraNomRessurs).toList();
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
            if (representertLeder != null) {
                List<NomOrgEnhet> orgenheter = bruker.get().getTilganger().stream().map((id) -> nomGraphQLClient.hentOrganisasjoner(authorization, id)).toList();
                Optional<NomRessurs> lederRessurs = orgenheter.stream().flatMap(this::hentOrgenhetsLedere).filter((ressurs) -> ressurs.getNavident().equals(representertLeder.getIdent())).findFirst();
                if (lederRessurs.isPresent()) {
                    Leder leder;
                    Optional<Leder> eksisterendeLeder = lederrepository.findByIdent(representertLeder.getIdent());
                    leder = eksisterendeLeder.orElseGet(() -> lederrepository.save(Leder.fraNomRessurs(lederRessurs.get())));

                    Bruker brukerMedLeder = bruker.get();
                    brukerMedLeder.setRepresentertLeder(leder);
                    brukerrepository.save(brukerMedLeder);
                    return leder;
                } else {
                    throw new AuthorizationException("Bruker med ident " + brukerIdent + " har ikke tilgang til leder " + representertLeder.getIdent());
                }
            } else {
                Bruker brukerUtenLeder = bruker.get();
                brukerUtenLeder.setRepresentertLeder(null);
                brukerrepository.save(brukerUtenLeder);
                return null;
            }
        } else {
            throw new AuthorizationException("Bruker med ident "+ brukerIdent + " er ikke HR ansatt");
        }
    }

    private Stream<NomRessurs> hentOrgenhetsLedere(NomOrgEnhet orgEnhet){
        if (orgEnhet == null) return Stream.empty();
        return Stream.concat(orgEnhet.getLeder().stream().map(NomLeder::getRessurs), orgEnhet.getOrganiseringer().stream().flatMap((organisering -> hentOrgenhetsLedere(organisering.getOrgEnhet()))));
    }
}
