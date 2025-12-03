package no.nav.pim.primbrukerstyring.domain;

import lombok.*;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;

import java.time.Instant;
import java.util.Date;
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

        Set<AnsattStillingsavtale> stillingsavtalerUtenDuplikater = stillingsavtaler.stream()
                .filter(stillingsavtale -> {
                    if (stillingsavtaler.stream().anyMatch(sa -> sa.getLeder().getIdent().equals(stillingsavtale.getLeder().getIdent())
                            && sa.getStillingsavtale().equals(Stillingsavtale.DR))) { // Finnes en direkterapporterende avtale koblet på samme leder
                        return stillingsavtale.getStillingsavtale().equals(Stillingsavtale.DR); // Tar kun med direkterapporterende avtale
                    }
                    return true;
                })
                .collect(Collectors.toSet());

        nyAnsatt.setStillingsavtaler(stillingsavtalerUtenDuplikater);
        nyAnsatt.setAktiv(ressurs.getOrgTilknytninger().stream().anyMatch(ot -> !ot.getGyldigFom().after(new Date())));
        return nyAnsatt;
    }

    String ident;

    String navn;

    Boolean aktiv;

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
