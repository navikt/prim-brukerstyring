package no.nav.pim.primbrukerstyring.repository;

import no.nav.pim.primbrukerstyring.domain.OverstyrendeLeder;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OverstyrendeLederRepository extends JpaRepository<OverstyrendeLeder, String> {

    Optional<OverstyrendeLeder> findByAnsattIdentAndTilIsNull(String ansattIdent);

    List<OverstyrendeLeder> findByOverstyrendeLeder_IdentAndTilIsNull(String ident);

    List<OverstyrendeLeder> findAllByTilIsNull(Sort sort);

    List<OverstyrendeLeder> findAllByTilIsNotNull(Sort sort);

    List<OverstyrendeLeder> findAllByTilIsBefore(Date tilBefore, Sort sort);

    List<OverstyrendeLeder> findAllByTilIsGreaterThanEqualOrTilIsNull(Date tilIsGreaterThan, Sort sort);

    @Query(nativeQuery = true, value = "select * from overstyrende_leder where ansatt_ident = :ansattIdent and (til >= :tilIsGreaterThan or til is null)")
    Optional<OverstyrendeLeder> findByAnsattIdentAndTilIsGreaterThanEqualOrTilIsNull(String ansattIdent, Date tilIsGreaterThan);

}
