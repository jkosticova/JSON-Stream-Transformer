package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.PathAutomaton.*;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.*;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

public class StackTransducer implements Transducer {
    private State currentState;
    private boolean paused;
    
    //states
    private final Eval evalState;
    private final Match matchState;
    private final Gen genState;
    private final Del delState;
    private final Find_i find_iState;
    private final Match_i match_iState;    
    // stacks
    Stack<Integer> paStack;    
    Stack<Integer> indexStack;    
    JsonGenerator generator;
    JsonParser parser;
    
    PathAutomaton pa;
    TransformationFormat specification;

    public StackTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        specification = mapper.getTransformationFormat();        
        // stacks
        paStack = new Stack<>();        
        indexStack = new Stack<>();        
        pa = new SimplePathAutomaton(specification.getPath()); 

        
        JsonFactory factory = new JsonFactory();
        try {
            parser = factory.createParser(inputStream);
            generator = factory.createGenerator(outputStream).useDefaultPrettyPrinter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // states
        evalState = new Eval(this);
        matchState = new Match(this);
        delState = new Del(this);
        find_iState = new Find_i(this);
        match_iState = new Match_i(this);        
        genState = new Gen(this);
        
        currentState = evalState;
        paused = false;
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public JsonGenerator getGenerator() {
        return this.generator;
    }

    @Override
    public boolean isGenerating() {
        return true;
    }

    @Override
    public void setIsGenerating(boolean isGenerating) {
           return;
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
        return null;
    }

    @Override
    public State getMeminDelState() {
        return null;
    }

    @Override
    public State getMemoutState() {
        return null;
    }

    @Override
    public Stack<Integer> getPaStack() {
        return this.paStack;
    }

    @Override
    public PathAutomaton getPa() {
        return this.pa;
    }

    @Override
    public Stack<Integer> getIndexStack() {
        return this.indexStack;    }


    @Override
    public TransformationFormat getSpecification() {
        return this.specification;
    }

    public boolean process() {
        try {
            JsonToken event = null;

            // inicializacia stackov - aby boli prazdne
            paStack.clear();
            indexStack.clear();
            
            paStack.push(INITIAL_PA_STATE);

            // kym sa cita nieco zo vstupu
            while (!parser.isClosed()) {
                if (!paused) {
                    event = parser.nextToken();
                }
                // EOF && prazdny stack
                if (event == null || paStack.isEmpty()) break;
                currentState.process(event, parser);
            }
            parser.close();
            generator.close();
        } catch (Exception e) {
            System.out.println("Issue while processing StackTransducer: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public State getCurrentState() {
        return this.currentState;
    }

    @Override
    public void getFromMemory() {
    }

    @Override
    public void addToMemory() {
    }

    @Override
    public void setState(State state) {
        this.currentState = state;
    }
}
