package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;

import java.util.Objects;

@Entity
@Table(name = "leder")
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Leder {

    static public Leder fraNomRessurs(NomRessurs ressurs) {
        return new Leder(ressurs.getNavident(), ressurs.getVisningsnavn());
    }

    public Leder(){}

    @Id
    @Column
    @NotNull
    private String ident;

    @Column
    private String navn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Leder leder)) return false;
        return Objects.equals(ident, leder.ident) && Objects.equals(navn, leder.navn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident, navn);
    }
}
