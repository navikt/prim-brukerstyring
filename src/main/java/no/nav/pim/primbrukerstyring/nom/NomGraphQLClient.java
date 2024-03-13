package no.nav.pim.primbrukerstyring.nom;

import jakarta.annotation.PostConstruct;
import no.nav.pim.primbrukerstyring.nom.domain.OrgEnheter;
import no.nav.pim.primbrukerstyring.nom.domain.Ressurs;
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

    public Ressurs getLedersResurser(String authorization, String navident) {
        log.info("Henter leders resurser for navident {}", navident);
        String document =
                """
                query LedersRessurser {
                    ressurs(where: {navident: "%s"}) {
                        navident
                        visningsnavn
                        lederFor {
                            orgEnhet {
                                koblinger {
                                    ressurs {
                                        navident
                                        visningsnavn
                                    }
                                }
                                navn
                                organiseringer {
                                    orgEnhet {
                                        navn
                                        leder {
                                            ressurs {
                                                navident
                                                visningsnavn
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """.formatted(navident);

        try {
            HttpGraphQlClient graphQlClient = HttpGraphQlClient.create(webClient).mutate().header("Authorization", oidcUtil.getAuthHeader(authorization, scope)).build();
            return graphQlClient.document(document).retrieve("ressurs").toEntity(Ressurs.class).block();
        } catch (Exception e) {
            log.info("Noe gikk galt med henting av leders ressurser i NOM for navident {}. Feilmelding: {}", navident, e.getMessage());
        }
        return null;
    }

    public OrgEnheter getOrganisasjonstre(String authorization) {
        log.info("Henter organisasjonstre");
        String document =
            """
                query Organisjonstre {
                      orgEnheter(where: {orgNiv: "ORGNIV0"}) {
                            orgEnhet {
                                  navn
                                  id
                                  organiseringer(retning: under) {
                                        orgEnhet {
                                              navn
                                              id
                                              organiseringer(retning: under) {
                                                    orgEnhet {
                                                          navn
                                                          id
                                                          organiseringer(retning: under) {
                                                                orgEnhet {
                                                                      navn
                                                                      id
                                                                      organiseringer(retning: under) {
                                                                            orgEnhet {
                                                                              navn
                                                                              id
                                                                            }
                                                                      }
                                                                }
                                                          }
                                                    }
                                              }
                                        }
                                  }
                            }
                      }
                }
            """;
        try {
            HttpGraphQlClient graphQlClient = HttpGraphQlClient.create(webClient).mutate().header("Authorization", oidcUtil.getAuthHeader(authorization, scope)).build();
            return graphQlClient.document(document).retrieve("orgEnheter").toEntity(OrgEnheter.class).block();
        } catch (Exception e) {
            log.info("Noe gikk galt med henting av organisasjons pyramiden. Feilmelding: {}", e.getMessage());
        }
        return null;
    }
}
