package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.domain.Ansatt;
import no.nav.pim.primbrukerstyring.domain.Bruker;
import no.nav.pim.primbrukerstyring.domain.Leder;
import no.nav.pim.primbrukerstyring.service.dto.BrukerDto;

import java.util.List;

public interface BrukertjenesteInterface {

    BrukerDto hentBruker(String authorization);

    Bruker leggTilBrukerRolle(String authorization, Bruker bruker);

    Bruker endreBrukerRolle(String authorization, String ident, Bruker bruker);

    Bruker endreBrukerTilganger(String authorization, String ident, Bruker bruker);

    void slettBrukerRolle(String authorization, String ident);

    List<Bruker> hentAlleHRMedarbeidere(String authorization);

    List<Ansatt> hentLedersRessurser(String authorization, String lederIdent);

    List<Leder> hentLedere(String authorization);

    Leder validerLeder(String authorization, String lederIdent);
}
