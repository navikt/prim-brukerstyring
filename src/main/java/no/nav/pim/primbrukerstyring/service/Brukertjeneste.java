package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import jakarta.validation.Valid;
import no.nav.pim.primbrukerstyring.domain.BrukerRolle;
import no.nav.pim.primbrukerstyring.domain.Rolle;
import no.nav.pim.primbrukerstyring.exceptions.AuthorizationException;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.Kobling;
import no.nav.pim.primbrukerstyring.nom.domain.Leder;
import no.nav.pim.primbrukerstyring.nom.domain.Ressurs;
import no.nav.pim.primbrukerstyring.repository.BrukerRollerepository;
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
    BrukerRollerepository brukerrollerepository;

    @Autowired
    OIDCUtil oidcUtil;

    @Autowired
    NomGraphQLClient nomGraphQLClient;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @GetMapping(path = "/rolle")
    public Rolle hentBrukerRolle(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentBrukerRolle").increment();
        String brukerIdent = oidcUtil.finnClaimFraOIDCToken(authorization, "NAVident").orElseThrow(() -> new AuthorizationException("Ikke gyldig OIDC-token"));
        Optional<BrukerRolle> brukerRolle = brukerrollerepository.findByIdent(brukerIdent);

        if (brukerRolle.isEmpty()) {
            Ressurs ressurs = nomGraphQLClient.getLedersResurser(authorization, brukerIdent);
            if (ressurs != null) {
                if (ressurs.getLederFor().size() > 0) {
                    brukerrollerepository.save(BrukerRolle.builder().ident(brukerIdent).rolle(Rolle.LEDER).build());
                    return Rolle.LEDER;
                } else {
                    return Rolle.MEDARBEIDER;
                }
            }
            log.error("###Kunne ikke hente bruker i NOM: {}", brukerIdent);
            return Rolle.UKJENT;
        } else {
            return brukerRolle.get().getRolle();
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PostMapping(path = "/rolle", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BrukerRolle leggTilBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @Valid @RequestBody BrukerRolle brukerRolle) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "leggTilBrukerRolle").increment();
        Optional<BrukerRolle> finnesBrukerRolle = brukerrollerepository.findByIdent(brukerRolle.getIdent());

        if (finnesBrukerRolle.isEmpty()) {
            Ressurs ressurs = nomGraphQLClient.getLedersResurser(authorization, brukerRolle.getIdent());
            if (ressurs != null) {
                brukerRolle.setNavn(ressurs.getVisningsnavn());
            }
        } else {
            brukerRolle.setNavn(finnesBrukerRolle.get().getNavn());
        }
        return brukerrollerepository.save(brukerRolle);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PutMapping(path = "/rolle/{ident}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BrukerRolle endreBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident, @Valid @RequestBody BrukerRolle brukerRolle) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "endreBrukerRolle").increment();
        Optional<BrukerRolle> eksisterendeBrukerRolle = brukerrollerepository.findByIdent(ident);
        if (eksisterendeBrukerRolle.isPresent()) {
            BrukerRolle oppdatertBrukerRolle = eksisterendeBrukerRolle.get();
            oppdatertBrukerRolle.setRolle(brukerRolle.getRolle());
            return brukerrollerepository.save(oppdatertBrukerRolle);
        } else {
            Metrics.counter("prim_error", "exception", "UserDoesntExistException").increment();
            throw new RuntimeException("Bruker med ident " + ident + " eksisterer ikke i PRIM");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PutMapping(path = "/tilganger/{ident}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BrukerRolle endreBrukerTilganger(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident, @Valid @RequestBody BrukerRolle brukerRolle) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "endreBrukerRolle").increment();
        Optional<BrukerRolle> eksisterendeBrukerRolle = brukerrollerepository.findByIdent(ident);
        if (eksisterendeBrukerRolle.isPresent()) {
            BrukerRolle oppdatertBrukerRolle = eksisterendeBrukerRolle.get();
            oppdatertBrukerRolle.setTilganger(brukerRolle.getTilganger());
            return brukerrollerepository.save(oppdatertBrukerRolle);
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
        brukerrollerepository.deleteByIdent(ident);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @GetMapping(path = "/hr")
    public List<BrukerRolle> hentAlleHRMedarbeidere(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentAlleHRMedarbeidere").increment();
        return brukerrollerepository.findAllByRolleIn(List.of(Rolle.HR_MEDARBEIDER, Rolle.HR_MEDARBEIDER_BEMANNING));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @GetMapping(path = "/ressurser/{ident}")
    public List<Ressurs> hentLedersRessurser(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentLedersRessurser").increment();
        String brukerIdent = oidcUtil.finnClaimFraOIDCToken(authorization, "NAVident").orElseThrow(() -> new AuthorizationException("Ikke gyldig OIDC-token"));
        Optional<BrukerRolle> brukerRolle = brukerrollerepository.findByIdent(brukerIdent);
        boolean erHR = brukerRolle.isPresent() && List.of(Rolle.HR_MEDARBEIDER, Rolle.HR_MEDARBEIDER_BEMANNING).contains(brukerRolle.get().getRolle());
        if (erHR || brukerIdent.equals(ident)) {
            Ressurs ledersRessurser = nomGraphQLClient.getLedersResurser(authorization, ident);
            return ledersRessurser.getLederFor().stream()
                .flatMap((lederFor) -> {
                    List<Ressurs> koblinger = lederFor.getOrgEnhet().getKoblinger().stream().map((Kobling::getRessurs)).toList();
                    List<Ressurs> organiseringer = lederFor.getOrgEnhet().getOrganiseringer().stream()
                            .flatMap(org -> org.getOrgEnhet().getLeder().stream().map(Leder::getRessurs)).toList();
                    return Stream.concat(koblinger.stream(), organiseringer.stream());
                }).filter(ressurs -> !ressurs.getNavident().equals(ident)).distinct().toList();
        } else {
            throw new AuthorizationException("Bruker med ident "+ brukerIdent + " har ikke tilgang til ident " + ident);
        }

    }
}
