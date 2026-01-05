package no.nav.pim.primbrukerstyring.service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OverstyrendeLederDto {

    String lederIdent;

    String ansattIdent;

    LocalDateTime overstyringFom;

    LocalDateTime overstyringTom;
}
