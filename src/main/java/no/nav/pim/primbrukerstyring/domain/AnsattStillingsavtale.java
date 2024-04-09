package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import no.nav.pim.primbrukerstyring.nom.domain.NomLeder;

import java.io.Serializable;

@Entity
@Table(name = "ansattStillingsavtale")
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AnsattStillingsavtale {
    static AnsattStillingsavtale fraNomLeder(NomLeder leder) {
        return AnsattStillingsavtale.builder()
                .leder(Leder.fraNomRessurs(leder.getRessurs()))
                .ansattType(AnsattType.fraNomSektor(leder.getRessurs().getSektor()))
                .stillingsavtale(leder.getErDagligOppfolfing() ? Stillingsavtale.DR : Stillingsavtale.MR)
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
}
