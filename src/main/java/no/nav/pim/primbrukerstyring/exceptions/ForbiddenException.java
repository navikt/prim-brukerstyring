package no.nav.pim.primbrukerstyring.exceptions;

import io.micrometer.core.instrument.Metrics;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
        Metrics.counter("prim_error", "exception", "ForbiddenException").increment();
    }
}