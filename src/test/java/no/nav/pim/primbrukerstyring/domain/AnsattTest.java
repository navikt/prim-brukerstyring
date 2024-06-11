package no.nav.pim.primbrukerstyring.domain;


import no.nav.pim.primbrukerstyring.nom.domain.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class AnsattTest {

    /*@Test
    public void hentAnsattFraNomSetterOverstyrtLederForst() {
        NomRessurs nomLederRessurs = new NomRessurs("T123456", "Test Testesen", "Test@Testesen.no",
                List.of(), List.of(new NomLederFor(new NomOrgEnhet("aa000a", "Test Org", ""))), List.of(NomSektor.NAV_STATLIG), List.of());

        NomRessurs nomAnsattRessurs = new NomRessurs("T123456", "Test Testesen", "Test@Testesen.no",
                List.of(), List.of(), List.of(NomSektor.NAV_STATLIG), List.of(new NomLeder(true, nomLederRessurs)));

        NomRessurs nomOverstyrtLederRessurs = new NomRessurs("O123456", "Overstyrt Leder", "Overstyrt@Leder.no",
                List.of(), List.of(new NomLederFor(new NomOrgEnhet("aa000a", "Test Org", ""))), List.of(NomSektor.NAV_STATLIG), List.of());


        AnsattStillingsavtale ansattStillingsavtale = AnsattStillingsavtale.fraOverstyrendeLeder(
            OverstyrendeLeder.builder()
                .ansattIdent(nomAnsattRessurs.getNavident())
                .ansattNavn(nomAnsattRessurs.getVisningsnavn())
                .fra(new Date())
                .overstyrendeLeder(Leder.fraNomRessurs(nomOverstyrtLederRessurs))
                .build()
        );

        Ansatt overstyrtAnsatt = Ansatt.fraNomRessurs(nomAnsattRessurs, ansattStillingsavtale);
        assertThat(overstyrtAnsatt.getStillingsavtaler().size(), is(2));
        Optional<AnsattStillingsavtale> direkteRapporterende = overstyrtAnsatt.getStillingsavtaler().stream().filter(as -> as.getStillingsavtale().equals(Stillingsavtale.DR)).findFirst();
        assertTrue(direkteRapporterende.isPresent());
        assertThat(direkteRapporterende.get().getLeder().getIdent(), is(nomOverstyrtLederRessurs.getNavident()));
        Optional<AnsattStillingsavtale> ikkeDirekteRapporterende = overstyrtAnsatt.getStillingsavtaler().stream().filter(as -> as.getStillingsavtale().equals(Stillingsavtale.MR)).findFirst();
        assertTrue(ikkeDirekteRapporterende.isPresent());
        assertThat(ikkeDirekteRapporterende.get().getLeder().getIdent(), is(nomLederRessurs.getNavident()));

    }*/
}
