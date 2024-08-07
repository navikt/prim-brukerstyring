package no.nav.pim.primbrukerstyring.repository;

import no.nav.pim.primbrukerstyring.domain.Leder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LederRepository extends JpaRepository<Leder, Long> {
    Optional<Leder> findByIdent(String ident);
}
