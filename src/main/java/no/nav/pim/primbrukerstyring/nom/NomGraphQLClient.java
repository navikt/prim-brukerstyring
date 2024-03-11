package no.nav.pim.primbrukerstyring.nom;

import jakarta.annotation.PostConstruct;
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

    public String getLedersResurser(String authorization, String navident) {
        log.info("Henter leders resurser for navident {}", navident);
        String document =
                """
                query LedersRessurser {
                    ressurs(where: navident: $navident:String!) {
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
                """;
        HttpGraphQlClient graphQlClient = HttpGraphQlClient.create(webClient).mutate().header("Authorization", authorization).build();
        return graphQlClient.document(document).variable("navident", navident).retrieve("lederFor").toEntity(String.class).block();
    }
}
