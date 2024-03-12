package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.domain.BrukerRolle;
import no.nav.pim.primbrukerstyring.domain.Rolle;
import no.nav.pim.primbrukerstyring.nom.domain.Ressurs;

import java.util.List;

public interface BrukertjenesteInterface {

    Rolle hentBrukerRolle(String authorization);

    BrukerRolle leggTilBrukerRolle(String authorization, BrukerRolle brukerRolle);

    BrukerRolle endreBrukerRolle(String authorization, String ident, BrukerRolle brukerRolle);

    void slettBrukerRolle(String authorization, String ident);

    List<BrukerRolle> hentAlleHRMedarbeidere(String authorization);

    List<Ressurs> hentLedersRessurser(String authorization, String ident);
}
