package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

import java.util.List;

@Data
public class NomOrgEnhet {
    String id;
    String navn;
    String orgEnhetsType;
    List<NomKobling> orgTilknytninger;
    List<NomOrganisering> organiseringer;
    List<NomLeder> leder;
}
