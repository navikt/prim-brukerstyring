package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NomOrganisering {
    NomOrgEnhet orgEnhet;
}
