package no.nav.pim.primbrukerstyring.utils;

import no.nav.pim.primbrukerstyring.nom.domain.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class NomUtils {

    public static NomTelefon lagNomTelefon(NomTelefonType type) {
        NomTelefon telefon = new NomTelefon();
        telefon.setType(type);
        telefon.setNummer(lagTilfeldigString());
        return telefon;
    }

    public static NomOrgEnhet lagNomOrgEnhet(boolean direktoratet, List<NomKobling> koblinger, List<NomOrganisering> organiseringer, List<NomLeder> ledere) {
        NomOrgEnhet nomOrgEnhet = new NomOrgEnhet();
        nomOrgEnhet.setId(lagTilfeldigString());
        nomOrgEnhet.setNavn(lagTilfeldigString());
        nomOrgEnhet.setOrgEnhetsType(direktoratet ? "DIREKTORAT" : "");
        nomOrgEnhet.setKoblinger(koblinger != null ? koblinger : List.of());
        nomOrgEnhet.setOrganiseringer(organiseringer != null ? organiseringer : List.of());
        nomOrgEnhet.setLeder(ledere != null ? ledere : List.of());
        return nomOrgEnhet;
    }

    public static NomLederFor lagNomLederFor(boolean direktoratet) {
        return lagNomLederFor(direktoratet, null, null, null);
    }

    public static NomLederFor lagNomLederFor(boolean direktoratet, List<NomKobling> koblinger, List<NomOrganisering> organiseringer, List<NomLeder> ledere) {
        NomLederFor nomLederFor = new NomLederFor();
        nomLederFor.setOrgEnhet(lagNomOrgEnhet(direktoratet, koblinger, organiseringer, ledere));
        return nomLederFor;
    }

    public static NomLeder lagNomLeder(boolean erDagligOppfolging, NomRessurs ressurs) {
        NomLeder nomLeder = new NomLeder();
        nomLeder.setErDagligOppfolging(erDagligOppfolging);
        nomLeder.setRessurs(ressurs);
        return nomLeder;
    }
    public static NomRessurs lagNomRessurs(String ident, List<NomTelefon> telefon, List<NomLederFor> lederFor, List<NomLeder> ledere) {
        NomRessurs nomRessurs = new NomRessurs();
        nomRessurs.setNavident(ident != null ? ident : lagTilfeldigString());
        nomRessurs.setVisningsnavn(lagTilfeldigString());
        nomRessurs.setEpost(lagTilfeldigString());
        nomRessurs.setSektor(List.of(NomSektor.NAV_STATLIG));
        nomRessurs.setTelefon(telefon != null ? telefon : List.of());
        nomRessurs.setLederFor(lederFor != null ? lederFor : List.of());
        nomRessurs.setLedere(ledere != null ? ledere : List.of());
        return nomRessurs;
    }

    public static NomKobling lagNomKobling(NomRessurs ressurs) {
        NomKobling nomKobling = new NomKobling();
        nomKobling.setRessurs(ressurs);
        return nomKobling;
    }

    public static NomOrganisering lagNomOrganisering(NomLeder leder) {
        NomOrganisering nomOrganisering = new NomOrganisering();
        nomOrganisering.setOrgEnhet(lagNomOrgEnhet(false, null, null, List.of(leder)));
        return nomOrganisering;
    }

    public static NomRessurs kopierRessurs(NomRessurs ressurs) {
        NomRessurs kopiertRessurs = new NomRessurs();
        kopiertRessurs.setNavident(ressurs.getNavident());
        kopiertRessurs.setVisningsnavn(ressurs.getVisningsnavn());
        kopiertRessurs.setEpost(ressurs.getEpost());
        kopiertRessurs.setSektor(ressurs.getSektor());
        kopiertRessurs.setTelefon(ressurs.getTelefon());
        kopiertRessurs.setLederFor(ressurs.getLederFor());
        kopiertRessurs.setLedere(ressurs.getLedere());
        return kopiertRessurs;
    }

    private static String lagTilfeldigString() {
        String karrakterer = "0123456789abcdefghijklmnopqrstuvwxyzæøåABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ";
        StringBuilder bygger = new StringBuilder();
        Random rnd = new Random();
        while (bygger.length() < 8) {
            int index = (int) (rnd.nextFloat() * karrakterer.length());
            bygger.append(karrakterer.charAt(index));
        }
        return bygger.toString();
    }
}
