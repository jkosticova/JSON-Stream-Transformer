package Prototype.StateArchitecture.Transducer;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.State;
import com.fasterxml.jackson.core.JsonGenerator;

import java.util.Stack;

public interface Transducer {
    int INITIAL_PA_STATE = 0;
    int OBJECT_ARR_INDEX = -1;
    
    void setPaused(boolean paused);

    JsonGenerator getGenerator();    
    
    State getEvalState();
    State getMatchState();
    State getFind_iState();
    State getMatch_iState();
    State getDelState();
    State getGenState();
    State getMeminState();
    State getMeminDelState();
    State getMemoutState();
    
    PathAutomaton getPa();
    
    Stack<Integer> getPaStack();

    Stack<Integer> getIndexStack();
   
    TransformationFormat getSpecification();

    boolean process();

    State getCurrentState();

    void getFromMemory();

    void addToMemory();

    void setState(State state);
}
