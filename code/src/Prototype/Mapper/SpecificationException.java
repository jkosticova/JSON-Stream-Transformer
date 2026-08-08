package prototype.mapper;

/**
 * Thrown when a transformation specification file cannot be read, parsed,
 * or does not conform to the expected JSON schema.
 *
 * This is a RuntimeException because a bad specification is an
 * unrecoverable, top-level failure for this application: there is nothing
 * meaningful the caller can do except report the problem to the user and
 * exit. Keeping it unchecked avoids forcing every intermediate caller in
 * the Mapper/Transducer chain to declare or swallow it.
 */
public class SpecificationException extends RuntimeException {

    public SpecificationException(String message) {
        super(message);
    }

    public SpecificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
