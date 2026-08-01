package Prototype.StateArchitecture.State;

import Prototype.SpecificationParser.RenameTransformation;
import Prototype.SpecificationParser.ReplaceTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.Transducer;
import Prototype.Writer.JsonWriter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

public class Match implements State {
    private final Transducer transducer;
    private final JsonWriter writer;
    private final TransformationFormat specification;    

    public Match(Transducer transducer) {
        this.transducer = transducer;
        this.writer = transducer.getWriter();
        this.specification = transducer.getSpecification();            
    }

    @Override
    public void process(JsonParser parser) {        
        
        transducer.setNoGen(true);
        transducer.setPaused(false);
        JsonToken event = parser.currentToken();
        switch (specification.getType()) {
            // write fieldName from specification and only copy the rest of the input
            case "rename":
                try {
                    writer.writeFieldName(((RenameTransformation) specification).getKey());                    
                    // TODO: preskocit aktualny fieldName - urobit nejako lepsie
                    parser.nextToken();
                    transducer.setState(transducer.getGenState());
                    
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }                                
                break;
            // skip current subtree, including current event
            case "remove":
                // ak sme uz na prvku pola
                if (parser.currentToken() != JsonToken.FIELD_NAME) {
                        transducer.setPaused(true);
                }   
                transducer.setState(transducer.getSkipSubtreeState());                
                break;
            
            case "replace":
                try {                    
                    if (((ReplaceTransformation) specification).getKey() != null) {
                        writer.writeFieldName(((ReplaceTransformation) specification).getKey());                        
                    // current key is copied only in case of object field name, it doesn't make sense for other cases
                    } else if (transducer.getPaStack().peek()>=0 && event == JsonToken.FIELD_NAME) {
                        writer.writeCurrentEvent(parser);                        
                    }

                    writer.writeRaw(((ReplaceTransformation) specification).getValue());
                    
                    // ak sme uz na prvku pola
                    if (parser.currentToken() != JsonToken.FIELD_NAME) {
                        transducer.setPaused(true);
                    }
                    transducer.setState(transducer.getSkipSubtreeState());                    
                    // prvy krok v skip stave este stojime
                    
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }                
                break;
            case "add":
                try {                    
                    transducer.setState(transducer.getFindPosState());                                          
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                break;

        }
        
    }
    public boolean isGenerating() {
        return false;
    }
}
