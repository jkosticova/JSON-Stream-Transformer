package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.State.Sync;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BufferTransducer {
    private final State currentState;
    private final SourceTransducer sourceTransducer;
    private final DestinationTransducer destinationTransducer;

    private boolean paused;
    JsonGenerator generator;
    JsonParser parser;
    TokenBuffer buffer;
    TransformationFormat specification;

    public BufferTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        try {
            JsonFactory factory = new JsonFactory();
            parser = factory.createParser(inputStream);
            generator = factory.createGenerator(outputStream).useDefaultPrettyPrinter();
            specification = mapper.getTransformationFormat();

            paused = false;

            ObjectMapper objectMapper = new ObjectMapper();
            TokenBuffer sourceOutputStream = new TokenBuffer(objectMapper, false);
            TokenBuffer destinationOutputStream = new TokenBuffer(objectMapper, false);
            buffer = new TokenBuffer(objectMapper, false);

            sourceTransducer = new SourceTransducer(mapper, inputStream, sourceOutputStream, this);
            destinationTransducer = new DestinationTransducer(mapper, inputStream, destinationOutputStream, this);
            currentState = new Sync(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getFromMemory() {
        try {
            buffer.serialize(generator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addToMemory() {
        try {
            buffer.copyCurrentEvent(parser);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public JsonGenerator getGenerator() {
        return this.generator;
    }

    public TransformationFormat getSpecification() {
        return this.specification;
    }

    public SourceTransducer getSourceTransducer() {
        return this.sourceTransducer;
    }

    public DestinationTransducer getDestinationTransducer() {
        return this.destinationTransducer;
    }

    public boolean process() {
        try {
            JsonToken event = null;

            while (!parser.isClosed()) {
                if (!paused) {
                    event = parser.nextToken();
                }
                if (event == null) break;
                currentState.process(event, parser);
            }

            parser.close();
            generator.close();
        } catch (Exception e) {
            System.out.println("Issue while processing BufferTransducer: " + e.getMessage());
            return false;
        }
        return true;
    }
}
