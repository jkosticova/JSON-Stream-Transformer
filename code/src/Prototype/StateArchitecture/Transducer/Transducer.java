package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.PathAutomaton.PathAutomaton;
import Prototype.PathAutomaton.SimplePathAutomaton;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.Eval;
import Prototype.StateArchitecture.State.FindPos;
import Prototype.StateArchitecture.State.Gen;
import Prototype.StateArchitecture.State.Match;
import Prototype.StateArchitecture.State.MatchPos;
import Prototype.StateArchitecture.State.Memin;
import Prototype.StateArchitecture.State.MeminSkip;
import Prototype.StateArchitecture.State.Memout;
import Prototype.StateArchitecture.State.SkipSubtree;
import Prototype.StateArchitecture.State.State;
import Prototype.Writer.JsonWriter;
import Prototype.Writer.RawUtf8Writer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

public abstract class Transducer {
    int INITIAL_PA_STATE = 0;
    int OBJECT_ARR_INDEX = -1;
    State currentState;
    boolean paused;
    
    //states
    final Eval evalState;
    final Match matchState;
    final Gen genState;
    final SkipSubtree delState;
    final FindPos find_iState;
    final MatchPos match_iState;    
    final Memin meminState;
    final MeminSkip meminDelState;
    final Memout memoutState;    
    boolean isGenerating = true;
    boolean noGen = false;
    // stacks
    Stack<Integer> paStack;    
    Stack<Integer> indexStack;    
    JsonParser parser;
    JsonWriter writer;
    MatchEntryMode entryMode;
    PathAutomaton pa;
    TransformationFormat specification;
    BufferTransducer parentTransducer = null;

    public enum MatchEntryMode {
        AT_KEY,
        AT_VALUE
    }

    public Transducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        specification = mapper.getTransformationFormat();        
        // stacks
        paStack = new Stack<>();        
        indexStack = new Stack<>();        
        pa = new SimplePathAutomaton(specification.getPath()); 
        entryMode = null;

        
        JsonFactory factory = new JsonFactory();
        try {
            parser = factory.createParser(inputStream);
            RawUtf8Writer rawWriter = new RawUtf8Writer(outputStream);
            this.writer = new JsonWriter(rawWriter);            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // states
        evalState = new Eval(this);
        matchState = new Match(this);
        delState = new SkipSubtree(this);
        find_iState = new FindPos(this);
        match_iState = new MatchPos(this);        
        genState = new Gen(this);        
        meminState = null;
        meminDelState = null;
        memoutState = null;
        
        

        
        currentState = evalState;
        paused = false;
    };

     public Transducer(SpecificationMapper mapper, BufferTransducer parentTransducer) {
     
        this.parentTransducer = parentTransducer;
        pa = new SimplePathAutomaton(specification.getPath()); 
        paStack = new Stack<>();
        paStack.push(INITIAL_PA_STATE);
        indexStack = new Stack<>();
         
        writer = parentTransducer.getWriter();
        
        paused = false;
        // states
        evalState = new Eval(this);
        matchState = new Match(this);
        delState = new SkipSubtree(this);
        find_iState = new FindPos(this);
        match_iState = new MatchPos(this);        
        genState = new Gen(this);    
        meminState = new Memin(this);    
        meminDelState = new MeminSkip(this);    
        memoutState = new Memout(this);            
        

        currentState = evalState;
        isGenerating= true;
        noGen = false;


    }
    
    public boolean process() {
        return true;
    }
        
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean getPaused() {
        return this.paused;
    }

    
    public void setEntryMode(MatchEntryMode mode) {
        this.entryMode = mode;
    }
    
    
    public MatchEntryMode getEntryMode() {
        return entryMode;
    }

    
    public JsonWriter getWriter() {
        return this.writer;
    }

    public void setWriter(JsonWriter writer) {
        this.writer = writer;
    }

    
    public boolean isGenerating() {
        return true;
    }

    
    public void setIsGenerating(boolean isGenerating) {
           return;
    }

    
    public boolean noGen() {
        return true;
    }
    
    
    public void setNoGen(boolean noGen) {
            // do nothing
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
        return this.delState;
    }

    
    public State getFindPosState() {
        return this.find_iState;
    }

    
    public State getMatchPosState() {
        return this.match_iState;
    }

    
    public State getMeminState() {
        return null;
    }

    
    public State getMeminSkipState() {
        return null;
    }

    
    public State getMemoutState() {
        return null;
    }

    
    public Stack<Integer> getPaStack() {
        return this.paStack;
    }

    
    public PathAutomaton getPa() {
        return this.pa;
    }

    
    public Stack<Integer> getIndexStack() {
        return this.indexStack;    }


    
    public TransformationFormat getSpecification() {
        return this.specification;
    }
    

    public State getCurrentState() {
        return this.currentState;
    }

   
   
   
    public void setState(State state) {
        this.currentState = state;
    }
    
    public void getFromMemory() {
        if (parentTransducer!=null) parentTransducer.getFromMemory();
    }

    public void addToMemory() {
        if (parentTransducer!=null) parentTransducer.addToMemory();
    }

    
    

    
    

}
