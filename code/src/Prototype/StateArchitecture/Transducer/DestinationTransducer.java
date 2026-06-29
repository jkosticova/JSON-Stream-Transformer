package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.PathAutomaton.PathAutomaton;
import Prototype.PathAutomaton.SimplePathAutomaton;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.*;
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
    //states
    private final Eval evalState;
    private final Match matchState;
    private final Gen genState;
    private final Del delState;
    private final Find_i find_iState;
    private final Match_i match_iState;
    private final Memin meminState;
    private final MeminDel meminDelState;
    private final Memout memoutState;
    JsonGenerator generator;
    JsonParser parser;
    PathAutomaton pa;    
    Stack<Integer> paStack;
    Stack<Integer> indexStack;
    TransformationFormat specification;

    public DestinationTransducer(SpecificationMapper mapper, BufferTransducer parentTransducer) {
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
        
        pa = new SimplePathAutomaton(specification.getPath());
        
        paStack = new Stack<>();
        paStack.push(INITIAL_PA_STATE);
        indexStack = new Stack<>();
        
        generator = parentTransducer.generator;
        
        paused = false;
        // states
        evalState = new Eval(this);
        matchState = new Match(this);
        delState = new Del(this);
        find_iState = new Find_i(this);
        match_iState = new Match_i(this);        
        genState = new Gen(this);    
        meminState = new Memin(this);    
        meminDelState = new MeminDel(this);    
        memoutState = new Memout(this);    
        
        currentState = evalState;
        
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
    public State getEvalState() {
        return this.evalState;
    }

    @Override
    public State getMatchState() {
        return this.matchState;
    }
    
    @Override
    public State getGenState() {
        return this.genState;
    }

    @Override
    public State getDelState() {
        return this.delState;
    }

    @Override
    public State getFind_iState() {
        return this.find_iState;
    }

    @Override
    public State getMatch_iState() {
        return this.match_iState;
    }

    @Override
    public State getMeminState() {
        return this.meminState;
    }

    @Override
    public State getMeminDelState() {
        return this.meminDelState;
    }

    @Override
    public State getMemoutState() {
        return this.memoutState;
    }

    @Override
    public JsonGenerator getGenerator() {
        return this.generator;
    }

    @Override
    public PathAutomaton getPa() {
        return this.pa;
    }
    
    @Override
    public Stack<Integer> getPaStack() {
        return this.paStack;
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
