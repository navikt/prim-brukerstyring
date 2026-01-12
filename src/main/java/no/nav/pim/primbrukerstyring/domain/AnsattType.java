package no.nav.pim.primbrukerstyring.domain;

import no.nav.pim.primbrukerstyring.nom.domain.NomSektor;

public enum AnsattType {
    E("Eksterne"),
    F("Fast_ansatte"),
    N("Kommunalt_ansatt"),
    U("Ukjent");

    private final String ansattTypeBeskrivelse;

    AnsattType(String ansattTypeBeskrivelse) {
        this.ansattTypeBeskrivelse = ansattTypeBeskrivelse;
    }

    public String getAnsattTypeBeskrivelse() {
        return ansattTypeBeskrivelse;
    }

    static AnsattType fraNomSektor(NomSektor sektor) {
        if (sektor.getSektorVerdi().equals(NomSektor.NAV_STATLIG.getSektorVerdi())) return AnsattType.F;
        if (sektor.getSektorVerdi().equals(NomSektor.NAV_KOMMUNAL.getSektorVerdi())) return AnsattType.N;
        if (sektor.getSektorVerdi().equals(NomSektor.EKSTERN.getSektorVerdi())) return AnsattType.E;
        return AnsattType.U;
    }
}