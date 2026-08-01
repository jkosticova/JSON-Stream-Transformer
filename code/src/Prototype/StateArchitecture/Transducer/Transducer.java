package Prototype.StateArchitecture.Transducer;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.State;
import Prototype.Writer.JsonWriter;

import java.util.Stack;

public interface Transducer {
    int INITIAL_PA_STATE = 0;
    int OBJECT_ARR_INDEX = -1;
    
    void setPaused(boolean paused);
    
    JsonWriter getWriter();    
    
    State getEvalState();
    State getMatchState();
    State getFindPosState();
    State getMatchPosState();
    State getSkipSubtreeState();    
    State getGenState();
    State getMeminState();
    State getMeminSkipState();
    State getMemoutState();
    
    PathAutomaton getPa();
    
    Stack<Integer> getPaStack();

    Stack<Integer> getIndexStack();
   
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
