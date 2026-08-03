package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.SpecificationParser.RenameTransformation;
import Prototype.StateArchitecture.JsonPushdownAutomaton.JsonPushdownAutomaton;
import Prototype.StateArchitecture.JsonPushdownAutomaton.ProcessingResult;
import Prototype.StateArchitecture.JsonPushdownAutomaton.StackConfiguration;
import Prototype.Writer.JsonWriter;
import Prototype.Writer.RawUtf8Writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class RenameTransducer extends Transducer {

    private RenameTransformation transf;     
    
    public RenameTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        
        this.transf = ((RenameTransformation) mapper.getTransformationFormat());        
        this.jsonPda = new JsonPushdownAutomaton(transf.getPath(), transf.getType());
        
        this.syncStates = new ArrayList<>();        
        syncStates.add(jsonPda.getMatchState());
    }
        
    @Override
    void synchronize() {
        if (jsonPda.getState().equals(jsonPda.getMatchState())) {            
                try {    
                    jWriter.writeFieldName(transf.getKey());                    
                    // TODO: preskocit aktualny fieldName - urobit nejako lepsie
                    jParser.nextToken();
                    jsonPda.setState(jsonPda.getTraverseState());                    
            }
            catch (IOException e) {
                e.printStackTrace();
            }            
        }
        else {
            // TODO
        }    
                        

    }    
}

