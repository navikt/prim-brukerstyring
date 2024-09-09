package no.nav.pim.primbrukerstyring.service.dto;

import lombok.Builder;
import lombok.Data;
import no.nav.pim.primbrukerstyring.domain.Leder;
import no.nav.pim.primbrukerstyring.domain.OrgEnhet;

import java.util.Set;

@Data
@Builder
public class LederDto implements Comparable<LederDto> {
    static public LederDto fraLeder(Leder leder) {
        return LederDto.builder()
                .ident(leder.getIdent())
                .navn(leder.getNavn())
                .erDirektoratsleder(leder.getErDirektoratsleder())
                .email(leder.getEmail())
                .tlf(leder.getTlf())
                .orgEnheter(leder.getOrgEnheter())
                .build();
    }

    String ident;

    String navn;

    Boolean erDirektoratsleder;

    String email;

    String tlf;

    Set<OrgEnhet> orgEnheter;

    @Override
    public int compareTo(LederDto other) {
        return this.navn.compareTo(other.navn);
    }

}
