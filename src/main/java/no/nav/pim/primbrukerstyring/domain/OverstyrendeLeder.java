package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
    @Column
    @NotNull
    String ansattIdent;

    @Column
    @NotNull
    String ansattNavn;

    @ManyToOne(fetch=FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "overstyrende_leder_id", referencedColumnName = "leder_id")
    private Leder overstyrendeLeder;

}
