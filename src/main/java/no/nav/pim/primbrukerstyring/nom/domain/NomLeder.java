package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

@Data
public class NomLeder {
    Boolean erDagligOppfolging;
    NomRessurs ressurs;

    /*public NomLeder(Boolean erDagligOppfolging, NomRessurs ressurs) {
        this.erDagligOppfolging = erDagligOppfolging;
        this.ressurs = ressurs;
    }*/
}
