package prototype.stateArchitecture.transducer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

public final class IoHandler {

    private IoHandler() {
    }

    public static JsonParser createParser(
            JsonFactory factory,
            InputStream input) {

        try {
            return factory.createParser(input);
        } catch (IOException e) {
            throw new TransducerException(
                    "Could not open input stream for parsing", e);
        }
    }

    public static JsonGenerator createGenerator(
            JsonFactory factory,
            OutputStream output) {

        try {
            return factory.createGenerator(output)
                    .useDefaultPrettyPrinter();
        } catch (IOException e) {
            throw new TransducerException(
                    "Could not open output stream for generation", e);
        }
    }

    public static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (Exception e) {
            System.err.println(
                    "Warning: failed to close resource cleanly: "
                            + e.getMessage());
        }
    }
}