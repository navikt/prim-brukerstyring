package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

import java.util.List;

@Data
public class OrgEnhet {
    List<Kobling> koblinger;
    List<Organisering> organiseringer;
    List<Leder> leder;
}
