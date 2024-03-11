package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class Leder {
    String navident;
    String visningsnavn;
    List<LederFor> lederFor;
}
