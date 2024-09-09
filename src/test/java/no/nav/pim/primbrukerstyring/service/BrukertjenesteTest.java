package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.pim.primbrukerstyring.domain.Bruker;
import no.nav.pim.primbrukerstyring.domain.Leder;
import no.nav.pim.primbrukerstyring.domain.OverstyrendeLeder;
import no.nav.pim.primbrukerstyring.domain.Rolle;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.*;
import no.nav.pim.primbrukerstyring.repository.BrukerRepository;
import no.nav.pim.primbrukerstyring.repository.DriftOgVedlikeholdRepository;
import no.nav.pim.primbrukerstyring.repository.LederRepository;
import no.nav.pim.primbrukerstyring.repository.OverstyrendeLederRepository;
import no.nav.pim.primbrukerstyring.util.OIDCUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static no.nav.pim.primbrukerstyring.utils.NomUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(Brukertjeneste.class)
public class BrukertjenesteTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    MeterRegistry metricsRegistry;

    @MockBean
    Ansatttjeneste ansatttjeneste;

    @MockBean
    BrukerRepository brukerrepository;

    @MockBean
    LederRepository lederrepository;

    @MockBean
    DriftOgVedlikeholdRepository driftOgVedlikeholdRepository;

    @MockBean
    OverstyrendeLederRepository overstyrendelederrepository;

    @MockBean
    OIDCUtil oidcUtil;

    @MockBean
    NomGraphQLClient nomGraphQLClient;

    private String ident;

    @Before
    public void setUp() {
        ident = "T123456";
        given(oidcUtil.finnClaimFraOIDCToken(anyString(), anyString())).willReturn(Optional.of(ident));
        given(metricsRegistry.counter(anyString(), anyString(), anyString(), anyString(), anyString())).willReturn(mock(Counter.class));
    }

    @Test
    public void hentBrukerForRessursMedLederForSetterStatusLeder() throws Exception {

        NomRessurs nomRessursLeder = lagNomRessurs(ident, null, List.of(lagNomLederFor(false)), null);

        given(brukerrepository.findByIdent(anyString())).willReturn(Optional.empty());
        given(nomGraphQLClient.getLedersResurser(any(), anyString())).willReturn(nomRessursLeder);
        given(overstyrendelederrepository.findByOverstyrendeLeder_IdentAndTilIsNull(anyString())).willReturn(List.of());
        given(brukerrepository.save(any())).willAnswer(i -> i.getArguments()[0]);


        mvc.perform(get("/bruker")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolle").value(Rolle.LEDER.toString()));
    }

    @Test
    public void hentBrukerForRessursUtenLederForSetterStatusMedarbeider() throws Exception {
        NomRessurs nomRessursLeder = lagNomRessurs(null, null, null, null);

        given(brukerrepository.findByIdent(anyString())).willReturn(Optional.empty());
        given(nomGraphQLClient.getLedersResurser(any(), anyString())).willReturn(nomRessursLeder);
        given(overstyrendelederrepository.findByOverstyrendeLeder_IdentAndTilIsNull(anyString())).willReturn(List.of());
        given(brukerrepository.save(any())).willAnswer(i -> i.getArguments()[0]);


        mvc.perform(get("/bruker")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolle").value(Rolle.MEDARBEIDER.toString()));
    }

    @Test
    public void hentBrukerForRessursMedHRMedarbeiderMedValgtLederReturnererLeder() throws Exception {
        Leder leder = Leder.builder()
                .ident("A123456")
                .navn("Test Testesen")
                .build();
        Bruker bruker = Bruker.builder()
                .ident("T123456")
                .navn("Test Testesen")
                .rolle(Rolle.HR_MEDARBEIDER)
                .tilganger(List.of())
                .ledere(Set.of(leder))
                .sistAksessert(new Date())
                .build();

        given(brukerrepository.findByIdent(anyString())).willReturn(Optional.of(bruker));
        given(brukerrepository.save(any())).willAnswer(i -> i.getArguments()[0]);


        mvc.perform(get("/bruker")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolle").value(Rolle.HR_MEDARBEIDER.toString()));
    }

    @Test
    public void hentLedersRessurserReturnererKoblingerOgLedereForOrganisasjoner() throws Exception {
        NomRessurs nomRessursLeder = lagNomRessurs(ident, null, null, null);
        NomRessurs ansatt1 = lagNomRessurs(null, null, null, List.of(lagNomLeder(true, nomRessursLeder)));
        NomRessurs ansatt2 = lagNomRessurs(null, null, null, List.of(lagNomLeder(true, nomRessursLeder)));

        NomRessurs nomRessursLederMedAnsatte = kopierRessurs(nomRessursLeder);
        nomRessursLederMedAnsatte.setLederFor(
            List.of(lagNomLederFor(
                true,
                List.of(lagNomKobling(ansatt1)),
                List.of(lagNomOrganisering(lagNomLeder(true, ansatt2))),
                null
            ))
        );

        Leder leder = Leder.builder()
                .ident(nomRessursLeder.getNavident())
                .navn(nomRessursLeder.getVisningsnavn())
                .build();
        Bruker bruker = Bruker.builder()
                .ident(nomRessursLeder.getNavident())
                .navn(nomRessursLeder.getVisningsnavn())
                .rolle(Rolle.LEDER)
                .ledere(Set.of(leder))
                .sistAksessert(new Date())
                .build();

        given(brukerrepository.findByIdent(anyString())).willReturn(Optional.of(bruker));
        given(lederrepository.findByIdent(anyString())).willReturn(Optional.of(leder));
        given(nomGraphQLClient.getLedersResurser(anyString(), anyString())).willReturn(nomRessursLederMedAnsatte);
        given(overstyrendelederrepository.findByAnsattIdentAndTilIsNull(anyString())).willReturn(Optional.empty());
        given(overstyrendelederrepository.findByOverstyrendeLeder_IdentAndTilIsNull(anyString())).willReturn(List.of());

        mvc.perform(get("/bruker/leder/" + bruker.getIdent() + "/ressurser")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].ident").value(containsInAnyOrder(ansatt1.getNavident(), ansatt2.getNavident())));
    }

    @Test
    public void hentLedersRessurserReturnererOverstyrteLedere() throws Exception {
        NomRessurs nomRessursLeder = lagNomRessurs(ident, null, null, null);
        NomRessurs ansatt1 = lagNomRessurs(null, null, null, List.of(lagNomLeder(true, nomRessursLeder)));
        NomRessurs ansatt2 = lagNomRessurs(null, null, null, List.of(lagNomLeder(true, nomRessursLeder)));

        NomRessurs nomRessursLederMedAnsatte = kopierRessurs(nomRessursLeder);
        nomRessursLederMedAnsatte.setLederFor(
            List.of(lagNomLederFor(
                false,
                List.of(lagNomKobling(ansatt1)),
                List.of(lagNomOrganisering(lagNomLeder(true, ansatt2))),
                null
            ))
        );

        Leder leder = Leder.builder()
                .ident(nomRessursLeder.getNavident())
                .navn(nomRessursLeder.getVisningsnavn())
                .build();
        Bruker bruker = Bruker.builder()
                .ident(nomRessursLeder.getNavident())
                .navn(nomRessursLeder.getVisningsnavn())
                .rolle(Rolle.LEDER)
                .sistAksessert(new Date())
                .ledere(Set.of(leder))
                .build();

        OverstyrendeLeder overstyrendeLeder = OverstyrendeLeder.builder()
                .ansattIdent(ansatt1.getNavident())
                .ansattNavn(ansatt1.getVisningsnavn())
                .fra(new Date())
                .overstyrendeLeder(Leder.builder().erDirektoratsleder(true).ident("O123456").navn("Overstyrende Leder").build())
                .build();

        given(brukerrepository.findByIdent(anyString())).willReturn(Optional.of(bruker));
        given(lederrepository.findByIdent(anyString())).willReturn(Optional.of(leder));
        given(nomGraphQLClient.getLedersResurser(anyString(), anyString())).willReturn(nomRessursLederMedAnsatte);
        given(overstyrendelederrepository.findByAnsattIdentAndTilIsNull(overstyrendeLeder.getAnsattIdent())).willReturn(Optional.of(overstyrendeLeder));
        given(overstyrendelederrepository.findByOverstyrendeLeder_IdentAndTilIsNull(anyString())).willReturn(List.of());

        mvc.perform(get("/bruker/leder/" + bruker.getIdent() + "/ressurser")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
                //.andExpect(jsonPath("$[*].stillingsavtaler[*].stillingsavtale").value(containsInAnyOrder(List.of("DR"), List.of("DR", "MR"))));
    }

    @Configuration
    @ComponentScan(basePackageClasses = {Brukertjeneste.class})
    public static class TestConf {
    }

}
