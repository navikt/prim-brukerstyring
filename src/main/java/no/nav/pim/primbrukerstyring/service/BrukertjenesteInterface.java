package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.domain.BrukerRolle;
import no.nav.pim.primbrukerstyring.domain.Rolle;

import java.util.List;

public interface BrukertjenesteInterface {

    Rolle hentBrukerRolle(String authorization);

    BrukerRolle leggTilBrukerRolle(String authorization, BrukerRolle brukerRolle);

    BrukerRolle endreBrukerRolle(String authorization, String ident, Rolle rolle);

    void slettBrukerRolle(String authorization, String ident);

    List<BrukerRolle> hentAlleBrukereMedRolle(String authorization, Rolle rolle);
}