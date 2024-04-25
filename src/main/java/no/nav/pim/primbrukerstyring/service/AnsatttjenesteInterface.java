package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.domain.Ansatt;
import no.nav.pim.primbrukerstyring.domain.OverstyrendeLeder;
import no.nav.pim.primbrukerstyring.service.dto.OverstyrendeLederDto;

import java.util.List;

public interface AnsatttjenesteInterface {

    Ansatt hentAnsatt(String authorization, String ident);

    OverstyrendeLeder leggTilOverstyrendeLeder(String authorization, OverstyrendeLederDto overstyrendeLederDto);

    List<OverstyrendeLeder> hentAlleOverstyrendeLedere(String authorization);
}
