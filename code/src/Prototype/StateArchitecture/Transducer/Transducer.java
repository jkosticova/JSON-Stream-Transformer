package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.PathAutomaton.PathAutomaton;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.Eval;
import Prototype.StateArchitecture.State.FindPos;
import Prototype.StateArchitecture.State.Gen;
import Prototype.StateArchitecture.State.MatchPos;
import Prototype.StateArchitecture.State.MeminSkip;
import Prototype.StateArchitecture.State.MeminSubtree;
import Prototype.StateArchitecture.State.Memout;
import Prototype.StateArchitecture.State.Match;
import Prototype.StateArchitecture.State.SkipSubtree;
import Prototype.StateArchitecture.State.State;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.util.Objects;
import java.util.Stack;

public abstract class Transducer {
    public static final int INITIAL_PA_STATE = 0;
    public static final int OBJECT_ARR_INDEX = -1;
    public static byte SRC_TRANSDUCER = 0;
    public static byte DEST_TRANSDUCER = 1;

    // transducer state
    protected State currentState;
    protected boolean paused;        

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

    // I/O
    JsonGenerator generator;
    JsonParser parser;


    // resuable states (must be reused due to measuremennt nethod that counts all memory allocations)
    protected Eval evalState;
    protected Match matchState;
    protected Gen genState;
    protected SkipSubtree skipSubtree;
    protected FindPos findPosState;
    protected MatchPos matchPosState;    
    protected MeminSubtree meminSubtreeState;    
    protected MeminSkip meminSkipState;    
    protected Memout memoutState;    

    public Transducer(SpecificationMapper mapper, boolean source) {
        
        paused = false;

        this.specification = mapper.getTransformationFormat();       
        this.transformationType = specification.getType();
        
        if (source) {            
            this.path = specification.getPath();
            this.transducerRole = SRC_TRANSDUCER;
        }
        else if (transformationType.equals("copy")) {
                this.path = ((CopyTransformation) specification).getDestPath();
                this.transducerRole = DEST_TRANSDUCER;
            }
        else if (transformationType.equals("move")) {
                this.path = ((MoveTransformation) specification).getDestPath();
                this.transducerRole = DEST_TRANSDUCER;
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
        skipSubtree = new SkipSubtree(this);
        findPosState = new FindPos(this);
        matchPosState = new MatchPos(this);        
        genState = new Gen(this);
        meminSubtreeState = new MeminSubtree(this);
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
        return this.skipSubtree;
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
    
    public State getMeminSkipState() {
        return this.meminSkipState;
    }
    
    public State getMemoutState() {
        return this.memoutState;
    }
    
    public Stack<Integer> getPaStack() {
        return this.paStack;
    }

    
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
