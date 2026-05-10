package Prototype.StateArchitecture.Transducer;

import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.State;
import com.fasterxml.jackson.core.JsonGenerator;

import java.util.Stack;

public interface Transducer {
    void setPaused(boolean paused);

    JsonGenerator getGenerator();

    Stack<String> getPathStack();

    Stack<Integer> getIndexStack();

    TransformationFormat getSpecification();

    boolean process();

    State getCurrentState();

    void getFromMemory();

    void addToMemory();

    void setState(State state);
}
