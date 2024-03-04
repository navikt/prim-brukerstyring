package no.nav.pim.primbrukerstyring.nom;

public class NomQueries {

    public static String getLedersResurser(String navident) {
        return  """
                query LedersRessurser {
                    ressurs(where: {navident: "%s"}) {
                        navident
                        lederFor {
                            orgEnhet {
                                koblinger {
                                    ressurs {
                                        navident
                                    }
                                }
                            }
                        }
                    }
                }
                """.formatted(navident);
    }
}
