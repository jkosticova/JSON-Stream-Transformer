package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.Eval;
import Prototype.StateArchitecture.State.State;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

public class StackTransducer implements Transducer {
    private State currentState;
    private boolean paused;
    JsonGenerator generator;
    JsonParser parser;
    Stack<String> pathStack;
    Stack<Integer> indexStack;
    TransformationFormat specification;

    public StackTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        specification = mapper.getTransformationFormat();
        pathStack = new Stack<>();
        indexStack = new Stack<>();
        JsonFactory factory = new JsonFactory();
        try {
            parser = factory.createParser(inputStream);
            generator = factory.createGenerator(outputStream).useDefaultPrettyPrinter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        currentState = new Eval(this);
        paused = false;
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public JsonGenerator getGenerator() {
        return this.generator;
    }

    @Override
    public Stack<String> getPathStack() {
        return this.pathStack;
    }

    @Override
    public Stack<Integer> getIndexStack() {
        return this.indexStack;
    }

    @Override
    public TransformationFormat getSpecification() {
        return this.specification;
    }

    public boolean process() {
        try {
            JsonToken event = null;

            while (!indexStack.empty()) indexStack.pop();
            while (!pathStack.empty()) pathStack.pop();
            pathStack.push("$");

            while (!parser.isClosed()) {
                if (!paused) {
                    event = parser.nextToken();
                }
                if (event == null || pathStack.isEmpty()) break;
                currentState.process(event, parser);
            }

            parser.close();
            generator.close();
        } catch (Exception e) {
            System.out.println("Issue while processing StackTransducer: " + e.getMessage());
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
