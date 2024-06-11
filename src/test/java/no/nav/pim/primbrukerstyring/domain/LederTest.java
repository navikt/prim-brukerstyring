package no.nav.pim.primbrukerstyring.domain;


import no.nav.pim.primbrukerstyring.nom.domain.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class LederTest {

    @Test
    public void hentLederFraNomSetterRiktigTelefonnummer() {
        NomTelefon privat = new NomTelefon("11111111", NomTelefonType.PRIVAT_TELEFON);
        NomTelefon tjeneste = new NomTelefon("22222222", NomTelefonType.NAV_TJENESTE_TELEFON);
        NomTelefon kontor = new NomTelefon("33333333", NomTelefonType.NAV_KONTOR_TELEFON);

        NomRessurs nomRessursAlleTelefoner = new NomRessurs("T123456", "Test Testesen", "Test@Testesen.no",
                List.of(privat, tjeneste, kontor), List.of(new NomLederFor(new NomOrgEnhet("aa000a", "Test Org", ""))), List.of(NomSektor.NAV_STATLIG), List.of());

        NomRessurs nomRessursUtenTjenesteTelefon = new NomRessurs("T123456", "Test Testesen", "Test@Testesen.no",
                List.of(privat, kontor), List.of(new NomLederFor(new NomOrgEnhet("aa000a", "Test Org", ""))), List.of(NomSektor.NAV_STATLIG), List.of());

        NomRessurs nomRessursBarePrivatTelefon = new NomRessurs("T123456", "Test Testesen", "Test@Testesen.no",
                List.of(privat), List.of(new NomLederFor(new NomOrgEnhet("aa000a", "Test Org", ""))), List.of(NomSektor.NAV_STATLIG), List.of());

        assertThat(Leder.fraNomRessurs(nomRessursAlleTelefoner).getTlf(), is(tjeneste.getNummer()));
        assertThat(Leder.fraNomRessurs(nomRessursUtenTjenesteTelefon).getTlf(), is(kontor.getNummer()));
        assertNull(Leder.fraNomRessurs(nomRessursBarePrivatTelefon).getTlf());
    }

    @Test
    public void hentLederFraNomSetterDirektoratslederRiktig() {
        NomRessurs nomRessursDirektoratleder = new NomRessurs("T123456", "Test Testesen", "Test@Testesen.no",
                List.of(), List.of(new NomLederFor(new NomOrgEnhet("aa000a", "Test Org", "DIREKTORAT"))), List.of(NomSektor.NAV_STATLIG), List.of());

        NomRessurs nomRessursIkkeDirektoratleder = new NomRessurs("T123456", "Test Testesen", "Test@Testesen.no",
                List.of(), List.of(new NomLederFor(new NomOrgEnhet("aa000a", "Test Org", "DIR"))), List.of(NomSektor.NAV_STATLIG), List.of());

        assertTrue(Leder.fraNomRessurs(nomRessursDirektoratleder).getErDirektoratsleder());
        assertFalse(Leder.fraNomRessurs(nomRessursIkkeDirektoratleder).getErDirektoratsleder());
    }
}
