package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

import java.util.Date;

@Data
public class NomLeder {
    Boolean erDagligOppfolging;
    Date gyldigTom;
    NomRessurs ressurs;
}
