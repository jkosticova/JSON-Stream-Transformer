package Prototype.StateArchitecture.Transducer;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.State;
import com.fasterxml.jackson.core.JsonGenerator;

import java.util.Stack;

public interface Transducer {
    int INITIAL_PA_STATE = 0;
    int OBJECT_ARR_INDEX = -1;
    
    boolean getPaused();
    void setPaused(boolean paused);

    JsonGenerator getGenerator();    
    void setGenerator(JsonGenerator generator);
    
    // reusable states
    State getEvalState();
    State getMatchState();
    State getFindPosState();
    State getMatchPosState();
    State getSkipSubtreeState();
    State getGenState();
    State getMeminState();
    State getMeminDelState();
    State getMemoutState();
    
    PathAutomaton getPa();
    
    Stack<Integer> getPaStack();

    Stack<Integer> getIndexStack();
   
    String getTransfType();

    TransformationFormat getSpecification();

    boolean isGenerating();
    void setIsGenerating(boolean isGenerating);
    boolean noGen();
    void setNoGen(boolean noGen);
    
    boolean process();

    State getCurrentState();

    void getFromMemory();

    void addToMemory();

    void setState(State state);
}
