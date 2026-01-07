package no.nav.pim.primbrukerstyring.service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OverstyrendeLederDto {

    String lederIdent;

    String ansattIdent;

    LocalDate overstyringFom;

    LocalDate overstyringTom;
}
