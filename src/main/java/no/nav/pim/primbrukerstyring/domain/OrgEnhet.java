package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.pim.primbrukerstyring.nom.domain.NomOrgEnhet;


@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrgEnhet implements Comparable<OrgEnhet> {

    static public OrgEnhet fraNomOrgenhet(NomOrgEnhet orgEnhet) {
        return OrgEnhet.builder().navn(orgEnhet.getNavn()).nomId(orgEnhet.getId()).build();
    }

    @Column(name = "nom_id")
    private String nomId;

    @Column(name = "navn")
    private String navn;

    @Override
    public int compareTo(OrgEnhet orgEnhet) {
        return this.navn.compareTo(orgEnhet.navn);
    }
}
