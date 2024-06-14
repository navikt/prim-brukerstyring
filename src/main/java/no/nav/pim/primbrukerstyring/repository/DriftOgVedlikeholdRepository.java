package no.nav.pim.primbrukerstyring.repository;

import no.nav.pim.primbrukerstyring.domain.DriftOgVedlikehold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriftOgVedlikeholdRepository extends JpaRepository<DriftOgVedlikehold, Long> {
}
