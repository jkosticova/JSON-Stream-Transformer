package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.Eval;
import Prototype.StateArchitecture.State.State;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

public class DestinationTransducer implements Transducer {
    private State currentState;
    private final BufferTransducer parentTransducer;
    private boolean paused;
    JsonGenerator generator;
    JsonParser parser;
    Stack<String> pathStack;
    Stack<Integer> indexStack;
    TransformationFormat specification;

    public DestinationTransducer(SpecificationMapper mapper, InputStream inputStream, TokenBuffer tokenBuffer, BufferTransducer parentTransducer) {
        if (mapper.getTransformationFormat() instanceof CopyTransformation oldSpecification) {
            CopyTransformation newSpecification = new CopyTransformation();
            newSpecification.setIndex(oldSpecification.getIndex());
            newSpecification.setPath(oldSpecification.getDestPath());
            newSpecification.setValue(oldSpecification.getValue());
            newSpecification.setType(oldSpecification.getType());
            newSpecification.setKey(oldSpecification.getKey());
            specification = newSpecification;
        } else if (mapper.getTransformationFormat() instanceof MoveTransformation oldSpecification) {
            MoveTransformation newSpecification = new MoveTransformation();
            newSpecification.setIndex(oldSpecification.getIndex());
            newSpecification.setPath(oldSpecification.getDestPath());
            newSpecification.setValue(oldSpecification.getValue());
            newSpecification.setType(oldSpecification.getType());
            newSpecification.setKey(oldSpecification.getKey());
            specification = newSpecification;
        }

        this.parentTransducer = parentTransducer;


        pathStack = new Stack<>();
        pathStack.push("$");
        indexStack = new Stack<>();
        JsonFactory factory = new JsonFactory();
        try {
            parser = factory.createParser(inputStream);
            generator = tokenBuffer;
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

    public boolean getPaused() {
        return this.paused;
    }

    public void setGenerator(JsonGenerator generator) {
        this.generator = generator;
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
        return true;
    }

    @Override
    public State getCurrentState() {
        return this.currentState;
    }

    @Override
    public void getFromMemory() {
        parentTransducer.getFromMemory();
    }

    @Override
    public void addToMemory() {
        parentTransducer.addToMemory();
    }

    @Override
    public void setState(State state) {
        this.currentState = state;
    }
}
