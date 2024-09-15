package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

import java.util.Date;

@Data
public class NomOrgTilknytning {
    Boolean erDagligOppfolging;
    NomOrgEnhet orgEnhet;
    Date gyldigTom;
}
