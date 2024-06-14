package no.nav.pim.primbrukerstyring.domain;

import lombok.*;
import no.nav.pim.primbrukerstyring.nom.domain.NomLeder;

import java.util.Objects;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnsattStillingsavtale {
    static public AnsattStillingsavtale fraNomLeder(NomLeder nomLeder, AnsattType ansattType, boolean erOverstyrt) {
        Leder leder = Leder.fraNomRessurs(nomLeder.getRessurs());
        return AnsattStillingsavtale.builder()
                .leder(leder)
                .ansattType(ansattType)
                .stillingsavtale((nomLeder.getErDagligOppfolging() && !erOverstyrt) ? Stillingsavtale.DR : Stillingsavtale.MR)
                .build();
    }

    static public AnsattStillingsavtale fraOverstyrendeLeder(OverstyrendeLeder leder) {
        return AnsattStillingsavtale.builder()
                .leder(leder.getOverstyrendeLeder())
                .ansattType(AnsattType.F)
                .stillingsavtale(Stillingsavtale.DR)
                .build();
    }

    private Leder leder;

    private AnsattType ansattType;

    private Stillingsavtale stillingsavtale;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnsattStillingsavtale that)) return false;
        return Objects.equals(leder, that.leder) && ansattType == that.ansattType && stillingsavtale == that.stillingsavtale;
    }

    @Override
    public int hashCode() {
        return Objects.hash(leder.getLederId(), ansattType, stillingsavtale);
    }
}
