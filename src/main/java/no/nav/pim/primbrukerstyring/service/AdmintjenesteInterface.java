package no.nav.pim.primbrukerstyring.service;

import no.nav.pim.primbrukerstyring.domain.DriftOgVedlikehold;

public interface AdmintjenesteInterface {

    DriftOgVedlikehold hentDriftOgVedlikehold(String authorization);


    DriftOgVedlikehold settDriftOgVedlikehold(String authorization, DriftOgVedlikehold driftOgVedlikehold);
}
