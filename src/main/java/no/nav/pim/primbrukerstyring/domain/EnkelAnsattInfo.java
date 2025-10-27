package no.nav.pim.primbrukerstyring.domain;

import lombok.*;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;

@Builder
public class EnkelAnsattInfo {
    static public EnkelAnsattInfo fraNomRessurs(NomRessurs ressurs) {
        return EnkelAnsattInfo.builder()
                .ident(ressurs.getNavident())
                .navn(ressurs.getVisningsnavn())
                .epost(ressurs.getEpost())
                .build();
    }

    String ident;
    String navn;
    String epost;
}
