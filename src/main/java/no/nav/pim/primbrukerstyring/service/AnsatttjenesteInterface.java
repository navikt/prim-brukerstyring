package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.domain.Ansatt;
import no.nav.pim.primbrukerstyring.domain.EnkelAnsattInfo;
import no.nav.pim.primbrukerstyring.domain.OverstyrendeLeder;
import no.nav.pim.primbrukerstyring.service.dto.OverstyrendeLederDto;

import java.util.List;

public interface AnsatttjenesteInterface {

    EnkelAnsattInfo hentEnkelAnsattInfo(String authorization, String ident);

    Ansatt hentAnsatt(String authorization, String ident);

    OverstyrendeLeder leggTilOverstyrendeLeder(String authorization, OverstyrendeLederDto overstyrendeLederDto);

    OverstyrendeLeder fjernOverstyrendeLeder(String authorization, String ansattIdent);

    List<OverstyrendeLeder> hentAlleOverstyrendeLedere(String authorization);

    List<OverstyrendeLeder> hentAlleInaktiveOverstyrendeLedere(String authorization);
}
