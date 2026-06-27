package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.Gen;
import Prototype.StateArchitecture.State.State;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

public class IdentityTransducer implements Transducer {
    private State currentState;
    JsonGenerator generator;
    JsonParser parser;
    TransformationFormat specification;

    public IdentityTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        specification = mapper.getTransformationFormat();
        JsonFactory factory = new JsonFactory();
        try {
            parser = factory.createParser(inputStream);
            generator = factory.createGenerator(outputStream).useDefaultPrettyPrinter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        currentState = new Gen(this);
    }

    @Override
    public void setPaused(boolean paused) {
    }

    @Override
    public JsonGenerator getGenerator() {
        return this.generator;
    }

    @Override
    public Stack<Integer> getPaStack() {
        return null;
    }

    @Override
    public Stack<Integer> getIndexStack() {
        return null;
    }

    @Override
    public TransformationFormat getSpecification() {
        return this.specification;
    }

    public boolean process() {
        try {
            JsonToken event;

            while (!parser.isClosed()) {
                event = parser.nextToken();

                if (event == null) break;
                currentState.process(event, parser);
            }

            parser.close();
            generator.close();
        } catch (Exception e) {
            System.out.println("Issue while processing IdentityTransducer: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public State getCurrentState() {
        return this.currentState;
    }

    @Override
    public void getFromMemory() {
    }

    @Override
    public void addToMemory() {
    }

    @Override
    public void setState(State state) {
        this.currentState = state;
    }
}
