package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.domain.Bruker;
import no.nav.pim.primbrukerstyring.domain.Rolle;
import no.nav.pim.primbrukerstyring.nom.domain.Ressurs;

import java.util.List;

public interface BrukertjenesteInterface {

    Rolle hentBrukerRolle(String authorization);

    Bruker leggTilBrukerRolle(String authorization, Bruker bruker);

    Bruker endreBrukerRolle(String authorization, String ident, Bruker bruker);

    Bruker endreBrukerTilganger(String authorization, String ident, Bruker bruker);

    void slettBrukerRolle(String authorization, String ident);

    List<Bruker> hentAlleHRMedarbeidere(String authorization);

    List<Ressurs> hentLedersRessurser(String authorization, String ident);
}
