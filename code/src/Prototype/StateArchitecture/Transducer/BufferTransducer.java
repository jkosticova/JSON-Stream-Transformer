package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.Utils.Helper;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.State.Sync;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BufferTransducer {
    private final State currentState;
    private final StackTransducer sourceTransducer;
    private final StackTransducer destinationTransducer;
    private byte firstMatch = NONE;

    public static final byte NONE = 0;
    public static final byte SRC_FIRST = 1;
    public static final byte DEST_FIRST = 2;

    private boolean paused;
    JsonGenerator generator;
    JsonParser parser;
    TokenBuffer buffer;
    TransformationFormat specification;
    public boolean generateFromSource;
    
    public BufferTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {        
            specification = mapper.getTransformationFormat();
            paused = false;

            JsonFactory factory = new JsonFactory();
            try {
                parser = factory.createParser(inputStream);
                generator = factory.createGenerator(outputStream).useDefaultPrettyPrinter();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            buffer = new TokenBuffer((ObjectCodec) null, false);            
            sourceTransducer = new StackTransducer(mapper, this, Transducer.SRC_TRANSDUCER);            
            destinationTransducer = new StackTransducer(mapper, this, Transducer.DEST_TRANSDUCER);
            
            
            currentState = new Sync(this);        
    }

    public void getFromMemory() {
        try {
            buffer.serialize(generator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addToMemory() {
        try {
            buffer.copyCurrentEvent(parser);
            //recordBufferMemory();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setFirstMatch(byte firstMatch) {
        this.firstMatch = firstMatch;
    }

    public byte getFirstMatch() {
        return this.firstMatch;
    }

    public JsonGenerator getGenerator() {
        return this.generator;
    }

    public TransformationFormat getSpecification() {
        return this.specification;
    }

    public StackTransducer getSourceTransducer() {
        return this.sourceTransducer;
    }

    public StackTransducer getDestinationTransducer() {
        return this.destinationTransducer;
    }

    public boolean process() {
        try {
            JsonToken event = null;

            while (!parser.isClosed()) {
                setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused()); 
                if (!paused) {
                    event = parser.nextToken();
                }
                if (event == null) break;
                currentState.process(parser);
                if (sourceTransducer.getGenerating() &&
                    destinationTransducer.getGenerating()) {
                    generator.copyCurrentEvent(parser);                    
                }                
                generator.flush();
            }
            generator.flush();
            parser.close();
            generator.close();
        } catch (Exception e) {
            System.out.println("Issue while processing BufferTransducer: " + e.getMessage());
            e.printStackTrace();

            return false;
        }
        return true;
    }

     public void moveToValue(byte transducerRole) {                
       
        try {
            parser.nextToken();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         // process value by the other transducer if not paused
        if (transducerRole == Transducer.SRC_TRANSDUCER && !destinationTransducer.getPaused()) {
            destinationTransducer.getCurrentState().process(parser);            
        }
        else if (transducerRole == Transducer.DEST_TRANSDUCER && !sourceTransducer.getPaused()) {
            sourceTransducer.getCurrentState().process(parser);
        }
     }   

}
