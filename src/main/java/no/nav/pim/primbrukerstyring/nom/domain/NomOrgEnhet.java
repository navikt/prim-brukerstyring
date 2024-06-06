package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NomOrgEnhet {
    String id;
    String navn;
    String orgEnhetsType;
    List<NomKobling> koblinger;
    List<NomOrganisering> organiseringer;
    List<NomLeder> leder;
}
