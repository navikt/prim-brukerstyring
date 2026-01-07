package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "overstyrende_leder")
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OverstyrendeLeder {

    @Id
    @SequenceGenerator(name = "sequence-generator", sequenceName = "brukerstyring_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence-generator")
    @Column(name = "id")
    private Long id;

    @Column
    @NotNull
    private String ansattIdent;

    @Column
    @NotNull
    private String ansattNavn;

    @ManyToOne(fetch=FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "overstyrende_leder_id", referencedColumnName = "leder_id")
    private Leder overstyrendeLeder;

    @Column
    private LocalDate fra;

    @Column
    private LocalDate til;
}
