package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NomLeder {
    Boolean erDagligOppfolging;
    NomRessurs ressurs;
}
