package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

import java.util.List;

@Data
public class Ressurs {
    String navident;
    String visningsnavn;
    List<LederFor> lederFor;
}
