package no.nav.pim.primbrukerstyring.nom.domain;

import lombok.Data;

@Data
public class NomTelefon implements Comparable<NomTelefon> {
    String nummer;
    NomTelefonType type;

    @Override
    public int compareTo(NomTelefon other) {
        if (this.type.equals(other.type)) return 0;
        if (this.type.equals(NomTelefonType.NAV_TJENESTE_TELEFON)) return -1;
        if (other.type.equals(NomTelefonType.NAV_TJENESTE_TELEFON)) return 1;
        if (this.type.equals(NomTelefonType.NAV_KONTOR_TELEFON)) return -1;
        return 1;
    }
}
