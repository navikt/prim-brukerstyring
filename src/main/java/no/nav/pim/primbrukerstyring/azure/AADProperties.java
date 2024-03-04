package no.nav.pim.primbrukerstyring.azure;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AADProperties {

    @Value("${token.endpoint}")
    private String tokenEndpoint;
    @Value("${clientId}")
    private String clientId;
    @Value("${clientSecret}")
    private String clientSecret;
}

