package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NomLeder {
    Boolean erDagligOppfolging;
    NomRessurs ressurs;
}
