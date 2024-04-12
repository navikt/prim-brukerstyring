package no.nav.pim.primbrukerstyring.domain;

import no.nav.pim.primbrukerstyring.nom.domain.NomSektor;

import java.util.List;

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

    static AnsattType fraNomSektor(List<NomSektor> sektor) {
        if (sektor.stream().anyMatch(s -> s.equals(NomSektor.NAV_STATLIG))) return AnsattType.F;
        if (sektor.stream().anyMatch(s -> s.equals(NomSektor.NAV_KOMMUNAL))) return AnsattType.N;
        if (sektor.stream().anyMatch(s -> s.equals(NomSektor.EKSTERN))) return AnsattType.E;
        return AnsattType.U;
    }
}