package no.nav.pim.primbrukerstyring.repository;

import no.nav.pim.primbrukerstyring.domain.Bruker;
import no.nav.pim.primbrukerstyring.domain.Rolle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Brukerrepository extends JpaRepository<Bruker, String> {

    Optional<Bruker> findByIdent(String ident);

    void deleteByIdent(String ident);

    List<Bruker> findAllByRolleIn(List<Rolle> roller);
}
