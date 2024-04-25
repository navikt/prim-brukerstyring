package no.nav.pim.primbrukerstyring.repository;

import no.nav.pim.primbrukerstyring.domain.AnsattStillingsavtale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnsattStillingsavtalerepository extends JpaRepository<AnsattStillingsavtale, String> {
}
