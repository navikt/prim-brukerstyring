package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;

@Entity
@Table(name = "leder")
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Leder {

    static public Leder fraNomRessurs(NomRessurs ressurs) {
        return Leder.builder().ident(ressurs.getNavident()).navn(ressurs.getVisningsnavn()).build();
    }

    @Id
    @SequenceGenerator(name = "sequence-generator", sequenceName = "brukerstyring_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence-generator")
    @Column(name = "leder_id")
    private Long lederId;

    @Column
    @NotNull
    private String ident;

    @Column
    @NotNull
    private String navn;
}
