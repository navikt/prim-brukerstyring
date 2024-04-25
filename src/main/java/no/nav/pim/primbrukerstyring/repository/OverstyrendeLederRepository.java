package no.nav.pim.primbrukerstyring.repository;

import no.nav.pim.primbrukerstyring.domain.OverstyrendeLeder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface OverstyrendeLederRepository extends JpaRepository<OverstyrendeLeder, String> {

    Optional<OverstyrendeLeder> findByAnsattIdentAndTil(String ansattIdent, Date til);
}
