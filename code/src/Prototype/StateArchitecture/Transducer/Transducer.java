package prototype.stateArchitecture.transducer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import prototype.mapper.SpecificationMapper;
import prototype.specificationParser.TransformationFormat;
import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.state.freeTraversal.Gen;

import java.io.IOException;

/* 
    Basic Transducer without Stack / Buffer
*/

public class Transducer {


    // transducer state
    protected State currentState;
    protected boolean paused;
    protected boolean generating;

    // I/O
    JsonGenerator generator;
    JsonParser parser;
    
    // transformation
    protected TransformationFormat specification;
    protected String transformationType;

    // resuable states (must be reused due to measuremennt nethod that counts all memory allocations)    
    protected Gen genState;
    
    public Transducer(SpecificationMapper mapper, JsonParser parser, JsonGenerator generator) {
        
        this.specification = mapper.getTransformationFormat();
        if (this.specification == null) {            
            throw new IllegalArgumentException("SpecificationMapper returned no transformation format");
        }

        this.parser = parser;
        this.generator = generator;

        this.transformationType = specification.getType();     
        
        paused = false;
        generating = true;                
        
        initStates();           
        currentState = this.genState;
    }

    
    
    // states must be initalized outside constructor, because they need initialized fields from subclasses' constructors
    protected void initStates() {        
        genState = new Gen(this);        
    }
    
    public boolean process() {
        boolean success = true;
        try {
            JsonToken event;

            while (!parser.isClosed()) {
                event = parser.nextToken();

                if (event == null) break;
                currentState.process(parser);
                if (this.getGenerating()) {
                    generator.copyCurrentEvent(parser);
                }
            }
        } catch (IOException e) {
            System.err.println("Issue while processing IdentityTransducer: " + e);
            success = false;
        } finally {
            // Always attempt to release both streams, whether processing
            // succeeded or failed, so a failure here doesn't leak file
            // handles on top of the original problem.
            closeQuietly(parser);
            closeQuietly(generator);
        }
        return success;
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

    
    public State getGenState() {
        return this.genState;
    }


    public void setGenerating(boolean generating) {
        this.generating = generating;
    }

    public boolean getGenerating() {
        return this.generating;
    }    


    public String getTransformationType() {
        return this.transformationType;
    }


    public State getCurrentState() {
        return this.currentState;
    }

    public void setState(State state) {
        this.currentState = state;
    }

    

    /**
     * Closes a Closeable, logging any failure instead of throwing, so that
     * cleanup of one resource can't mask an already-in-flight exception or
     * prevent cleanup of the other resource.
     */
    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            System.err.println("Warning: failed to close resource cleanly: " + e.getMessage());
        }
    }

    
}
