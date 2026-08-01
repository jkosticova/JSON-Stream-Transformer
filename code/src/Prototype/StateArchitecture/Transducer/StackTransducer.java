package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;

import com.fasterxml.jackson.core.JsonToken;

import java.io.InputStream;
import java.io.OutputStream;

public class StackTransducer extends Transducer {
    
    public StackTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        super(mapper, inputStream, outputStream);
    }
    
    @Override
    public boolean process() {
        try {
            JsonToken event = null;

            // inicializacia stackov - aby boli prazdne
            paStack.clear();
            indexStack.clear();
            
            paStack.push(INITIAL_PA_STATE);

            // kym sa cita nieco zo vstupu
            while (!parser.isClosed()) {
                boolean skipEvent = false;
                if (!this.paused) {
                    event = parser.nextToken();
                }
                /*if (!skipEvent) {
                    event = parser.nextToken();
                }*/
                // EOF && prazdny stack
                if (event == null || paStack.isEmpty()) break;                
                currentState.process(parser);                
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

    
}
