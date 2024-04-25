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
    static AnsattStillingsavtale fraNomLeder(NomLeder nomLeder, Leder leder) {
        return AnsattStillingsavtale.builder()
                .leder(leder)
                .ansattType(AnsattType.fraNomSektor(nomLeder.getRessurs().getSektor()))
                .stillingsavtale(nomLeder.getErDagligOppfolging() ? Stillingsavtale.DR : Stillingsavtale.MR)
                .build();
    }

    private Leder leder;

    private Ansatt ansatt;

    private AnsattType ansattType;

    private Stillingsavtale stillingsavtale;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnsattStillingsavtale that)) return false;
        return Objects.equals(leder, that.leder) && Objects.equals(ansatt, that.ansatt) && ansattType == that.ansattType && stillingsavtale == that.stillingsavtale;
    }

    @Override
    public int hashCode() {
        return Objects.hash(leder.getIdent(), ansatt.getIdent(), ansattType, stillingsavtale);
    }
}
