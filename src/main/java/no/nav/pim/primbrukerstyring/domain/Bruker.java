package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import no.nav.pim.primbrukerstyring.util.StringToListConverter;
import org.springframework.data.annotation.CreatedDate;
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

    @JsonBackReference
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinTable(
        name = "bruker_leder",
        joinColumns = @JoinColumn(name = "ident"),
        inverseJoinColumns = @JoinColumn(name = "leder_id"))
    private Set<Leder> ledere = new HashSet<>();

    @Column
    private boolean sluttet = false;

    @Column
    private boolean endretEnhet = false;

    @Column
    private String enhet;

    @Column(name = "sist_aksessert")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sistAksessert;

    @Column(name = "sist_endret")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sistEndret;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Date updated;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date created;


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
