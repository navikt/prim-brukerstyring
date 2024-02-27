package no.nav.pim.primbrukerstyring.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import jakarta.validation.Valid;
import no.nav.pim.primbrukerstyring.domain.BrukerRolle;
import no.nav.pim.primbrukerstyring.domain.Rolle;
import no.nav.pim.primbrukerstyring.exceptions.AuthorizationException;
import no.nav.pim.primbrukerstyring.repository.BrukerRollerepository;
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
@RequestMapping(value = "/bruker")
public class Brukertjeneste implements BrukertjenesteInterface{

    private static final Logger log = LoggerFactory.getLogger(Brukertjeneste.class);

    @Autowired
    MeterRegistry metricsRegistry;

    @Autowired
    BrukerRollerepository brukerRollerepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @GetMapping(path = "/rolle")
    public Rolle hentBrukerRolle(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentBrukerRolle").increment();
        String brukerIdent = finnClaimFraOIDCToken(authorization, "NAVident").orElseThrow(() -> new AuthorizationException("Ikke gyldig OIDC-token"));
        Optional<BrukerRolle> brukerRolle = brukerRollerepository.findByIdent(brukerIdent);

        if (brukerRolle.isEmpty()) {
            // HENT FRA NOM
        } else {
            return brukerRolle.get().getRolle();
        }
        log.error("###Kunne ikke finne rolle for bruker {}", brukerIdent);
        return Rolle.UKJENT;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @PostMapping(path = "/rolle", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BrukerRolle leggTilBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @Valid @RequestBody BrukerRolle brukerRolle) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "leggTilBrukerRolle").increment();
        Optional<BrukerRolle> finnesBrukerRolle = brukerRollerepository.findByIdent(brukerRolle.getIdent());

        if (finnesBrukerRolle.isEmpty()) {
            return brukerRollerepository.save(brukerRolle);
        } else {
            Metrics.counter("prim_error", "exception", "UserAlreadyExistException").increment();
            throw new RuntimeException("Bruker med ident " + brukerRolle.getIdent() + " har allerede en rolle i PRIM");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @PutMapping(path = "/rolle/{ident}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BrukerRolle endreBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident, @Valid @RequestBody Rolle rolle) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "endreBrukerRolle").increment();
        Optional<BrukerRolle> eksisterendeBrukerRolle = brukerRollerepository.findByIdent(ident);
        if (eksisterendeBrukerRolle.isPresent()) {
            BrukerRolle oppdatertBrukerRolle = eksisterendeBrukerRolle.get();
            oppdatertBrukerRolle.setRolle(rolle);
            return brukerRollerepository.save(oppdatertBrukerRolle);
        } else {
            Metrics.counter("prim_error", "exception", "UserAlreadyExistException").increment();
            throw new RuntimeException("Bruker med ident " + ident + " har allerede en rolle i PRIM");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @DeleteMapping(path = "/rolle/{ident}")
    public void slettBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "leggTilBrukerRolle").increment();
        brukerRollerepository.deleteByIdent(ident);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @GetMapping(path = "/{rolle}")
    public List<BrukerRolle> hentAlleBrukereMedRolle(@RequestHeader(value = "Authorization") String authorization,  @PathVariable Rolle rolle) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentAlleMedHRMedarbeiderRolle").increment();
        return brukerRollerepository.findAllByRolle(rolle);
    }

    private Optional<String> finnClaimFraOIDCToken(String authorization, String claim) {
        if (authorization != null) {
            String[] authElements = authorization.split(",");
            for (String authElement : authElements) {
                try {
                    String[] pair = authElement.split(" ");
                    if (pair[0].trim().equalsIgnoreCase("bearer")) {
                        JWT jwt = JWTParser.parse(pair[1].trim());
                        return Optional.ofNullable(jwt.getJWTClaimsSet().getStringClaim(claim));
                    }
                } catch (Exception e) {
                    log.error("###OIDC-token har ikke riktig format");
                    return Optional.empty();
                }
            }
            log.error("###Authorization-header inneholder ikke OIDC-token");
            return Optional.empty();
        }
        log.error("###Ingen Authorization-header");
        return Optional.empty();
    }
}
