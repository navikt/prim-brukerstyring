package no.nav.pim.primbrukerstyring.exceptions;

import io.micrometer.core.instrument.Metrics;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
        Metrics.counter("prim_error", "exception", "NotFoundException").increment();
    }
}