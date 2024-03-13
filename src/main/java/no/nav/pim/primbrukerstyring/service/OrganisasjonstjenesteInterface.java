package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.nom.domain.OrgEnheter;

public interface OrganisasjonstjenesteInterface {
        OrgEnheter hentOrganisasjonstre(String authorization);

}
