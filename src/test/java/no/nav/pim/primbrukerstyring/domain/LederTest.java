package no.nav.pim.primbrukerstyring.domain;


import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;
import no.nav.pim.primbrukerstyring.nom.domain.NomTelefon;
import no.nav.pim.primbrukerstyring.nom.domain.NomTelefonType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static no.nav.pim.primbrukerstyring.utils.NomUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class LederTest {

    @Test
    public void hentLederFraNomSetterRiktigTelefonnummer() {
        NomTelefon privat = lagNomTelefon(NomTelefonType.PRIVAT_TELEFON);
        NomTelefon tjeneste = lagNomTelefon(NomTelefonType.NAV_TJENESTE_TELEFON);
        NomTelefon kontor = lagNomTelefon(NomTelefonType.NAV_KONTOR_TELEFON);

        NomRessurs nomRessursAlleTelefoner = lagNomRessurs(null, List.of(privat, tjeneste, kontor), null, null);
        NomRessurs nomRessursUtenTjenesteTelefon = lagNomRessurs(null, List.of(privat, kontor), null, null);
        NomRessurs nomRessursBarePrivatTelefon = lagNomRessurs(null, List.of(privat), null, null);

        assertThat(Leder.fraNomRessurs(nomRessursAlleTelefoner).getTlf(), is(tjeneste.getNummer()));
        assertThat(Leder.fraNomRessurs(nomRessursUtenTjenesteTelefon).getTlf(), is(kontor.getNummer()));
        assertNull(Leder.fraNomRessurs(nomRessursBarePrivatTelefon).getTlf());
    }

    @Test
    public void hentLederFraNomSetterDirektoratslederRiktig() {
        NomRessurs nomRessursDirektoratleder = lagNomRessurs(null, null, List.of(lagNomLederFor(true)), null);
        NomRessurs nomRessursIkkeDirektoratleder = lagNomRessurs(null, null, List.of(lagNomLederFor(false)), null);

        assertTrue(Leder.fraNomRessurs(nomRessursDirektoratleder).getErDirektoratsleder());
        assertFalse(Leder.fraNomRessurs(nomRessursIkkeDirektoratleder).getErDirektoratsleder());
    }
}
