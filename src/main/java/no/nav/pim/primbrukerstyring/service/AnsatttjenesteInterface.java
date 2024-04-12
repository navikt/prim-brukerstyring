package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.domain.Ansatt;

public interface AnsatttjenesteInterface {

    Ansatt hentAnsatt(String authorization, String ident);

}
