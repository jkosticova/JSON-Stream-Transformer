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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openjdk.jol.info.GraphLayout;

public class BufferTransducer {
    private final State currentState;
    private final StackTransducer sourceTransducer;
    private final StackTransducer destinationTransducer;

    private boolean paused;
    JsonGenerator generator;
    JsonParser parser;
    TokenBuffer buffer;
    TransformationFormat specification;
    
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
            //ObjectMapper objectMapper = new ObjectMapper();
            buffer = new TokenBuffer((ObjectCodec) null, false);            
            sourceTransducer = new StackTransducer(mapper, this, true);
            destinationTransducer = new StackTransducer(mapper, this, false);
            
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
                if (!paused) {
                    event = parser.nextToken();
                }
                if (event == null) break;
                currentState.process(parser);
                generator.flush();
            }
            generator.flush();
            parser.close();
            generator.close();
        } catch (Exception e) {
            System.out.println("Issue while processing BufferTransducer: " + e.getMessage());
            return false;
        }
        return true;
    }

}
