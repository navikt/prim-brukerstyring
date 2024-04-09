package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import no.nav.pim.primbrukerstyring.util.StringToListConverter;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.*;

@Entity
@Table(name = "bruker")
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Bruker {

    public Bruker() {}

    @Id
    @Column
    @NotNull
    private String ident;

    @Column
    @Enumerated(EnumType.STRING)
    private Rolle rolle;

    @Column
    private String navn;

    @Column(name = "tilganger")
    @Convert(converter = StringToListConverter.class)
    private List<String> tilganger;

    @ManyToOne(fetch=FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "FK_representert_leder_id", referencedColumnName = "leder_id")
    private Leder representertLeder;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date sist_aksessert;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bruker bruker)) return false;
        return Objects.equals(ident, bruker.ident) && rolle == bruker.rolle && Objects.equals(navn, bruker.navn) && Objects.equals(tilganger, bruker.tilganger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident, rolle, navn, tilganger);
    }
}
