package no.nav.pim.primbrukerstyring.nom;

import jakarta.annotation.PostConstruct;
import no.nav.pim.primbrukerstyring.nom.domain.NomOrgEnhet;
import no.nav.pim.primbrukerstyring.nom.domain.NomOrganisering;
import no.nav.pim.primbrukerstyring.nom.domain.NomRessurs;
import no.nav.pim.primbrukerstyring.util.OIDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;


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

    public NomRessurs getLedersResurser(String authorization, String navident) {
        log.info("Henter leders resurser for navident {}", navident);
        String document =
            """
                    query LedersRessurser {
                        ressurs(where: {navident: "%s"}) {
                            navident
                            visningsnavn
                            epost
                            telefon {
                              nummer
                              type
                            }
                            lederFor {
                                orgEnhet {
                                    id
                                    navn
                                    orgEnhetsType
                                    koblinger {
                                        ressurs {
                                            navident
                                            visningsnavn
                                            sektor
                                            ledere {
                                                erDagligOppfolging
                                                ressurs {
                                                    navident
                                                    visningsnavn
                                                    epost
                                                    telefon {
                                                      nummer
                                                      type
                                                    }
                                                    lederFor {
                                                        orgEnhet {
                                                            id
                                                            navn
                                                            orgEnhetsType
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    organiseringer {
                                        orgEnhet {
                                            navn
                                            leder {
                                                ressurs {
                                                    navident
                                                    visningsnavn
                                                    sektor
                                                    ledere {
                                                        erDagligOppfolging
                                                        ressurs {
                                                            navident
                                                            visningsnavn
                                                            epost
                                                            telefon {
                                                              nummer
                                                              type
                                                            }
                                                            lederFor {
                                                                orgEnhet {
                                                                    id
                                                                    navn
                                                                    orgEnhetsType
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
                    }
                        """.formatted(navident);

        try {
            HttpGraphQlClient graphQlClient = HttpGraphQlClient.create(webClient).mutate().header("Authorization", oidcUtil.getAuthHeader(authorization, scope)).build();
            return graphQlClient.document(document).retrieve("ressurs").toEntity(NomRessurs.class).block();
        } catch (Exception e) {
            log.info("Noe gikk galt med henting av leders ressurser i NOM for navident {}. Feilmelding: {}", navident, e.getMessage());
        }
        return null;
    }

    public NomRessurs getRessurs(String authorization, String navident) {
        log.info("Henter resurs for navident {}", navident);
        String document =
                """
                        query AnsattRessurs {
                            ressurs(where: {navident: "%s"}) {
                                navident
                                visningsnavn
                                sektor
                                epost
                                telefon {
                                  nummer
                                  type
                                }
                                ledere {
                                    erDagligOppfolging
                                    ressurs {
                                        navident
                                        visningsnavn
                                        epost
                                        telefon {
                                          nummer
                                          type
                                        }
                                        lederFor {
                                            orgEnhet {
                                                id
                                                navn
                                                orgEnhetsType
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        """.formatted(navident);

        try {
            HttpGraphQlClient graphQlClient = HttpGraphQlClient.create(webClient).mutate().header("Authorization", oidcUtil.getAuthHeader(authorization, scope)).build();
            return graphQlClient.document(document).retrieve("ressurs").toEntity(NomRessurs.class).block();
        } catch (Exception e) {
            log.info("Noe gikk galt med henting av leders ressurser i NOM for navident {}. Feilmelding: {}", navident, e.getMessage());
        }
        return null;
    }

    public List<NomOrganisering> getOrganisasjonstre(String authorization) {
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
            return graphQlClient.document(document).retrieve("orgEnheter").toEntityList(NomOrganisering.class).block();
        } catch (Exception e) {
            log.info("Noe gikk galt med henting av organisasjons pyramiden. Feilmelding: {}", e.getMessage());
        }
        return null;
    }

    public NomOrgEnhet hentOrganisasjoner(String authorization, String organisasjonsId) {
        log.info("Henter organisasjon med id: {}", organisasjonsId);
        String document =
            """
            query Organisjon {
                orgEnhet(where: {id: "%s"}) {
                    navn
                    id
                    leder {
                        ressurs {
                            navident
                            visningsnavn
                            epost
                            telefon {
                              nummer
                              type
                            }
                            lederFor {
                                orgEnhet {
                                    id
                                    navn
                                    orgEnhetsType
                                }
                            }
                        }
                    }
                    organiseringer(retning: under) {
                        orgEnhet {
                            navn
                            id
                            leder {
                                ressurs {
                                    navident
                                    visningsnavn
                                    epost
                                    telefon {
                                      nummer
                                      type
                                    }
                                    lederFor {
                                        orgEnhet {
                                            id
                                            navn
                                            orgEnhetsType
                                        }
                                    }
                                }
                            }
                            organiseringer(retning: under) {
                                orgEnhet {
                                    navn
                                    id
                                    leder {
                                        ressurs {
                                            navident
                                            visningsnavn
                                            epost
                                            telefon {
                                              nummer
                                              type
                                            }
                                            lederFor {
                                                orgEnhet {
                                                    id
                                                    navn
                                                    orgEnhetsType
                                                }
                                            }
                                        }
                                    }
                                    organiseringer(retning: under) {
                                        orgEnhet {
                                            navn
                                            id
                                            leder {
                                                ressurs {
                                                    navident
                                                    visningsnavn
                                                    epost
                                                    telefon {
                                                      nummer
                                                      type
                                                    }
                                                    lederFor {
                                                        orgEnhet {
                                                            id
                                                            navn
                                                            orgEnhetsType
                                                        }
                                                    }
                                                }
                                            }
                                            organiseringer(retning: under) {
                                                orgEnhet {
                                                    navn
                                                    id
                                                    leder {
                                                        ressurs {
                                                            navident
                                                            visningsnavn
                                                            epost
                                                            telefon {
                                                              nummer
                                                              type
                                                            }
                                                            lederFor {
                                                                orgEnhet {
                                                                    id
                                                                    navn
                                                                    orgEnhetsType
                                                                }
                                                            }
                                                        }
                                                    }
                                                    organiseringer(retning: under) {
                                                        orgEnhet {
                                                            navn
                                                            id
                                                            leder {
                                                                ressurs {
                                                                    navident
                                                                    visningsnavn
                                                                    epost
                                                                    telefon {
                                                                      nummer
                                                                      type
                                                                    }
                                                                    lederFor {
                                                                        orgEnhet {
                                                                            id
                                                                            navn
                                                                            orgEnhetsType
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            organiseringer(retning: under) {
                                                                orgEnhet {
                                                                    navn
                                                                    id
                                                                    leder {
                                                                        ressurs {
                                                                            navident
                                                                            visningsnavn
                                                                            epost
                                                                            telefon {
                                                                              nummer
                                                                              type
                                                                            }
                                                                            lederFor {
                                                                                orgEnhet {
                                                                                    id
                                                                                    navn
                                                                                    orgEnhetsType
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    organiseringer(retning: under) {
                                                                        orgEnhet {
                                                                            navn
                                                                            id
                                                                            leder {
                                                                                ressurs {
                                                                                    navident
                                                                                    visningsnavn
                                                                                    epost
                                                                                    telefon {
                                                                                      nummer
                                                                                      type
                                                                                    }
                                                                                    lederFor {
                                                                                        orgEnhet {
                                                                                            id
                                                                                            navn
                                                                                            orgEnhetsType
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
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """.formatted(organisasjonsId);
        try {
            HttpGraphQlClient graphQlClient = HttpGraphQlClient.create(webClient).mutate().header("Authorization", oidcUtil.getAuthHeader(authorization, scope)).build();
            return graphQlClient.document(document).retrieve("orgEnhet").toEntity(NomOrgEnhet.class).block();
        } catch (Exception e) {
            log.info("Noe gikk galt med henting av organisasjons pyramiden. Feilmelding: {}", e.getMessage());
        }
        return null;
    }
}
