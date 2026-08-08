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

public class StackTransducer extends Transducer {    
        
    
    /* constructor for a single STACK transformation */
    public StackTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {        
        super(mapper, true);

        parentTransducer = null;        
        JsonFactory factory = new JsonFactory();
        try {
            parser = factory.createParser(inputStream);
            generator = factory.createGenerator(outputStream).useDefaultPrettyPrinter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }            
        // stacks
        this.paStack = new Stack<>();        
        this.indexStack = new Stack<>();        
                            
        pa = new SimplePathAutomaton(path); 
        paStack.push(INITIAL_PA_STATE);
        
        initStates();        
        
        currentState = evalState;                
    }

    /* constructor for a single COPY or MOVE transformation */
    public StackTransducer(SpecificationMapper mapper, BufferTransducer parentTransducer, boolean source) {        
        super(mapper, source);

        this.parentTransducer = parentTransducer;
        this.parser = parentTransducer.parser;
        this.generator = parentTransducer.generator;
        
        // stacks
        paStack = new Stack<>();        
        indexStack = new Stack<>();        
                                                
        pa = new SimplePathAutomaton(path);         
        paStack.push(INITIAL_PA_STATE);            

        initStates();        

        currentState = evalState;                
    }

    // used only for stack transformations
    @Override
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
            e.printStackTrace();
            return false;
        }
        return true;
    }

    

    

    
    
}
