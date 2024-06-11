package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

@Data
public class NomKobling {
    NomRessurs ressurs;

    public NomKobling (NomRessurs ressurs) {
        this.ressurs = ressurs;
    }
}
