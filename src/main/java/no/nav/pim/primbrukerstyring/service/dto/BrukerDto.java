package no.nav.pim.primbrukerstyring.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.pim.primbrukerstyring.domain.Leder;
import no.nav.pim.primbrukerstyring.domain.Rolle;

@Data
@AllArgsConstructor
public class BrukerDto {

    Rolle rolle;

    Leder representertLeder;
}
