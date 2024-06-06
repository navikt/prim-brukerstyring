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
        NomTelefon privat = NomTelefon.builder().nummer("11111111").type(NomTelefonType.PRIVAT_TELEFON).build();
        NomTelefon tjeneste = NomTelefon.builder().nummer("22222222").type(NomTelefonType.NAV_TJENESTE_TELEFON).build();
        NomTelefon kontor = NomTelefon.builder().nummer("33333333").type(NomTelefonType.NAV_KONTOR_TELEFON).build();
        NomRessurs nomRessursAlleTelefoner = NomRessurs.builder()
            .navident("T123456")
            .visningsnavn("Test Testesen")
            .epost("Test@Testesen.no")
            .telefon(List.of(privat, tjeneste, kontor))
            .sektor(List.of(NomSektor.NAV_STATLIG))
            .lederFor(List.of(
                NomLederFor.builder().orgEnhet(NomOrgEnhet.builder().id("aa000a").navn("Test Org").build()).build()
            ))
            .build();

        NomRessurs nomRessursUtenTjenesteTelefon = NomRessurs.builder()
                .navident("T123456")
                .visningsnavn("Test Testesen")
                .epost("Test@Testesen.no")
                .telefon(List.of(privat, kontor))
                .sektor(List.of(NomSektor.NAV_STATLIG))
                .lederFor(List.of(
                        NomLederFor.builder().orgEnhet(NomOrgEnhet.builder().id("aa000a").navn("Test Org").build()).build()
                ))
                .build();

        NomRessurs nomRessursBarePrivatTelefon = NomRessurs.builder()
                .navident("T123456")
                .visningsnavn("Test Testesen")
                .epost("Test@Testesen.no")
                .telefon(List.of(privat))
                .sektor(List.of(NomSektor.NAV_STATLIG))
                .lederFor(List.of(
                        NomLederFor.builder().orgEnhet(NomOrgEnhet.builder().id("aa000a").navn("Test Org").build()).build()
                ))
                .build();

        assertThat(Leder.fraNomRessurs(nomRessursAlleTelefoner).getTlf(), is(tjeneste.getNummer()));
        assertThat(Leder.fraNomRessurs(nomRessursUtenTjenesteTelefon).getTlf(), is(kontor.getNummer()));
        assertNull(Leder.fraNomRessurs(nomRessursBarePrivatTelefon).getTlf());
    }

    @Test
    public void hentLederFraNomSetterDirektoratslederRiktig() {
        NomRessurs nomRessursDirektoratleder = NomRessurs.builder()
                .navident("T123456")
                .visningsnavn("Test Testesen")
                .epost("Test@Testesen.no")
                .telefon(List.of())
                .sektor(List.of(NomSektor.NAV_STATLIG))
                .lederFor(List.of(
                        NomLederFor.builder().orgEnhet(NomOrgEnhet.builder().id("aa000a").navn("Test Org").orgEnhetsType("DIREKTORAT").build()).build()
                ))
                .build();
        NomRessurs nomRessursIkkeDirektoratleder = NomRessurs.builder()
                .navident("T123456")
                .visningsnavn("Test Testesen")
                .epost("Test@Testesen.no")
                .telefon(List.of())
                .sektor(List.of(NomSektor.NAV_STATLIG))
                .lederFor(List.of(
                        NomLederFor.builder().orgEnhet(NomOrgEnhet.builder().id("aa000a").navn("Test Org").orgEnhetsType("DIR").build()).build()
                ))
                .build();

        assertTrue(Leder.fraNomRessurs(nomRessursDirektoratleder).getErDirektoratsleder());
        assertFalse(Leder.fraNomRessurs(nomRessursIkkeDirektoratleder).getErDirektoratsleder());
    }
}
