package prototype.stateArchitecture.transducer;

import com.fasterxml.jackson.core.JsonFactory;

import com.fasterxml.jackson.core.JsonToken;

import prototype.mapper.SpecificationMapper;
import prototype.pathAutomaton.*;
import prototype.stateArchitecture.state.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

public class StackTransducer extends Transducer {


    /* constructor for a single STACK transformation */
    public StackTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        super(mapper, SIMPLE_TRANSDUCER);

        parentTransducer = null;
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

        // stacks
        this.paStack = new Stack<>();
        this.indexStack = new Stack<>();

        pa = new SimplePathAutomaton(path);
        paStack.push(INITIAL_PA_STATE);

        initStates();

        currentState = evalPathState;
    }

    /* constructor for a single COPY or MOVE transformation */
    public StackTransducer(SpecificationMapper mapper, BufferTransducer parentTransducer, byte role) {
        super(mapper, role);

        this.parentTransducer = parentTransducer;
        this.parser = parentTransducer.parser;
        this.generator = parentTransducer.generator;

        // stacks
        paStack = new Stack<>();
        indexStack = new Stack<>();

        pa = new SimplePathAutomaton(path);
        paStack.push(INITIAL_PA_STATE);

        initStates();

        currentState = evalPathState;

    }

    // used only for stack transformations
    // (StackTransducers created with the COPY/MOVE constructor above are
    // driven state-by-state via Sync/BufferTransducer instead - this
    // override is only ever invoked on a transducer that owns its own
    // parser/generator, so closing both streams below is safe.)
    @Override
    public boolean process() {
        boolean success = true;
        try {
            JsonToken event = null;

            // while the input is being read
            while (!parser.isClosed()) {
                if (!paused) {
                    event = parser.nextToken();
                }
                // EOF && empty stack
                if (event == null || paStack.isEmpty()) {
                    break;
                }
                this.getCurrentState().process(parser);
                // generovanie
                if (this.generating) {
                    generator.copyCurrentEvent(parser);
                }
                generator.flush();
            }
            generator.flush();
        } catch (IOException e) {
            System.err.println("Issue while processing StackTransducer: " + e);
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
        try {
            parser.nextToken(); // move to value and if it is a structure, process opening token
        } catch (IOException e) {            
            throw new TransducerException("Failed to advance parser while moving to value", e);
        }

        if (parser.currentToken() == JsonToken.START_ARRAY) {
            paStack.push(State.ARR_MARKER);
            indexStack.push(0);
        }
        else if (parser.currentToken() == JsonToken.START_OBJECT) {
            paStack.push(State.OBJ_MARKER);
        }
    }

    public void processValueAfterMatch() {
        // in case of a fieldname match, we move to the corresponding value
        if (parser.currentToken() == JsonToken.FIELD_NAME) {
            // SIMPLE transducer - use current method
            if (transducerRole == SIMPLE_TRANSDUCER) {
                moveToValue();
            }
            // SRC and DEST transducer - movement must be synchronized by buffer transducer
            // parentTransducer.moveToValue(...) throws TransducerException on
            // failure (see BufferTransducer), which propagates naturally
            // from here - no separate handling needed.
            else {
                parentTransducer.moveToValue(this.transducerRole);
            }
        }
        // we process the start of current value with next state
        paused = true;
        // we do not generate the start of the current value
        generating = false;
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
