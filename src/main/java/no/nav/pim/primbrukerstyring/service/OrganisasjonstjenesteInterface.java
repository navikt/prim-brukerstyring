package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.nom.domain.NomOrganisering;

import java.util.List;

public interface OrganisasjonstjenesteInterface {
        List<NomOrganisering> hentOrganisasjonstre(String authorization);

}
