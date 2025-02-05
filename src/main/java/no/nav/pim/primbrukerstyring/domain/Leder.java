package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;
import no.nav.pim.primbrukerstyring.nom.domain.NomTelefon;
import no.nav.pim.primbrukerstyring.nom.domain.NomTelefonType;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "leder")
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Leder {

    static public Leder fraNomRessurs(NomRessurs ressurs) {
        Set<OrgEnhet> orgEnheter;
        if (ressurs.getLederFor() != null) {
            orgEnheter = ressurs.getLederFor().stream().map((lederFor -> OrgEnhet.fraNomOrgenhet(lederFor.getOrgEnhet()))).collect(Collectors.toSet());
        } else {
            orgEnheter = new HashSet<>();
        }

        Optional<String> telefonnummer = ressurs.getTelefon().stream()
                .filter(nomTelefon -> !nomTelefon.getType().equals(NomTelefonType.PRIVAT_TELEFON))
                .sorted()
                .map(NomTelefon::getNummer)
                .findFirst();

        return Leder.builder()
                .ident(ressurs.getNavident())
                .navn(ressurs.getVisningsnavn())
                .email(ressurs.getEpost())
                .tlf(telefonnummer.orElse(null))
                .orgEnheter(orgEnheter)
                .sluttet(ressurs.getSluttdato() != null && ressurs.getSluttdato().after(new Date()))
                .erDirektoratsleder(ressurs.getLederFor() != null && ressurs.getLederFor().stream().anyMatch((nomLederFor -> nomLederFor.getOrgEnhet().getOrgEnhetsType() != null && nomLederFor.getOrgEnhet().getOrgEnhetsType().equals("DIREKTORAT"))))
                .build();
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

    @Column
    private String email;

    @Column
    private String tlf;

    @Column
    @NotNull
    private Boolean erDirektoratsleder;

    @Column
    private Boolean sluttet;

    @JsonManagedReference
    @ManyToMany(mappedBy = "ledere")
    private Set<Bruker> brukere = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "orgenhet")
    private Set<OrgEnhet> orgEnheter = new HashSet<>();

    public Leder oppdaterMed(Leder oppdatertLeder) {
        this.navn = oppdatertLeder.getNavn();
        this.email = oppdatertLeder.getEmail();
        this.tlf = oppdatertLeder.getTlf();
        this.erDirektoratsleder = oppdatertLeder.getErDirektoratsleder();
        this.orgEnheter = oppdatertLeder.getOrgEnheter();
        this.sluttet = oppdatertLeder.getSluttet();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Leder leder)) return false;
        return Objects.equals(lederId, leder.lederId) && Objects.equals(ident, leder.ident) && Objects.equals(navn, leder.navn) && Objects.equals(erDirektoratsleder, leder.erDirektoratsleder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lederId, ident, navn, erDirektoratsleder);
    }
}
