package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.PathAutomaton.*;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.Eval;
import Prototype.StateArchitecture.State.State;
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
    JsonGenerator generator;
    JsonParser parser;
    Stack<Integer> paStack;    
    Stack<Integer> indexStack;    
    PathAutomaton pa;
    TransformationFormat specification;

    public StackTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        specification = mapper.getTransformationFormat();
        paStack = new Stack<>();        
        indexStack = new Stack<>();        
        pa = new SimplePathAutomaton(specification.getPath(),null); 
        
        JsonFactory factory = new JsonFactory();
        try {
            parser = factory.createParser(inputStream);
            generator = factory.createGenerator(outputStream).useDefaultPrettyPrinter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        currentState = new Eval(this);
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
    public Stack<Integer> getPaStack() {
        return this.paStack;
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
