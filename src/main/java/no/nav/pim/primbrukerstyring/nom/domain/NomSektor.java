package no.nav.pim.primbrukerstyring.nom.domain;

public enum NomSektor {
    NAV_STATLIG("NAV_STATLIG"),
    NAV_KOMMUNAL("NAV_KOMMUNAL"),
    EKSTERN("EKSTERN");

    private final String sektorVerdi;

    NomSektor(String sektorVerdi) {
        this.sektorVerdi = sektorVerdi;
    }

    public String getSektorVerdi() {
        return sektorVerdi;
    }
}
