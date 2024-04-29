package no.nav.pim.primbrukerstyring.repository;

import no.nav.pim.primbrukerstyring.domain.OverstyrendeLeder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OverstyrendeLederRepository extends JpaRepository<OverstyrendeLeder, String> {

    Optional<OverstyrendeLeder> findByAnsattIdentAndTil(String ansattIdent, Date til);

    @Query(value = "select * from overstyrende_leder ol inner join leder l on l.leder_id = ol.overstyrende_leder_id where l.ident = ?1 and ol.til IS NULL", nativeQuery = true)
    List<OverstyrendeLeder> findByLederIdent(String lederIdent);
}
