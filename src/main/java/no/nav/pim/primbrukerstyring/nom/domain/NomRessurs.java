package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

import java.util.List;

@Data
public class NomRessurs {
    String navident;
    String visningsnavn;
    List<NomLederFor> lederFor;
}
