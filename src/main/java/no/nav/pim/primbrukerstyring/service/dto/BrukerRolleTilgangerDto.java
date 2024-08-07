package no.nav.pim.primbrukerstyring.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.pim.primbrukerstyring.domain.Rolle;

import java.util.List;

@Data
@AllArgsConstructor
public class BrukerRolleTilgangerDto {
    String ident;
    Rolle rolle;
    List<String> tilganger;
}
