package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

import java.util.List;

@Data
public class NomOrgEnhet {
    String id;
    String navn;
    String orgEnhetsType;
    List<NomKobling> koblinger;
    List<NomOrganisering> organiseringer;
    List<NomLeder> leder;

    /*public NomOrgEnhet(String id, String navn, String orgEnhetsType) {
        this.id = id;
        this.navn = navn;
        this.orgEnhetsType = orgEnhetsType;
    }

    public NomOrgEnhet(String id, String navn, List<NomKobling> koblinger, List<NomOrganisering> organiseringer) {
        this.id = id;
        this.navn = navn;
        this.koblinger = koblinger;
        this.organiseringer = organiseringer;
    }

    public NomOrgEnhet(String id, String navn, List<NomLeder> leder) {
        this.id = id;
        this.navn = navn;
        this.leder = leder;
    }*/
}
