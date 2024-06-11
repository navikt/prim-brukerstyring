package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

import java.util.List;

@Data
public class NomRessurs {
    String navident;
    String visningsnavn;
    String epost;
    List<NomTelefon> telefon;
    List<NomLederFor> lederFor;
    List<NomSektor> sektor;
    List<NomLeder> ledere;

    public NomRessurs(String navident, String visningsnavn, String epost, List<NomTelefon> telefon, List<NomLederFor> lederFor, List<NomSektor> sektor, List<NomLeder> ledere) {
        this.navident = navident;
        this.visningsnavn = visningsnavn;
        this.epost = epost;
        this.telefon = telefon;
        this.lederFor = lederFor;
        this.sektor = sektor;
        this.ledere = ledere;
    }
}
