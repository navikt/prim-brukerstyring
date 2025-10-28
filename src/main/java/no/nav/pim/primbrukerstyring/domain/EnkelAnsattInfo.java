package no.nav.pim.primbrukerstyring.domain;

import lombok.*;
import no.nav.pim.primbrukerstyring.nom.domain.NomLeder;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;

import java.util.Optional;

@Builder
@Data
public class EnkelAnsattInfo {
    static public EnkelAnsattInfo fraNomRessurs(NomRessurs ressurs) {

        Optional<NomLeder> nomLeder = ressurs.getLedere().stream()
                .distinct()
                .filter(NomLeder::getErDagligOppfolging)
                .findFirst();


        return EnkelAnsattInfo.builder()
                .ident(ressurs.getNavident())
                .navn(ressurs.getVisningsnavn())
                .epost(ressurs.getEpost())
                .lederIdent(nomLeder.map(leder -> leder.getRessurs().getNavident()).orElse(null))
                .build();
    }

    String ident;
    String navn;
    String epost;
    String lederIdent;
}
