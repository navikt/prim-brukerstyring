package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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
    @Column
    private String ident;

    @Column
    @Enumerated(EnumType.STRING)
    private Rolle rolle;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrukerRolle that)) return false;
        return Objects.equals(ident, that.ident) && Objects.equals(rolle, that.rolle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident, rolle);
    }
}
