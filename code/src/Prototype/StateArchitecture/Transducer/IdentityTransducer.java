package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.PathAutomaton.PathAutomaton;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.Gen;
import Prototype.StateArchitecture.State.State;
import Prototype.Writer.JsonWriter;
import Prototype.Writer.RawUtf8Writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

public class IdentityTransducer extends Transducer {

    public IdentityTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        super(mapper, inputStream, outputStream);
        currentState = new Gen(this);
        
    }
    
    public boolean process() {
        try {
            JsonToken event;

            while (!parser.isClosed()) {
                event = parser.nextToken();

                if (event == null) break;
                currentState.process(parser);
                State current = currentState;
                currentState.process(parser);
                if (current.isGenerating()) {
                    writer.writeCurrentEvent(parser);
                }            
            }

            parser.close();
            writer.flush();
            //generator.close();
        } catch (Exception e) {
            System.out.println("Issue while processing IdentityTransducer: " + e.getMessage());
            return false;
        }
        return true;
    }


}
