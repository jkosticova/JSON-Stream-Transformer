package prototype.stateArchitecture.transducer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import prototype.mapper.SpecificationMapper;
import prototype.stateArchitecture.state.freeTraversal.Gen;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IdentityTransducer extends Transducer {
    JsonGenerator generator;
    JsonParser parser;

    public IdentityTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        super(mapper, SIMPLE_TRANSDUCER);

        JsonFactory factory = new JsonFactory();

        // Created separately (rather than in one try) so that if the
        // generator fails to open after the parser succeeded, we still
        // close the parser instead of leaking the open InputStream.
        try {
            parser = factory.createParser(inputStream);
        } catch (IOException e) {
            throw new TransducerException("Could not open input stream for parsing", e);
        }

        try {
            generator = factory.createGenerator(outputStream).useDefaultPrettyPrinter();
        } catch (IOException e) {
            closeQuietly(parser);
            throw new TransducerException("Could not open output stream for generation", e);
        }

        initStates();
        currentState = new Gen(this);
    }

    @Override
    public boolean process() {
        boolean success = true;
        try {
            JsonToken event;

            while (!parser.isClosed()) {
                event = parser.nextToken();

                if (event == null) break;
                currentState.process(parser);
                if (this.getGenerating()) {
                    generator.copyCurrentEvent(parser);
                }
            }
        } catch (IOException e) {
            System.err.println("Issue while processing IdentityTransducer: " + e);
            success = false;
        } finally {
            // Always attempt to release both streams, whether processing
            // succeeded or failed, so a failure here doesn't leak file
            // handles on top of the original problem.
            closeQuietly(parser);
            closeQuietly(generator);
        }
        return success;
    }

    public void moveToValue() {
        return;
    }

    public void processValueAfterMatch() {
        return;
    }

    /**
     * Closes a Closeable, logging any failure instead of throwing, so that
     * cleanup of one resource can't mask an already-in-flight exception or
     * prevent cleanup of the other resource.
     */
    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            System.err.println("Warning: failed to close resource cleanly: " + e.getMessage());
        }
    }

}
