package no.nav.pim.primbrukerstyring.azure;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AADToken {

    private String accessToken;
    private LocalDateTime expires;

    public AADToken(String accessToken, LocalDateTime expires) {
        this.accessToken = accessToken;
        this.expires = expires;
    }
}
