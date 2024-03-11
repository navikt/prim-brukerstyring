package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "bruker_rolle")
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BrukerRolle {

    public BrukerRolle() {}

    @Id
    @Column(name = "ident")
    @NotNull
    private String ident;

    @Column
    @Enumerated(EnumType.STRING)
    @NotNull
    private Rolle rolle;

    @Column
    private String navn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrukerRolle that)) return false;
        return Objects.equals(ident, that.ident) && Objects.equals(rolle, that.rolle) && Objects.equals(navn, that.navn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident, rolle, navn);
    }
}
