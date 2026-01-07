package no.nav.pim.primbrukerstyring.repository;

import no.nav.pim.primbrukerstyring.domain.OverstyrendeLeder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OverstyrendeLederRepository extends JpaRepository<OverstyrendeLeder, String> {

    Optional<OverstyrendeLeder> findByAnsattIdentAndTilIsNull(String ansattIdent);

    List<OverstyrendeLeder> findByOverstyrendeLeder_IdentAndTilIsNull(String ident);

    List<OverstyrendeLeder> findAllByTilIsBefore(LocalDate tilBefore, Sort sort);

    List<OverstyrendeLeder> findAllByTilIsGreaterThanEqualOrTilIsNull(LocalDate tilIsGreaterThan, Sort sort);

    @Query(nativeQuery = true, value = "select * from overstyrende_leder where ansatt_ident = :ansattIdent and (til >= :tilIsGreaterThan or til is null)")
    Optional<OverstyrendeLeder> findByAnsattIdentAndTilIsGreaterThanEqualOrTilIsNull(String ansattIdent, LocalDate tilIsGreaterThan);
}
