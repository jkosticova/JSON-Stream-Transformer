package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.PathAutomaton.PathAutomaton;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.Eval;
import Prototype.StateArchitecture.State.FindPos;
import Prototype.StateArchitecture.State.MatchPos;
import Prototype.StateArchitecture.State.Memout;
import Prototype.StateArchitecture.State.Match;
import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.State.FreeTraversal.Gen;
import Prototype.StateArchitecture.State.FreeTraversal.MeminSkip;
import Prototype.StateArchitecture.State.SubtreeTraversal.SubtreeGen;
import Prototype.StateArchitecture.State.SubtreeTraversal.SubtreeMemin;
import Prototype.StateArchitecture.State.SubtreeTraversal.SubtreeSkip;
import Prototype.StateArchitecture.State.SubtreeTraversal.SubtreeSkipMemin;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.util.Stack;

/* Abstract class for StackTransducer and IdentityTransducer
 */

public abstract class Transducer {
    public static final int INITIAL_PA_STATE = 0;
    public static final int OBJECT_ARR_INDEX = -1;
    
    public static byte SIMPLE_TRANSDUCER = 0;
    public static byte SRC_TRANSDUCER = 1;
    public static byte DEST_TRANSDUCER = 2;

    // transducer state
    protected State currentState;
    protected boolean paused;        
    protected boolean generating;        

    // other properties - axctually final, but are created in child classes
    protected byte transducerRole;
    protected TransformationFormat specification;
    protected BufferTransducer parentTransducer;        
    protected String transformationType;

    // stacks
    Stack<Integer> paStack;    
    Stack<Integer> indexStack;    
        
    PathAutomaton pa;    
    String path;       
    public Byte matchType = State.NO_MATCH;
    public boolean moveToValue = false;

    // I/O
    JsonGenerator generator;
    JsonParser parser;


    // resuable states (must be reused due to measuremennt nethod that counts all memory allocations)
    protected Eval evalState;
    protected Match matchState;
    protected Gen genState;
    protected SubtreeSkip skipSubtreeState;
    protected SubtreeGen genSubtreeState;
    protected FindPos findPosState;
    protected MatchPos matchPosState;    
    protected SubtreeMemin meminSubtreeState;    
    protected SubtreeSkipMemin meminSkipSubtreeState;    
    protected MeminSkip meminSkipState;    
    protected Memout memoutState;    

    public Transducer(SpecificationMapper mapper, byte role) {
        
        paused = false;
        generating = true;

        this.specification = mapper.getTransformationFormat();       
        this.transformationType = specification.getType();
        this.transducerRole = role;

        // simple and source transducer
        if (this.transducerRole != DEST_TRANSDUCER) {            
            this.path = specification.getPath();
            
        }
        // destination transducer
        else if (transformationType.equals("copy")) {
                this.path = ((CopyTransformation) specification).getDestPath();                
            }
        else if (transformationType.equals("move")) {
                this.path = ((MoveTransformation) specification).getDestPath();                
            }
        else {
            this.path = null;
                // TODO exception
            }                    
    }

    // states must be initalized outside constructor, because they need initialized fields from subclasses' constructors
    protected void initStates() {
        evalState = new Eval(this);
        matchState = new Match(this);
        skipSubtreeState = new SubtreeSkip(this);
        genSubtreeState = new SubtreeGen(this);
        findPosState = new FindPos(this);
        matchPosState = new MatchPos(this);        
        genState = new Gen(this);
        meminSubtreeState = new SubtreeMemin(this);
        meminSkipSubtreeState = new SubtreeSkipMemin(this);
        meminSkipState = new MeminSkip(this);
        memoutState = new Memout(this);
    }

    public abstract boolean process();
    
    
    public byte getTransducerRole() {
        return this.transducerRole;
    }
        
    public void setTransducerRole(byte type) {
        this.transducerRole = type;
    }
    
    public TransformationFormat getSpecification() {
        return this.specification;
    }
    
    public boolean getPaused() {
        return this.paused;
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    public JsonGenerator getGenerator() {
        return this.generator;
    }
    
    public void setGenerator(JsonGenerator generator) {
        this.generator = generator;
    }
    
    public State getEvalState() {
        return this.evalState;
    }
    
    public State getMatchState() {
        return this.matchState;
    }
        
    public State getGenState() {
        return this.genState;
    }
    
    public State getSkipSubtreeState() {
        return this.skipSubtreeState;
    }

    public State getGenSubtreeState() {
        return this.genSubtreeState;
    }
    
    public State getFindPosState() {
        return this.findPosState;
    }
    
    public State getMatchPosState() {
        return this.matchPosState;
    }
    
    public State getMeminSubtreeState() {
        return this.meminSubtreeState;
    }

    public State getMeminSkipSubtreeState() {
        return this.meminSkipSubtreeState;
    }
    
    public State getMeminSkipState() {
        return this.meminSkipState;
    }
    
    public State getMemoutState() {
        return this.memoutState;
    }
    
    public Stack<Integer> getPaStack() {
        return this.paStack;
    }

    public abstract void moveToValue();

    public void setFirstMatch(byte firstMatch) {
        if (this.parentTransducer != null) {
            this.parentTransducer.setFirstMatch(firstMatch);
        }
    }

    public byte getFirstMatch() {
        if (parentTransducer != null) {
            return parentTransducer.getFirstMatch();
        }
        else return BufferTransducer.NONE;
    }

    public void setGenerating(boolean generating) {
        this.generating = generating;
    }

    public boolean getGenerating() {
        return this.generating;
    }

    public abstract void processValueAfterMatch();

    

    
    public PathAutomaton getPa() {
        return this.pa;
    }

    
    public Stack<Integer> getIndexStack() {
        return this.indexStack;    }


    
    public String getTransformationType() {
        return this.transformationType;
    }    

    
    public State getCurrentState() {
        return this.currentState;
    }

    
    public void getFromMemory() {
        parentTransducer.getFromMemory();
    }

    
    public void addToMemory() {
        parentTransducer.addToMemory();
    }

    
    public void setState(State state) {
        this.currentState = state;
    }
}
