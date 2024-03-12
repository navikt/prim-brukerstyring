package no.nav.pim.primbrukerstyring.repository;

import no.nav.pim.primbrukerstyring.domain.BrukerRolle;
import no.nav.pim.primbrukerstyring.domain.Rolle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrukerRollerepository extends JpaRepository<BrukerRolle, String> {

    Optional<BrukerRolle> findByIdent(String ident);

    void deleteByIdent(String ident);

    List<BrukerRolle> findAllByRolleIn(List<Rolle> roller);
}
