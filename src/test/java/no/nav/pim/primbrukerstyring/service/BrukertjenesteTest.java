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

    @Before
    public void setUp() {
        given(oidcUtil.finnClaimFraOIDCToken(anyString(), anyString())).willReturn(Optional.of("T123456"));
        given(metricsRegistry.counter(anyString(), anyString(), anyString(), anyString(), anyString())).willReturn(mock(Counter.class));
    }

    @Test
    public void hentBrukerForRessursMedLederForSetterStatusLeder() throws Exception {
        NomRessurs nomRessursLeder = NomRessurs.builder()
            .navident("T123456")
            .visningsnavn("Test Testesen")
            .epost("Test@Testesen.no")
            .telefon(List.of())
            .sektor(List.of(NomSektor.NAV_STATLIG))
            .lederFor(List.of(
                    NomLederFor.builder().orgEnhet(NomOrgEnhet.builder().id("aa000a").navn("Test Org").build()).build()
            ))
            .build();

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
        NomRessurs nomRessursLeder = NomRessurs.builder()
                .navident("T123456")
                .visningsnavn("Test Testesen")
                .epost("Test@Testesen.no")
                .telefon(List.of())
                .sektor(List.of(NomSektor.NAV_STATLIG))
                .lederFor(List.of())
                .build();

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
    public void hentBrukerForRessursMedHRMedarbeiderMedUtgåttLederReturnererNull() throws Exception {
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
        NomRessurs nomRessursLeder = NomRessurs.builder()
                .navident("T123456")
                .visningsnavn("Test Testesen")
                .epost("Test@Testesen.no")
                .telefon(List.of())
                .sektor(List.of(NomSektor.NAV_STATLIG))
                .build();

        NomRessurs ansatt1 = NomRessurs.builder().navident("A111111").visningsnavn("Anders And").sektor(List.of(NomSektor.NAV_STATLIG)).ledere(List.of(
                NomLeder.builder().erDagligOppfolging(true).ressurs(nomRessursLeder).build()
        )).build();

        NomRessurs ansatt2 = NomRessurs.builder().navident("B222222").visningsnavn("Bjornar Bjorn").sektor(List.of(NomSektor.NAV_STATLIG)).ledere(List.of(
                NomLeder.builder().erDagligOppfolging(true).ressurs(nomRessursLeder).build()
        )).build();

        NomRessurs nomRessursLederMedAnsatte = nyRessurs(nomRessursLeder);
        nomRessursLederMedAnsatte.setLederFor(List.of(
            NomLederFor.builder().orgEnhet(
                NomOrgEnhet.builder()
                    .id("aa000a")
                    .navn("Test Org")
                    .koblinger(List.of(NomKobling.builder().ressurs(ansatt1).build()))
                    .organiseringer(List.of(
                        NomOrganisering.builder()
                            .orgEnhet(NomOrgEnhet.builder().id("aa000a").navn("Test Org").leder(List.of(NomLeder.builder().ressurs(ansatt2).build())).build())
                            .build()
                    ))
                    .build()
            ).build()
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
        NomRessurs nomRessursLeder = NomRessurs.builder()
                .navident("T123456")
                .visningsnavn("Test Testesen")
                .epost("Test@Testesen.no")
                .telefon(List.of())
                .sektor(List.of(NomSektor.NAV_STATLIG))
                .build();

        NomRessurs ansatt1 = NomRessurs.builder().navident("A111111").visningsnavn("Anders And").sektor(List.of(NomSektor.NAV_STATLIG)).ledere(List.of(
                NomLeder.builder().erDagligOppfolging(true).ressurs(nomRessursLeder).build()
        )).build();

        NomRessurs ansatt2 = NomRessurs.builder().navident("B222222").visningsnavn("Bjornar Bjorn").sektor(List.of(NomSektor.NAV_STATLIG)).ledere(List.of(
                NomLeder.builder().erDagligOppfolging(true).ressurs(nomRessursLeder).build()
        )).build();

        NomRessurs nomRessursLederMedAnsatte = nyRessurs(nomRessursLeder);
        nomRessursLederMedAnsatte.setLederFor(List.of(
                NomLederFor.builder().orgEnhet(
                        NomOrgEnhet.builder()
                                .id("aa000a")
                                .navn("Test Org")
                                .koblinger(List.of(NomKobling.builder().ressurs(ansatt1).build()))
                                .organiseringer(List.of(
                                        NomOrganisering.builder()
                                                .orgEnhet(NomOrgEnhet.builder().id("aa000a").navn("Test Org").leder(List.of(NomLeder.builder().ressurs(ansatt2).build())).build())
                                                .build()
                                ))
                                .build()
                ).build()
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
        return NomRessurs.builder()
                .navident(ressurs.getNavident())
                .visningsnavn(ressurs.getVisningsnavn())
                .epost(ressurs.getEpost())
                .telefon(ressurs.getTelefon())
                .sektor(ressurs.getSektor())
                .lederFor(ressurs.getLederFor())
                .ledere(ressurs.getLedere())
                .build();
    }

    @Configuration
    @ComponentScan(basePackageClasses = {Brukertjeneste.class})
    public static class TestConf {
    }
}
