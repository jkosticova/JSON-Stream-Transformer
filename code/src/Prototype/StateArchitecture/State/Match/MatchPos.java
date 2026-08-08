package Prototype.StateArchitecture.State.Match;

import Prototype.SpecificationParser.AddTransformation;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

import static Prototype.Utils.Helper.writeJsonValue;

/*
    This state captures behavior when an exact position for inserting a value is matched.
    It always 
    - transitions to another state
    - keeps transducer paused
    It applies on add, copy and move transformations.
    It uses generator and specificiation to be able to perform specific operations on matching the path.
*/
public class MatchPos implements State {
    private final Transducer transducer;    
    private final TransformationFormat specification;
    private final JsonGenerator generator;

    public MatchPos(Transducer transducer) {
        this.transducer = transducer;
        this.specification = transducer.getSpecification();
        this.generator = transducer.getGenerator();          
    }

    @Override
    public void process(JsonParser parser) {          
        switch (specification.getType()) {
            case "add":
                // current token is either a fieldname or a value
                try {
                    if (((AddTransformation) specification).getKey() != null) {
                        generator.writeFieldName(((AddTransformation) specification).getKey());
                    }
                    writeJsonValue(generator, ((AddTransformation) specification).getValue());                                                                                        
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                transducer.setState(transducer.getGenState());                                    
                break;
            case "copy":
                try {
                    if (((CopyTransformation) specification).getKey() != null) {
                            generator.writeFieldName(((CopyTransformation) specification).getKey());
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }                
                if (transducer.getFirstMatch() == BufferTransducer.SRC_FIRST) {
                    transducer.setState(transducer.getMemoutState());
                }
                else if (transducer.getFirstMatch() == BufferTransducer.DEST_FIRST) {
                    transducer.setState(transducer.getMeminSkipState());
                }
                else {
                    // TODO report error
                }
                break;
            case "move":  
                  try {
                    if (((MoveTransformation) specification).getKey() != null) {
                            generator.writeFieldName(((MoveTransformation) specification).getKey());
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }                
                 if (transducer.getFirstMatch() == BufferTransducer.SRC_FIRST) {
                    transducer.setState(transducer.getMemoutState());
                }
                else if (transducer.getFirstMatch() == BufferTransducer.DEST_FIRST) {
                    transducer.setState(transducer.getMeminSkipState());
                }
                else {
                    // TODO report error
                }              
                break;              
            default:
                break;
        }
        transducer.setPaused(true);
    }
 
}
