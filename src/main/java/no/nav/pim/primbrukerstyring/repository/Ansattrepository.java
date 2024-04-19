package no.nav.pim.primbrukerstyring.repository;

import no.nav.pim.primbrukerstyring.domain.Ansatt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Ansattrepository extends JpaRepository<Ansatt, String> {
    Optional<Ansatt> findByIdent(String ident);
}
