package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.Mapper;
import Prototype.Mapper.SpecificationMapper;
import Prototype.PathAutomaton.*;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
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
    
    // resuable states (must be reused due to measuremennt nethod that counts all memory allocations)
    private final Eval evalState;
    private final Match matchState;
    private final Gen genState;
    private final SkipSubtree skipSubtree;
    private final FindPos findPosState;
    private final MatchPos matchPosState;    
    
    // stacks
    Stack<Integer> paStack;    
    Stack<Integer> indexStack;    
    
    JsonGenerator generator;
    JsonParser parser;
    
    PathAutomaton pa;    
    BufferTransducer parentTransducer;

    TransformationFormat specification;

    String path;
    String transfType;

    boolean isGenerating;
    boolean noGen;

    public StackTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {        
        // stacks
        paStack = new Stack<>();        
        indexStack = new Stack<>();        
        
        parentTransducer = null;        
        
        this.specification = mapper.getTransformationFormat();
        this.path = specification.getPath();
        this.transfType = specification.getType();

        pa = new SimplePathAutomaton(path); 
        
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
        skipSubtree = new SkipSubtree(this);
        findPosState = new FindPos(this);
        matchPosState = new MatchPos(this);        
        genState = new Gen(this);
        
        currentState = evalState;
        paused = false;        
        isGenerating = true;
        noGen = true;

        paStack.push(INITIAL_PA_STATE);
    }

    // for copy and move transformations, processing is delegated to the parent buffer transducer
    public StackTransducer(SpecificationMapper mapper, BufferTransducer parentTransducer, boolean source) {        
        // stacks
        paStack = new Stack<>();        
        indexStack = new Stack<>();        
        
        this.parentTransducer = parentTransducer;
        this.parser = parentTransducer.parser;
        this.generator = parentTransducer.generator;

        this.specification = mapper.getTransformationFormat();
        this.transfType = specification.getType();
        if (source) {
            this.path = specification.getPath();
        }
        else if (transfType == "copy") {
                this.path = ((CopyTransformation) specification).getDestPath();
            }
        else if (transfType == "move") {
                this.path = ((MoveTransformation) specification).getDestPath();
            }
        else {
            this.path = null;
                // TODO exception
            }    
        pa = new SimplePathAutomaton(path);         
        
        // states
        evalState = new Eval(this);
        matchState = new Match(this);
        skipSubtree = new SkipSubtree(this);
        findPosState = new FindPos(this);
        matchPosState = new MatchPos(this);        
        genState = new Gen(this);
        
        currentState = evalState;
        paused = false;
        isGenerating = true;
        noGen = true;    

        paStack.push(INITIAL_PA_STATE);
    }

    // used only for stack transformations
    public boolean process() {
        try {
            JsonToken event = null;
                        
            // while the input is being read
            while (!parser.isClosed()) {
                if (!paused) {
                    event = parser.nextToken();
                }
                // EOF && empty stack
                if (event == null || paStack.isEmpty()) {
                    break;
                }
                currentState.process(parser);                
                try {
                    if (this.getCurrentState().isGenerating())
                        generator.copyCurrentEvent(parser);
                    }
                catch (IOException e) {
                    e.printStackTrace();
                }
                generator.flush();
            }
            generator.flush();
            parser.close();
            generator.close();
        } catch (Exception e) {
            System.out.println("Issue while processing StackTransducer: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public TransformationFormat getSpecification() {
        return this.specification;
    }

    @Override
    public boolean getPaused() {
        return this.paused;
    }

    @Override
    public JsonGenerator getGenerator() {
        return this.generator;
    }

    @Override
    public void setGenerator(JsonGenerator generator) {
        this.generator = generator;
    }

    @Override
    public boolean isGenerating() {
        return this.isGenerating;
    }

    @Override
    public void setIsGenerating(boolean isGenerating) {
           this.isGenerating = isGenerating;
    }

    @Override
    public boolean noGen() {
        return this.noGen;
    }
    
    @Override
    public void setNoGen(boolean noGen) {
        this.noGen = noGen;
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
        return this.skipSubtree;
    }

    @Override
    public State getFindPosState() {
        return this.findPosState;
    }

    @Override
    public State getMatchPosState() {
        return this.matchPosState;
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
    public String getTransfType() {
        return this.transfType;
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
