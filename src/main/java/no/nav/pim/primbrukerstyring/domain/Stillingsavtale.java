package no.nav.pim.primbrukerstyring.domain;

public enum Stillingsavtale {
    DR("Direkte rapporterende"),
    MR("Midlertidig rapporterende");

    private final String stillingsavtaleBeskrivelse;

    Stillingsavtale(String stillingsavtaleBeskrivelse) {
        this.stillingsavtaleBeskrivelse = stillingsavtaleBeskrivelse;
    }

    public String getStillingsavtaleBeskrivelse() {
        return stillingsavtaleBeskrivelse;
    }
}
