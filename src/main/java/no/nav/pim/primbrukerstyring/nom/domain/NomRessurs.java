package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class NomRessurs {
    String navident;
    String visningsnavn;
    String epost;
    Date sluttdato;
    NomIdentType identType;
    List<NomTelefon> telefon;
    List<NomLederFor> lederFor;
    NomSektor gjeldendeSektor;
    List<NomLeder> ledere;
    List<NomOrgTilknytning> orgTilknytninger;
}
