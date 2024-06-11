package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

@Data
public class NomLederFor {
    NomOrgEnhet orgEnhet;

    public NomLederFor(NomOrgEnhet orgEnhet) {
        this.orgEnhet = orgEnhet;
    }
}
