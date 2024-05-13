package no.nav.pim.primbrukerstyring.service.dto;

import lombok.Builder;
import lombok.Data;
import no.nav.pim.primbrukerstyring.nom.domain.NomOrgEnhet;

import java.util.List;

@Data
@Builder
public class OrgenhetDto {

    static public OrgenhetDto fraNomOrgenhet(NomOrgEnhet orgEnhet) {
        return OrgenhetDto.builder()
                .navn(orgEnhet.getNavn())
                .nomId(orgEnhet.getId())
                .organiseringer(orgEnhet.getOrganiseringer().stream().map(organisering -> OrgenhetDto.fraNomOrgenhet(organisering.getOrgEnhet())).toList())
                .build();
    }

    private String nomId;

    private String navn;

    private List<OrgenhetDto> organiseringer;
}
