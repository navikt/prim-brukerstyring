package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "ansatt")
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Ansatt {

    static public Ansatt fraNomRessurs(NomRessurs ressurs) {
        Ansatt nyAnsatt = Ansatt.builder()
                .ident(ressurs.getNavident())
                .navn(ressurs.getVisningsnavn())
                .build();
        Set<AnsattStillingsavtale> stillingsavtaler = ressurs.getLedere().stream().distinct().map(leder -> AnsattStillingsavtale.fraNomLeder(nyAnsatt, leder)).collect(Collectors.toSet());
        nyAnsatt.setStillingsavtaler(stillingsavtaler);
        return nyAnsatt;
    }

    @Id
    @SequenceGenerator(name = "sequence-generator", sequenceName = "brukerstyring_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence-generator")
    @Column(name = "ansatt_id")
    private Long ansattId;

    @Column
    @NotNull
    String ident;

    @Column
    @NotNull
    String navn;

    @OneToMany(mappedBy = "ansatt")
    @JsonBackReference
    Set<AnsattStillingsavtale> stillingsavtaler = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ansatt ansatt)) return false;
        return Objects.equals(ansattId, ansatt.ansattId) && Objects.equals(ident, ansatt.ident) && Objects.equals(navn, ansatt.navn) && Objects.equals(stillingsavtaler, ansatt.stillingsavtaler);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ansattId, ident, navn, stillingsavtaler);
    }
}
