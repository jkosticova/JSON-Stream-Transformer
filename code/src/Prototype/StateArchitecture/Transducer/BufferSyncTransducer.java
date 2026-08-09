package prototype.stateArchitecture.transducer;

import prototype.mapper.SpecificationMapper;

import prototype.specificationParser.*;

import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.state.Sync;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BufferSyncTransducer {
    private final State currentState;
    private final StackTransducer sourceTransducer;
    private final StackTransducer destinationTransducer;
    private byte firstMatch;

    public static final byte NONE = 0;
    public static final byte SRC_FIRST = 1;
    public static final byte DEST_FIRST = 2;

    private boolean paused;
    JsonGenerator generator;
    JsonParser parser;
    TokenBuffer buffer;
    TransformationFormat specification;
    public boolean generateFromSource;

    public BufferSyncTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        specification = mapper.getTransformationFormat();
        paused = false;

        JsonFactory factory = new JsonFactory();
        parser = IoHandler.createParser(factory, inputStream);
        
        // TODO - close parse if generator creation fails
        generator = IoHandler.createGenerator(factory, outputStream);

        buffer = new TokenBuffer((ObjectCodec) null, false);
        sourceTransducer = new BufferStackTransducer(mapper, this, BufferStackTransducer.SRC_TRANSDUCER);
        destinationTransducer = new BufferStackTransducer(mapper, this, BufferStackTransducer.DEST_TRANSDUCER);
        this.firstMatch = NONE;

        currentState = new Sync(this);
    }

    public void getFromMemory() {
        try {
            buffer.serialize(generator);
        } catch (IOException e) {
            throw new TransducerException("Failed to write buffered tokens to output", e);
        }
    }

    public void addToMemory() {
        try {
            buffer.copyCurrentEvent(parser);            
        } catch (IOException e) {
            throw new TransducerException("Failed to buffer current token from input", e);
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setFirstMatch(byte firstMatch) {
        this.firstMatch = firstMatch;
    }

    public byte getFirstMatch() {
        return this.firstMatch;
    }

    public JsonGenerator getGenerator() {
        return this.generator;
    }

    public TransformationFormat getSpecification() {
        return this.specification;
    }

    public StackTransducer getSourceTransducer() {
        return this.sourceTransducer;
    }

    public StackTransducer getDestinationTransducer() {
        return this.destinationTransducer;
    }

    /**
     * Runs the transducer to completion.
     *
     * @return true if processing completed and the input was fully consumed;
     *         false if a stream-level I/O error was encountered. Any error
     *         indicating a bug in the transducer logic itself (a
     *         RuntimeException that isn't a TransducerException) is NOT
     *         swallowed here and instead propagates to the caller, since
     *         returning false for it would hide the real problem.
     */
    public boolean process() {
        boolean success = true;
        try {
            JsonToken event = null;

            while (!parser.isClosed()) {
                setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                if (!paused) {
                    event = parser.nextToken();
                }
                if (event == null)
                    break;
                currentState.process(parser);
                if (sourceTransducer.getGenerating() &&
                        destinationTransducer.getGenerating()) {
                    generator.copyCurrentEvent(parser);
                }
                generator.flush();
            }
            generator.flush();
        } catch (IOException e) {
            System.err.println("Issue while processing BufferTransducer: " + e);
            success = false;
        } finally {
            // Always attempt to release both streams, whether processing
            // succeeded or failed, so a failure here doesn't leak file
            // handles on top of the original problem.
            IoHandler.closeQuietly(parser);
            IoHandler.closeQuietly(generator);
        }
        return success;
    }

    public void moveToValue(byte transducerRole) {
        try {
            parser.nextToken();
        } catch (IOException e) {
            throw new TransducerException("Failed to advance parser while moving to value", e);
        }

        // process value by the other transducer if not paused
        if (transducerRole == BufferStackTransducer.SRC_TRANSDUCER && !destinationTransducer.getPaused()) {
            destinationTransducer.getCurrentState().process(parser);
        } else if (transducerRole == BufferStackTransducer.DEST_TRANSDUCER && !sourceTransducer.getPaused()) {
            sourceTransducer.getCurrentState().process(parser);
        }
    }

    

}
