package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

@Data
public class NomOrganisering {
    NomOrgEnhet orgEnhet;

    public NomOrganisering(NomOrgEnhet orgEnhet) {
        this.orgEnhet = orgEnhet;
    }
}
