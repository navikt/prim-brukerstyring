package no.nav.pim.primbrukerstyring.domain;

import lombok.*;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ansatt {

    static public Ansatt fraNomRessurs(NomRessurs ressurs, AnsattStillingsavtale ansattStillingsavtale) {
        boolean erOverstyrt = ansattStillingsavtale != null;
        Ansatt nyAnsatt = Ansatt.builder()
                .ident(ressurs.getNavident())
                .navn(ressurs.getVisningsnavn())
                .build();

        Set<AnsattStillingsavtale> stillingsavtaler = ressurs.getLedere().stream()
                .distinct()
                .map(nomLeder -> AnsattStillingsavtale.fraNomLeder(nomLeder, AnsattType.fraNomSektor(ressurs.getSektor()), erOverstyrt)).collect(Collectors.toSet());
        if (erOverstyrt) stillingsavtaler.add(ansattStillingsavtale);
        nyAnsatt.setStillingsavtaler(stillingsavtaler);
        return nyAnsatt;
    }

    String ident;

    String navn;

    Set<AnsattStillingsavtale> stillingsavtaler = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ansatt ansatt)) return false;
        return Objects.equals(ident, ansatt.ident) && Objects.equals(navn, ansatt.navn) && Objects.equals(stillingsavtaler, ansatt.stillingsavtaler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident, navn, stillingsavtaler);
    }
}
