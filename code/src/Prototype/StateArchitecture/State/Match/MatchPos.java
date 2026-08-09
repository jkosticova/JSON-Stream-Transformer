package prototype.stateArchitecture.state.match;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import prototype.specificationParser.AddTransformation;
import prototype.specificationParser.CopyTransformation;
import prototype.specificationParser.MoveTransformation;
import prototype.specificationParser.TransformationFormat;
import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.transducer.BufferStackTransducer;
import prototype.stateArchitecture.transducer.BufferSyncTransducer;
import prototype.stateArchitecture.transducer.StackTransducer;
import prototype.stateArchitecture.transducer.Transducer;

import static prototype.utils.Helper.writeJsonValue;

import java.io.IOException;

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
    private final BufferStackTransducer bTransducer;    
    private final TransformationFormat specification;
    private final JsonGenerator generator;

    public MatchPos(Transducer transducer) {
        
        this.transducer = transducer;

        if (transducer instanceof BufferStackTransducer bufferStackTransducer) {
            this.bTransducer = bufferStackTransducer;
        } else {
            this.bTransducer = null;
        }

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
                if (bTransducer == null) {
                    throw new IllegalArgumentException(
                    "Copy transformation requires a BufferStackTransducer"
                    );
                }
                try {
                    if (((CopyTransformation) specification).getKey() != null) {
                            generator.writeFieldName(((CopyTransformation) specification).getKey());
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }                
                if (bTransducer.getFirstMatch() == BufferSyncTransducer.SRC_FIRST) {
                    bTransducer.setState(bTransducer.getMemoutState());
                }
                else if (bTransducer.getFirstMatch() == BufferSyncTransducer.DEST_FIRST) {
                    bTransducer.setState(bTransducer.getMeminSkipState());
                }
                else {
                    throw new IllegalArgumentException(
                    "Copy: In MatchPos state, the first match must be recorded for BufferStackTransducer"
                    );
                }              
                break;
            case "move":  
                if (bTransducer == null) {
                    throw new IllegalArgumentException(
                    "Move transformation requires a BufferStackTransducer"
                    );
                }
                try {
                    if (((MoveTransformation) specification).getKey() != null) {
                            generator.writeFieldName(((MoveTransformation) specification).getKey());
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }                
                if (bTransducer.getFirstMatch() == BufferSyncTransducer.SRC_FIRST) {
                    bTransducer.setState(bTransducer.getMemoutState());
                }
                else if (bTransducer.getFirstMatch() == BufferSyncTransducer.DEST_FIRST) {
                    bTransducer.setState(bTransducer.getMeminSkipState());
                }
                else {
                    throw new IllegalArgumentException(
                    "Move: In MatchPos state, the first match must be recorded for BufferStackTransducer"
                    );
                }              
                break;              
            default:
                break;
        }
        transducer.setPaused(true);
    }
 
}
