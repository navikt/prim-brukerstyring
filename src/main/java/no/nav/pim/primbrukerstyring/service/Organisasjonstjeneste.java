package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.OrgEnheter;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Protected
@RequestMapping(value = "/organisasjon")
public class Organisasjonstjeneste implements OrganisasjonstjenesteInterface {

    @Autowired
    MeterRegistry metricsRegistry;

    @Autowired
    NomGraphQLClient nomGraphQLClient;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @GetMapping()
    public OrgEnheter hentOrganisasjonstre(@RequestHeader(value = "Authorization") String authorization) {
        metricsRegistry.counter("tjenestekall", "tjeneste", "Organisasjonstjeneste", "metode", "hentOrganisasjonstre").increment();
        return nomGraphQLClient.getOrganisasjonstre(authorization);
    }
}
