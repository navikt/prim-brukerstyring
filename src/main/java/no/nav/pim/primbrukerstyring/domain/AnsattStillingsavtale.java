package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import no.nav.pim.primbrukerstyring.nom.domain.NomLeder;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "ansatt_stillingsavtale")
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AnsattStillingsavtale {
    static AnsattStillingsavtale fraNomLeder(NomLeder nomLeder, Leder leder) {
        return AnsattStillingsavtale.builder()
                .leder(leder)
                .ansattType(AnsattType.fraNomSektor(nomLeder.getRessurs().getSektor()))
                .stillingsavtale(nomLeder.getErDagligOppfolging() ? Stillingsavtale.DR : Stillingsavtale.MR)
                .build();
    }

    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    public static class Id implements Serializable {

        @Column(name = "ansatt_id")
        protected Long ansattId;

        @Column(name = "leder_id")
        protected Long lederId;

        @Override
        public String toString() {
            return "Id{" +
                    "ansattId=" + ansattId +
                    ", lederId=" + lederId +
                    '}';
        }
    }

    @EmbeddedId
    protected Id id = new Id();

    @ManyToOne
    @JoinColumn(name = "ansatt_id", insertable = false, updatable = false)
    @JsonBackReference
    private Ansatt ansatt;

    @ManyToOne
    @JoinColumn(name = "leder_id", insertable = false, updatable = false)
    @JsonBackReference
    private Leder leder;

    @Enumerated(EnumType.STRING)
    private AnsattType ansattType;

    @Enumerated(EnumType.STRING)
    private Stillingsavtale stillingsavtale;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnsattStillingsavtale that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(ansatt, that.ansatt) && Objects.equals(leder, that.leder) && ansattType == that.ansattType && stillingsavtale == that.stillingsavtale;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ansattType, stillingsavtale);
    }
}
