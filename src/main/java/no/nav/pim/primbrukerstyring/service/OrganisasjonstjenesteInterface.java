package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.service.dto.OrgenhetDto;

import java.util.List;

public interface OrganisasjonstjenesteInterface {
        List<OrgenhetDto> hentOrganisasjonstre(String authorization);

}
