package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.nom.domain.Organisering;

import java.util.List;

public interface OrganisasjonstjenesteInterface {
        List<Organisering> hentOrganisasjonstre(String authorization);

}
