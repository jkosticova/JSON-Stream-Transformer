package Prototype.StateArchitecture.JsonPushdownAutomaton;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.Set;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.State.Eval;
import Prototype.StateArchitecture.State.FindPos;
import Prototype.StateArchitecture.State.Gen;
import Prototype.StateArchitecture.State.Match;
import Prototype.StateArchitecture.State.MatchPos;
import Prototype.StateArchitecture.State.Memin;
import Prototype.StateArchitecture.State.MeminSkip;
import Prototype.StateArchitecture.State.Memout;
import Prototype.StateArchitecture.State.TraverseSubtree;
import Prototype.Writer.JsonWriter;

/*
Executing part of the transducer 
- owns the state and the stack
- is reponsible for processing events passed by the Jackson parser and
updating state / stack correspondingly
*/

public class JsonPushdownAutomaton {
    private PathAutomaton pa;
    private Stack<StackConfiguration> stack;
    private int stackSize;
    private State currentState;
    private boolean paused;    
    private boolean generating;    

    private String transfType;
    
    

    //reusable states
    final Eval evalState;
    final Match matchState;
    final Gen traverseState;
    final TraverseSubtree traverseSubtreeState;
    final FindPos findPosState;
    final MatchPos matchPosState;    
    final Memin meminState;
    final MeminSkip meminSkipState;
    final Memout memoutState;            
    
    public JsonPushdownAutomaton(String path, String transfType) {
        this.transfType = transfType;
        this.pa = new PathAutomaton(path);
        this.stack = new Stack<>();
        this.stackSize = 0;        

        // states
        evalState = new Eval(null);
        matchState = new Match(null);        
        traverseSubtreeState = new TraverseSubtree(null);
        findPosState = new FindPos(null);
        matchPosState = new MatchPos(null);        
        traverseState = new Traverse(null);        
        
            meminState = null;
            meminSkipState = null;
            memoutState = null;
        
        init();      
    
    }

    private void init() {        
        int newPaState = PathAutomaton.INITIAL_STATE;
        stack.clear();
        // push initial configuration - json value type is set to null since we do not
        // know it yet            
        stack.push(new StackConfiguration(newPaState));
        stackSize++;        
        
        this.currentState = evalState;        
        
        this.proceed();
        this.startGenerating();

    }

    public ProcessingResult process(JsonToken token, String tokenString) {
        currentState.process(token, tokenString);
        return null;
    }
        
    /*
        Stack manipulation methods
    */
    public StackConfiguration getStackPeek() {
        return stack.elementAt(stackSize-1);    
    }

    public boolean stackIsEmpty() {
        return this.stack.empty();
    }

    public void pushScWithTransition(String value, byte valType) {
        StackConfiguration sc = this.getStackPeek();
        int newPaState = pa.transition(sc.getPaState(), value);                         
        // top of the stack reached -> create new SC object
        if (stackSize == stack.size()) {
            stack.push(new StackConfiguration(newPaState, valType));
        }
        // otherwise reuse existing SC object
        else {
            sc = stack.elementAt(stackSize);
            sc.setPaState(newPaState);
            sc.init(valType);
        }
        stackSize++;
    }

    public void pushScWithTransition(int value, byte valType) {
        StackConfiguration sc = this.getStackPeek();
        int newPaState = pa.transition(sc.getPaState(), value);                         
        // top of the stack reached -> create new SC object
        if (stackSize == stack.size()) {
            stack.push(new StackConfiguration(newPaState, valType));
        }
        // otherwise reuse existing SC object
        else {
            sc = stack.elementAt(stackSize);
            sc.setPaState(newPaState);
            sc.init(valType);
        }
        stackSize++;
    }
    
    public void popSc() {
        stackSize--;
    }

    /*
        Path methods
    */
    public boolean isPathMatch() {
        return pa.isFinalState(getStackPeek().getPaState());
    }
    
    /* 
        Methods to access current state
    */
    public State getState() {
        return this.currentState;
    }
         
    public void setState(State state) {
        this.currentState = state;
    }

    /* 
        Methods to access/set other properties
    */    
    public boolean isPaused() {
        return this.paused;
    }

    public void pause() {
        this.paused = true;        
    }

    public void proceed() {
        this.paused = false;        
    }
    
    public boolean isGenerating() {
        return generating;
    }
    
    public void startGenerating() {
        this.generating = true;
    }

    public void stopGenerating() {
        this.generating = false;
    }

    

    public String getTransfType() {
        return this.transfType;
    }


    /*
        Methods to access shared states
    */
    public State getEvalState() {
        return this.evalState;
    }
    
    public State getMatchState() {
        return this.matchState;
    }
            
    public State getTraverseState() {
        return this.traverseState;
    }
    
    public State getTraverseSubtreeState() {
        return this.traverseSubtreeState;
    }
    
    public State getFindPosState() {
        return this.findPosState;
    }
    
    public State getMatchPosState() {
        return this.matchPosState;
    }
    
    public State getMeminState() {
        return this.meminState;
    }
    
    public State getMeminSkipState() {
        return this.meminSkipState;
    }
    
    public State getMemoutState() {
        return this.memoutState;
    }                                     
}
