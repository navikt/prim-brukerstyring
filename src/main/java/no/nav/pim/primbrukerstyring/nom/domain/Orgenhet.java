package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

import java.util.List;

@Data
public class Orgenhet {
    List<Kobling> koblinger;
}
