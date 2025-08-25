package no.nav.pim.primbrukerstyring.service;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import no.nav.pim.primbrukerstyring.domain.Bruker;
import no.nav.pim.primbrukerstyring.domain.Rolle;
import no.nav.pim.primbrukerstyring.nom.NomGraphQLClient;
import no.nav.pim.primbrukerstyring.nom.domain.NomOrgEnhet;
import no.nav.pim.primbrukerstyring.nom.domain.NomOrganisering;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;
import no.nav.pim.primbrukerstyring.repository.BrukerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.data.DefaultRepositoryTagsProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Component
public class BrukerKontroll {

    private static final Logger log = LoggerFactory.getLogger(BrukerKontroll.class);

    @Autowired
    BrukerRepository brukerrepository;

    @Autowired
    NomGraphQLClient nomGraphQLClient;
    private DefaultRepositoryTagsProvider repositoryTagsProvider;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = RuntimeException.class)
    @Scheduled(cron = "${brukerstatuskontroll.cron-pattern:\"0 0 0 * * *\"}")
    @SchedulerLock(name = "Brukerstatuskontroll")
    public void sjekkBrukerstatus() {
        log.info( "Brukerstatuskontroll starter {}", new Date() );
        List<Bruker> brukere = brukerrepository.findAllByRolleIn(List.of(Rolle.HR_MEDARBEIDER, Rolle.HR_MEDARBEIDER_BEMANNING));
        List<NomOrgEnhet> organisasjonstre = nomGraphQLClient.getOrganisasjonstre(null).stream()
                .flatMap(this::flatMapOrganisasjonstre)
                .toList();
        log.info( "Brukerstatuskontroll fant {} brukere og {} orgenheter i organisasjonstreet", brukere.size(), organisasjonstre.size() );
        brukere.forEach(bruker -> {
            NomRessurs ressurs = nomGraphQLClient.getRessurs(null, bruker.getIdent());
            if (ressurs != null) {
                bruker.setNavn(ressurs.getVisningsnavn());
                if (ressurs.getSluttdato() != null && ressurs.getSluttdato().before(new Date())) {
                    log.info( "Bruker {} har sluttet {}", bruker.getIdent(), ressurs.getSluttdato() );
                    bruker.setSluttet(true);
                }
                ressurs.getOrgTilknytning().stream()
                    .filter(orgTilknytning -> orgTilknytning.getErDagligOppfolging() && (orgTilknytning.getGyldigTom() == null || orgTilknytning.getGyldigTom().after(new Date())))
                    .map(orgTilknytning -> orgTilknytning.getOrgEnhet().getId())
                    .findFirst().ifPresentOrElse(enhet -> {
                        if (!enhet.equals(bruker.getEnhet())) {
                            log.info( "Bruker {} har endret enhet fra {} til {}", bruker.getIdent(), bruker.getEnhet(), enhet );
                            if (bruker.getEnhet() != null) bruker.setEndretEnhet(true);
                            bruker.setEnhet(enhet);
                        }
                    }, () -> {
                        log.info( "Kunne ikke finne orgenhet for bruker {}", bruker.getIdent() );
                        bruker.setEnhet(null);
                    });

                List<String> ukjentTilgang = bruker.getTilganger().stream().filter(orgenhetId -> {
                    boolean tilgangGyldig = organisasjonstre.stream().anyMatch(orgEnhet -> orgEnhet.getId().equals(orgenhetId));
                    if (!tilgangGyldig) {
                        log.info( "Orgenhet {} finnes ikke lengre i organisasjonstreet", orgenhetId );
                    }
                    return !tilgangGyldig;
                }).toList();
                bruker.setUkjentTilgang(ukjentTilgang);
                brukerrepository.save(bruker);
            }
        });

    }


    private Stream<NomOrgEnhet> flatMapOrganisasjonstre(NomOrganisering orgenhet) {
        Stream<NomOrgEnhet> underOrganiseringer = orgenhet.getOrgEnhet().getOrganiseringer().stream()
                .flatMap(this::flatMapOrganisasjonstre);

        return Stream.concat(Stream.of(orgenhet.getOrgEnhet()), underOrganiseringer);
    }
}