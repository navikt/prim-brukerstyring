package no.nav.pim.primbrukerstyring.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Nullable;
import lombok.*;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "drift_og_vedlikehold")
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DriftOgVedlikehold {

    public DriftOgVedlikehold() {}

    @Id
    @Nullable
    @Column(name = "drift_og_vedlikehold_id")
    private Long driftOgVedlikeholdId;

    @Column
    private Boolean vedlikeholdModus;

    @Column
    private String vedlikeholdOverskrift;

    @Column
    private String vedlikeholdMelding;

    @Column
    private String driftsmelding;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriftOgVedlikehold that)) return false;
        return Objects.equals(driftOgVedlikeholdId, that.driftOgVedlikeholdId) && Objects.equals(vedlikeholdModus, that.vedlikeholdModus) && Objects.equals(vedlikeholdOverskrift, that.vedlikeholdOverskrift) && Objects.equals(vedlikeholdMelding, that.vedlikeholdMelding) && Objects.equals(driftsmelding, that.driftsmelding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driftOgVedlikeholdId, vedlikeholdModus, vedlikeholdOverskrift, vedlikeholdMelding, driftsmelding);
    }
}
