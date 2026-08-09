package prototype.stateArchitecture.transducer;

/**
 * Thrown when a Transducer encounters an I/O failure while reading from
 * the JsonParser or writing to the JsonGenerator.
 *
 * These are treated as unrecoverable at this layer: streaming JSON
 * transformation has no meaningful way to "partially succeed" once the
 * underlying parser/generator breaks, so the failure is propagated as an
 * unchecked exception up to the caller (Main), which decides how to
 * report it and exit.
 */
public class TransducerException extends RuntimeException {

    public TransducerException(String message, Throwable cause) {
        super(message, cause);
    }
}
