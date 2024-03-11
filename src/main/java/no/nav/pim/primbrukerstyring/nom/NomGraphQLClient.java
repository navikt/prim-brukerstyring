package no.nav.pim.primbrukerstyring.nom;

import jakarta.annotation.PostConstruct;
import no.nav.pim.primbrukerstyring.nom.domain.Leder;
import no.nav.pim.primbrukerstyring.util.OIDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;



@Component
public class NomGraphQLClient {

    @Value("${nom.url}")
    String url;

    @Value("${nom.scope}")
    String scope;

    @Autowired
    OIDCUtil oidcUtil;

    WebClient webClient;

    @PostConstruct
    void init() {
        webClient = WebClient.create(url);
    }

    private static final Logger log = LoggerFactory.getLogger(NomGraphQLClient.class);

    public Leder getLedersResurser(String authorization, String navident) {
        log.info("Henter leders resurser for navident {}", navident);
        String document =
                """
                query LedersRessurser {
                    ressurs(where: {navident: "%s"}) {
                        navident
                        lederFor {
                            orgEnhet {
                                koblinger {
                                    ressurs {
                                        navident
                                    }
                                }
                            }
                        }
                    }
                }
                """.formatted(navident);

        try {
            HttpGraphQlClient graphQlClient = HttpGraphQlClient.create(webClient).mutate().header("Authorization", oidcUtil.getAuthHeader(authorization, scope)).build();
            return graphQlClient.document(document).retrieve("ressurs").toEntity(Leder.class).block();
        } catch (Exception e) {
            log.info("Noe gikk galt med henting av leders ressurser i NOM for navident {}. Feilmelding: {}", navident, e.getMessage());
        }
        return null;
    }
}
