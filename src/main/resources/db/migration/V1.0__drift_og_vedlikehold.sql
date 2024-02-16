CREATE TABLE drift_og_vedlikehold (
                                    drift_og_vedlikehold_id BIGINT NOT NULL,
                                    vedlikehold_modus BOOLEAN NOT NULL,
                                    vedlikehold_overskrift TEXT NOT NULL,
                                    vedlikehold_melding TEXT NOT NULL,
                                    driftsmelding TEXT NOT NULL
);

INSERT INTO drift_og_vedlikehold (drift_og_vedlikehold_id, vedlikehold_modus, vedlikehold_overskrift, vedlikehold_melding, driftsmelding) VALUES (0, false, '', '', '');
