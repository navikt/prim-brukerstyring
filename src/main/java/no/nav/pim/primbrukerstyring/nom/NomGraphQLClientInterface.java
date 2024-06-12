package no.nav.pim.primbrukerstyring.nom;

import no.nav.pim.primbrukerstyring.nom.domain.NomOrgEnhet;
import no.nav.pim.primbrukerstyring.nom.domain.NomOrganisering;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;

import java.util.List;

public interface NomGraphQLClientInterface {

    NomRessurs getLedersResurser(String authorization, String navident);

    NomRessurs getRessurs(String authorization, String navident);

    List<NomOrganisering> getOrganisasjonstre(String authorization);

    NomOrgEnhet hentOrganisasjoner(String authorization, String organisasjonsId);

}
