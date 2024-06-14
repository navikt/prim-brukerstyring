package no.nav.pim.primbrukerstyring.nom.domain;

public enum NomTelefonType {
    NAV_TJENESTE_TELEFON("NAV_TJENESTE_TELEFON"),
    NAV_KONTOR_TELEFON("NAV_KONTOR_TELEFON"),
    PRIVAT_TELEFON("PRIVAT_TELEFON");

    private final String telefonType;

    NomTelefonType(String telefonType) {
        this.telefonType = telefonType;
    }

    public String getTelefonType() {
        return telefonType;
    }
}
