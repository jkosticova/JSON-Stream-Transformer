package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.PathAutomaton.*;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.*;
import Prototype.Writer.JsonWriter;
import Prototype.Writer.RawUtf8Writer;

import com.fasterxml.jackson.core.JsonFactory;
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
    private final SkipSubtree delState;
    private final FindPos find_iState;
    private final MatchPos match_iState;    
    // stacks
    Stack<Integer> paStack;    
    Stack<Integer> indexStack;    
    JsonParser parser;
    JsonWriter writer;
    
    
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
        
        currentState = evalState;
        paused = false;
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public JsonWriter getWriter() {
        return this.writer;
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
    public boolean noGen() {
        return true;
    }
    
    @Override
    public void setNoGen(boolean noGen) {
            // do nothing
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
    public State getSkipSubtreeState() {
        return this.delState;
    }

    @Override
    public State getFindPosState() {
        return this.find_iState;
    }

    @Override
    public State getMatchPosState() {
        return this.match_iState;
    }

    @Override
    public State getMeminState() {
        return null;
    }

    @Override
    public State getMeminSkipState() {
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
                currentState.process(parser);
                // ked pridem do match stavu, automaticky negenerujem a stojim
                if (currentState.isGenerating()) {
                    writer.writeCurrentEvent(parser);
                    writer.flush();
                }            
                
            }
            parser.close();
            writer.flush();
            //writer.close();
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
