package no.nav.pim.primbrukerstyring.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.pim.primbrukerstyring.domain.Bruker;
import no.nav.pim.primbrukerstyring.domain.Leder;
import no.nav.pim.primbrukerstyring.domain.OverstyrendeLeder;
import no.nav.pim.primbrukerstyring.domain.Rolle;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClientInterface;
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
    NomGraphQLClientInterface nomGraphQLClient;

    @Before
    public void setUp() {
        given(oidcUtil.finnClaimFraOIDCToken(anyString(), anyString())).willReturn(Optional.of("T123456"));
        given(metricsRegistry.counter(anyString(), anyString(), anyString(), anyString(), anyString())).willReturn(mock(Counter.class));
    }

    @Test
    public void hentBrukerForRessursMedLederForSetterStatusLeder() throws Exception {
        NomRessurs nomRessursLeder = new NomRessurs("T123456", "Test Testesen", "Test@Testesen.no",
                List.of(), List.of(new NomLederFor(new NomOrgEnhet("aa000a", "Test Org", "", List.of(), List.of(), List.of()))), List.of(NomSektor.NAV_STATLIG), List.of());

        given(brukerrepository.findByIdent(anyString())).willReturn(Optional.empty());
        given(nomGraphQLClient.getLedersResurser(any(), anyString())).willReturn(nomRessursLeder);
        given(overstyrendelederrepository.findByOverstyrendeLeder_IdentAndTilIsNull(anyString())).willReturn(List.of());
        given(brukerrepository.save(any())).willAnswer(i -> i.getArguments()[0]);


        mvc.perform(get("/bruker")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolle").value(Rolle.LEDER.toString()))
                .andExpect(jsonPath("$.representertLeder.ident").value(nomRessursLeder.getNavident()));
    }

    @Test
    public void hentBrukerForRessursUtenLederForSetterStatusMedarbeider() throws Exception {
        NomRessurs nomRessursLeder = new NomRessurs("T123456", "Test Testesen", "Test@Testesen.no",
                List.of(), List.of(), List.of(NomSektor.NAV_STATLIG), List.of());

        given(brukerrepository.findByIdent(anyString())).willReturn(Optional.empty());
        given(nomGraphQLClient.getLedersResurser(any(), anyString())).willReturn(nomRessursLeder);
        given(overstyrendelederrepository.findByOverstyrendeLeder_IdentAndTilIsNull(anyString())).willReturn(List.of());
        given(brukerrepository.save(any())).willAnswer(i -> i.getArguments()[0]);


        mvc.perform(get("/bruker")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolle").value(Rolle.MEDARBEIDER.toString()))
                .andExpect(jsonPath("$.representertLeder").isEmpty());
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
                .sistAksessert(new Date())
                .representertLeder(leder)
                .build();

        given(brukerrepository.findByIdent(anyString())).willReturn(Optional.of(bruker));
        given(brukerrepository.save(any())).willAnswer(i -> i.getArguments()[0]);


        mvc.perform(get("/bruker")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolle").value(Rolle.HR_MEDARBEIDER.toString()))
                .andExpect(jsonPath("$.representertLeder.ident").value(leder.getIdent()));
    }

    @Test
    public void hentBrukerForRessursMedHRMedarbeiderMedUtgattLederReturnererNull() throws Exception {
        Leder leder = Leder.builder()
                .ident("A123456")
                .navn("Test Testesen")
                .build();
        Bruker bruker = Bruker.builder()
                .ident("T123456")
                .navn("Test Testesen")
                .rolle(Rolle.HR_MEDARBEIDER)
                .tilganger(List.of())
                .sistAksessert(Date.from(new Date().toInstant().minusSeconds(2 * 3600)))
                .representertLeder(leder)
                .build();

        given(brukerrepository.findByIdent(anyString())).willReturn(Optional.of(bruker));
        given(brukerrepository.save(any())).willAnswer(i -> i.getArguments()[0]);


        mvc.perform(get("/bruker")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolle").value(Rolle.HR_MEDARBEIDER.toString()))
                .andExpect(jsonPath("$.representertLeder").isEmpty());
    }

    @Test
    public void hentLedersRessurserReturnererKoblingerOgLedereForOrganisasjoner() throws Exception {
        NomRessurs nomRessursLeder = new NomRessurs("T123456", "Test Testesen", "Test@Testesen.no",
                List.of(), List.of(), List.of(NomSektor.NAV_STATLIG), List.of());

        NomRessurs ansatt1 = new NomRessurs("A111111", "Anders And", "Test@Testesen.no",
                List.of(), List.of(), List.of(NomSektor.NAV_STATLIG), List.of(new NomLeder(true, nomRessursLeder)));

        NomRessurs ansatt2 = new NomRessurs("B222222", "Bjornar Bjorn", "Test@Testesen.no",
                List.of(), List.of(), List.of(NomSektor.NAV_STATLIG), List.of(new NomLeder(true, nomRessursLeder)));

        NomRessurs nomRessursLederMedAnsatte = nyRessurs(nomRessursLeder);
        nomRessursLederMedAnsatte.setLederFor(List.of(
            new NomLederFor(
                new NomOrgEnhet("aa000a", "Test Org", "", List.of(new NomKobling(ansatt1)), List.of(
                    new NomOrganisering(
                        new NomOrgEnhet("aa000a", "Test Org", "", List.of(), List.of(), List.of(new NomLeder(true, ansatt2)))
                    )
                ), List.of())
            )
        ));

        Leder leder = Leder.builder()
                .ident("A123456")
                .navn("Test Testesen")
                .build();
        Bruker bruker = Bruker.builder()
                .ident("T123456")
                .navn("Test Testesen")
                .rolle(Rolle.LEDER)
                .sistAksessert(new Date())
                .representertLeder(leder)
                .build();

        given(brukerrepository.findByIdent(anyString())).willReturn(Optional.of(bruker));
        given(nomGraphQLClient.getLedersResurser(anyString(), anyString())).willReturn(nomRessursLederMedAnsatte);
        given(overstyrendelederrepository.findByAnsattIdentAndTilIsNull(anyString())).willReturn(Optional.empty());
        given(overstyrendelederrepository.findByOverstyrendeLeder_IdentAndTilIsNull(anyString())).willReturn(List.of());

        mvc.perform(get("/bruker/ressurser")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].ident").value(containsInAnyOrder(ansatt1.getNavident(), ansatt2.getNavident())));
    }

    @Test
    public void hentLedersRessurserReturnererOverstyrteLedere() throws Exception {
        NomRessurs nomRessursLeder = new NomRessurs("T123456", "Test Testesen", "Test@Testesen.no",
                List.of(), List.of(), List.of(NomSektor.NAV_STATLIG), List.of());

        NomRessurs ansatt1 = new NomRessurs("A111111", "Anders And", "Test@Testesen.no",
                List.of(), List.of(), List.of(NomSektor.NAV_STATLIG), List.of(new NomLeder(true, nomRessursLeder)));

        NomRessurs ansatt2 = new NomRessurs("B222222", "Bjornar Bjorn", "Test@Testesen.no",
                List.of(), List.of(), List.of(NomSektor.NAV_STATLIG), List.of(new NomLeder(true, nomRessursLeder)));

        NomRessurs nomRessursLederMedAnsatte = nyRessurs(nomRessursLeder);
        nomRessursLederMedAnsatte.setLederFor(List.of(
                new NomLederFor(
                        new NomOrgEnhet("aa000a", "Test Org", "", List.of(new NomKobling(ansatt1)), List.of(
                                new NomOrganisering(
                                        new NomOrgEnhet("aa000a", "Test Org", "", List.of(), List.of(), List.of(new NomLeder(true, ansatt2)))
                                )
                        ), List.of())
                )
        ));

        Leder leder = Leder.builder()
                .ident("A123456")
                .navn("Test Testesen")
                .build();
        Bruker bruker = Bruker.builder()
                .ident("T123456")
                .navn("Test Testesen")
                .rolle(Rolle.LEDER)
                .sistAksessert(new Date())
                .representertLeder(leder)
                .build();

        OverstyrendeLeder overstyrendeLeder = OverstyrendeLeder.builder()
                .ansattIdent(ansatt1.getNavident())
                .ansattNavn(ansatt1.getVisningsnavn())
                .fra(new Date())
                .overstyrendeLeder(Leder.builder().erDirektoratsleder(true).ident("S666666").navn("Sina Slange").build())
                .build();

        given(brukerrepository.findByIdent(anyString())).willReturn(Optional.of(bruker));
        given(nomGraphQLClient.getLedersResurser(anyString(), anyString())).willReturn(nomRessursLederMedAnsatte);
        given(overstyrendelederrepository.findByAnsattIdentAndTilIsNull(overstyrendeLeder.getAnsattIdent())).willReturn(Optional.of(overstyrendeLeder));
        given(overstyrendelederrepository.findByOverstyrendeLeder_IdentAndTilIsNull(anyString())).willReturn(List.of());

        mvc.perform(get("/bruker/ressurser")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
                //.andExpect(jsonPath("$[*].stillingsavtaler[*].stillingsavtale").value(containsInAnyOrder(List.of("DR"), List.of("DR", "MR"))));
    }


    private NomRessurs nyRessurs(NomRessurs ressurs) {
        return new NomRessurs(ressurs.getNavident(), ressurs.getVisningsnavn(), ressurs.getEpost(), ressurs.getTelefon(),
                ressurs.getLederFor(), ressurs.getSektor(), ressurs.getLedere());
    }

    @Configuration
    @ComponentScan(basePackageClasses = {Brukertjeneste.class})
    public static class TestConf {
    }
}
