package no.nav.pim.primbrukerstyring.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import no.nav.pim.primbrukerstyring.azure.AADProperties;
import no.nav.pim.primbrukerstyring.azure.AADToken;
import no.nav.pim.primbrukerstyring.exceptions.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class OIDCUtil {

    private static final Logger log = LoggerFactory.getLogger(OIDCUtil.class);
    private static volatile AADToken token;
    private final static String CLIENT_CREDENTIAL = "client_credentials";
    private final static String ON_BEHALF_OF_FLOW = "on_behalf_of";
    private final static String LOCAL = "local-dev";

    @Autowired
    private RestTemplate rest;

    @Value("${nais_cluster}")
    private String naisCluster;

    @Autowired
    private AADProperties aadProperties;

    public Optional<String> finnClaimFraOIDCToken(String authorization, String claim) {
        if (authorization != null) {
            if (!LOCAL.equals(naisCluster)) {
                String[] authElements = authorization.split(",");
                for (String authElement : authElements) {
                    try {
                        String[] pair = authElement.split(" ");
                        if (pair[0].trim().equalsIgnoreCase("bearer")) {
                            JWT jwt = JWTParser.parse(pair[1].trim());
                            return Optional.ofNullable(jwt.getJWTClaimsSet().getStringClaim(claim));
                        }
                    } catch (Exception e) {
                        log.error("###OIDC-token har ikke riktig format");
                        return Optional.empty();
                    }
                }
                log.error("###Authorization-header inneholder ikke OIDC-token");
                return Optional.empty();
            } else {
                return Optional.of(claim.equals("name") ? "Test Testesen" : "A123456");
            }
        }
        log.error("###Ingen Authorization-header");
        return Optional.empty();
    }

    public String getAuthHeader(String auth, String scope) throws Exception {
        if (LOCAL.equals(naisCluster)) {
            return "Bearer localtoken";
        } else {
            return "bearer " + getAccessToken(auth, scope);
        }
    }

    private String getAccessToken(String auth, String scope) throws Exception {
        String grantType = (auth == null) ? CLIENT_CREDENTIAL : finnGranttype(finnToken(auth));
        if (token == null ||
                (token != null && !kanTokenGjenbrukes(auth, scope, grantType)))  {
            synchronized (this) {
                log.info("Fornyer token av type {}, scope {}", grantType, scope);
                token = hentAccessToken(auth, grantType, scope);
                log.info("Ny token utløper {}", token.getExpires());
            }
        } else {
            log.info("Token med granttype {} gjenbrukes, utløper {}", grantType, (auth != null) ? finnExpiryTime(auth) : null);
        }
        return token.getAccessToken();
    }

    private boolean kanTokenGjenbrukes(String auth, String scope, String grantType) throws ParseException {
        try {
            String tokenGrantType = finnGranttype(token.getAccessToken());
            String mottaker = finnMottaker(token.getAccessToken());
            log.info("Eksisterende granttype:{}, mottaker:{}, bruker:{}, expiry:{}", tokenGrantType, mottaker, finnBruker(token.getAccessToken()), token.getExpires());
            log.info("Nytt token: scope:{}, granttype:{}, expiry:{}", scope, grantType, (auth != null) ? finnExpiryTime(auth) : null);
            return tokenGrantType.equals(grantType)
                    && tidsgyldig(token)
                    && (CLIENT_CREDENTIAL.equals(grantType) || sammeBruker(auth, token));
        } catch (ParseException e) {
            log.error("###Feil i parsing av token: {}", e.getMessage());
            return false;
        }
    }

    private LocalDateTime finnExpiryTime(String auth) throws ParseException {
        JWT jwt = JWTParser.parse(finnToken(auth));
        return jwt.getJWTClaimsSet().getExpirationTime()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private boolean sammeBruker(String auth, AADToken token) throws ParseException {
        return finnClaimFraOIDCToken(auth, "preferred_username").get().equalsIgnoreCase(finnBruker(token.getAccessToken()));
    }

    private boolean tidsgyldig(AADToken token) {
        return token.getExpires().isAfter(LocalDateTime.now().plusMinutes(2L));
    }

    private String finnToken(String auth) {
        String token = "";
        String[] pair = auth.split(" ");
        if (pair[0].trim().equalsIgnoreCase("bearer")) {
            token = pair[1].trim();
        }
        return token;
    }

    private String finnBruker(String token) throws ParseException {
        JWT jwt = JWTParser.parse(token.trim());
        return jwt.getJWTClaimsSet().getStringClaim("preferred_username");
    }

    private String finnMottaker(String token) throws ParseException {
        JWT jwt = JWTParser.parse(token);
        List<String> mottakere = jwt.getJWTClaimsSet().getAudience();
        return mottakere.get(0);
    }

    private String finnGranttype(String token) throws ParseException {
        JWT jwt = JWTParser.parse(token.trim());
        if (jwt.getJWTClaimsSet().getStringClaim("name") != null) {
            return ON_BEHALF_OF_FLOW;
        } else {
            return CLIENT_CREDENTIAL;
        }
    }

    private AADToken hentAccessToken(String auth, String grantType, String scope) throws Exception {
        if (!LOCAL.equals(naisCluster)) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", aadProperties.getClientId());
            map.add("client_secret", aadProperties.getClientSecret());
            map.add("scope", scope);
            if (ON_BEHALF_OF_FLOW.equals(grantType)) {
                map.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
                map.add("assertion", finnToken(auth));
                map.add("requested_token_use", "on_behalf_of");
            } else if (CLIENT_CREDENTIAL.equals(grantType)) {
                map.add("grant_type", "client_credentials");
            }
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> respons;
            try {
                respons = rest.postForEntity(aadProperties.getTokenEndpoint(), request, String.class);
            } catch (Exception e) {
                log.error("###Henting av access-token feilet: {}", e.getMessage());
                throw new AuthorizationException("Henting av access-token feilet: " + e.getMessage());
            }
            return finnAadToken(respons.getBody())
                    .orElseThrow(() -> new AuthorizationException("Henting av access-token feilet: Token-parsing feiler"));
        } else {
            return new AADToken("localtoken", LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")).plusHours(1));
        }
    }

    private Optional<AADToken> finnAadToken(String bearerToken) throws Exception {
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(bearerToken);
        String accessToken = null;
        LocalDateTime expirationTime = null;
        try {
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String name = parser.getCurrentName();
                if ("access_token".equals(name)) {
                    parser.nextToken();
                    accessToken = parser.getText();
                } else if ("expires_in".equals(name)) {
                    parser.nextToken();
                    long expiresIn = Long.parseLong(parser.getText());
                    Date now = new Date();
                    now.setTime(now.getTime() + expiresIn * 1000L);
                    expirationTime = now.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                }
            }
        } catch (Exception e) {
            log.error("###Parsing av token feiler: {}", e.getMessage());
            return Optional.empty();
        }
        return Optional.of(new AADToken(accessToken, expirationTime));
    }
}
