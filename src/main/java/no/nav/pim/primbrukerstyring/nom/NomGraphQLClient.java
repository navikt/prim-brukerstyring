package no.nav.pim.primbrukerstyring.nom;

import no.nav.pim.primbrukerstyring.util.OIDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;



@Component
public class NomGraphQLClient {

    @Value("${nom.url}")
    String url;

    @Value("${nom.scope}")
    String scope;

    @Autowired
    private RestTemplate rest;

    @Autowired
    OIDCUtil oidcUtil;

    private static final Logger log = LoggerFactory.getLogger(NomGraphQLClient.class);

    public ResponseEntity<String> callGraphQLService(String auth, String query) throws Exception {
        HttpHeaders headers;
        try {
            headers = oidcUtil.setHeaders(auth, scope);
        } catch (Exception e) {
            log.error("###Problemer med header ved bruk av NOM service! Feilmelding: {}", e.getMessage());
            throw e;
        }
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).query(query);

        try {
            return rest.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
        } catch (Exception e) {
            log.error("###Henting av NOM ressurs feilet: Feilmelding: {}", e.getMessage());
            throw e;
        }
    }
}
