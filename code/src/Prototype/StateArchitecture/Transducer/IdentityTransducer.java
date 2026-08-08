package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.Gen;
import Prototype.StateArchitecture.State.State;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IdentityTransducer extends Transducer {    
    JsonGenerator generator;
    JsonParser parser;    

    public IdentityTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        super(mapper, SIMPLE_TRANSDUCER);
        
        JsonFactory factory = new JsonFactory();
        try {
            parser = factory.createParser(inputStream);
            generator = factory.createGenerator(outputStream).useDefaultPrettyPrinter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        initStates();        
        currentState = new Gen(this);
    }

    @Override
    public boolean process() {
        try {
            JsonToken event;

            while (!parser.isClosed()) {
                event = parser.nextToken();

                if (event == null) break;
                currentState.process(parser);
                if (this.getCurrentState().isGenerating()) {
                    generator.copyCurrentEvent(parser);
                }
            }

            parser.close();
            generator.close();
        } catch (Exception e) {
            System.out.println("Issue while processing IdentityTransducer: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void moveToValue() {
        return;
    }

    public void processValueAfterMatch() {
        return;
    }


}
