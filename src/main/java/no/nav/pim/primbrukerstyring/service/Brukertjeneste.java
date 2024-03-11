package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import jakarta.validation.Valid;
import no.nav.pim.primbrukerstyring.domain.BrukerRolle;
import no.nav.pim.primbrukerstyring.domain.Rolle;
import no.nav.pim.primbrukerstyring.exceptions.AuthorizationException;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.Leder;
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

@RestController
@Protected
@RequestMapping(value = "/bruker")
public class Brukertjeneste implements BrukertjenesteInterface{

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
    @GetMapping(path = "/rolle/{brukerIdent}")
    public Rolle hentBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @PathVariable String brukerIdent) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentBrukerRolle").increment();
        //String brukerIdent = oidcUtil.finnClaimFraOIDCToken(authorization, "NAVident").orElseThrow(() -> new AuthorizationException("Ikke gyldig OIDC-token"));
        Optional<BrukerRolle> brukerRolle = brukerrollerepository.findByIdent(brukerIdent);

        if (brukerRolle.isEmpty()) {
            Leder leder = nomGraphQLClient.getLedersResurser(authorization, brukerIdent);
            if (leder != null) {
                if (leder.getLederFor().size() > 0) {
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
            Leder leder = nomGraphQLClient.getLedersResurser(authorization, brukerRolle.getIdent());
            if (leder != null) {
                brukerRolle.setNavn(leder.getVisningsnavn());
            }
            return brukerrollerepository.save(brukerRolle);
        } else {
            Metrics.counter("prim_error", "exception", "UserAlreadyExistException").increment();
            throw new RuntimeException("Bruker med ident " + brukerRolle.getIdent() + " har allerede en rolle i PRIM");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @PutMapping(path = "/rolle/{ident}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BrukerRolle endreBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident, @Valid @RequestBody Rolle rolle) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "endreBrukerRolle").increment();
        Optional<BrukerRolle> eksisterendeBrukerRolle = brukerrollerepository.findByIdent(ident);
        if (eksisterendeBrukerRolle.isPresent()) {
            BrukerRolle oppdatertBrukerRolle = eksisterendeBrukerRolle.get();
            oppdatertBrukerRolle.setRolle(rolle);
            return brukerrollerepository.save(oppdatertBrukerRolle);
        } else {
            Metrics.counter("prim_error", "exception", "UserAlreadyExistException").increment();
            throw new RuntimeException("Bruker med ident " + ident + " har allerede en rolle i PRIM");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @DeleteMapping(path = "/rolle/{ident}")
    public void slettBrukerRolle(@RequestHeader(value = "Authorization") String authorization, @PathVariable String ident) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "leggTilBrukerRolle").increment();
        brukerrollerepository.deleteByIdent(ident);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @GetMapping(path = "/{rolle}")
    public List<BrukerRolle> hentAlleBrukereMedRolle(@RequestHeader(value = "Authorization") String authorization,  @PathVariable Rolle rolle) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Brukertjeneste", "metode", "hentAlleMedHRMedarbeiderRolle").increment();
        return brukerrollerepository.findAllByRolle(rolle);
    }
}
