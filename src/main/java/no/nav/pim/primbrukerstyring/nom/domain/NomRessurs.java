package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NomRessurs {
    String navident;
    String visningsnavn;
    String epost;
    List<NomTelefon> telefon;
    List<NomLederFor> lederFor;
    List<NomSektor> sektor;
    List<NomLeder> ledere;
}
