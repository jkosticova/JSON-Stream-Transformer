package Prototype.StateArchitecture.State.Match;

import Prototype.SpecificationParser.RenameTransformation;
import Prototype.SpecificationParser.ReplaceTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import static Prototype.Utils.Helper.writeJsonValue;

/*
    This state captures behavior when a match of a path is found.
    It always 
    - transitions to another state
    - keeps transducer paused
    It applies on all path-evaluating transformations.    
    It uses generator and specificiation to be able to perform specific operations on matching the path.
    
*/

public class MatchPath implements State {
    private final Transducer transducer;
    private final TransformationFormat specification;
    private final JsonGenerator generator;

    public MatchPath(Transducer transducer) {
        this.transducer = transducer;
        this.specification = transducer.getSpecification();
        this.generator = transducer.getGenerator();
    }

    @Override
    public void process(JsonParser parser) {
        JsonToken event = parser.currentToken();        
        // set first match 
        if (transducer.getFirstMatch() == BufferTransducer.NONE ) {
            if (transducer.getTransducerRole() == Transducer.SRC_TRANSDUCER) {
                transducer.setFirstMatch(BufferTransducer.SRC_FIRST);
            }
            else if (transducer.getTransducerRole() == Transducer.DEST_TRANSDUCER) {
                transducer.setFirstMatch(BufferTransducer.DEST_FIRST);
            }
        }
        // parser is positioned at a match - either a fieldname or a value
        switch (transducer.getTransformationType()) {
            case "rename":
                try {
                    generator.writeFieldName(((RenameTransformation) specification).getKey());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // do not generate current fieldname in case of object member match
                // (default)
                transducer.setState(transducer.getGenState());
                transducer.setPaused(false);
                break;
            case "remove":
                transducer.setState(transducer.getSkipSubtreeState());
                // do not generate current fieldname in case of object member match
                // (default)
                break;
            case "replace":
                try {
                    // generate key for object member match
                    if (event.equals(JsonToken.FIELD_NAME)) {
                        if (((ReplaceTransformation) specification).getKey() != null) {
                            generator.writeFieldName(((ReplaceTransformation) specification).getKey());
                            // do not generate current fieldname
                            // (default)
                        } else {
                            generator.copyCurrentEvent(parser);
                        }
                    }
                    // generate value both for objet member match and array element match
                    writeJsonValue(generator, ((ReplaceTransformation) specification).getValue());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                transducer.setState(transducer.getSkipSubtreeState());
                break;
            // add and copy yield the same code
            case "add":                       
                transducer.setState(transducer.getFindPosState());
                // generate current fieldname in case of object member match
                generateCurrentFieldName(parser);                
                break;
            case "copy":
                // source transducer goes to MeminSubtree
                if (transducer.getTransducerRole() == Transducer.SRC_TRANSDUCER) {
                    transducer.setState(transducer.getMeminSubtreeState());
                    if (transducer.getFirstMatch() == BufferTransducer.SRC_FIRST) {
                        generateCurrentFieldName(parser);                                         
                    }
                }
                // simple and dest transducer go to FindPos
                else {
                    transducer.setState(transducer.getFindPosState());                    
                    generateCurrentFieldName(parser);                                     
                }   
                
                break;
            case "move":    
                // source transducer goes to MeminSubtree
                if (transducer.getTransducerRole() == Transducer.SRC_TRANSDUCER) {
                    if (transducer.getFirstMatch() == BufferTransducer.SRC_FIRST) {
                        transducer.setState(transducer.getMeminSkipSubtreeState());
                    }
                    else if (transducer.getFirstMatch() == BufferTransducer.DEST_FIRST) {
                        transducer.setState(transducer.getGenSubtreeState());
                    }
                    // don't generate fieldname
                    // we wither remove key value pair (object member) or a value (array element)
                }
                // simple and dest transducer go to FindPos
                else {
                    transducer.setState(transducer.getFindPosState());
                    generateCurrentFieldName(parser);                
                }    
                
                
                break;            
            default:
                break;        
        }
        transducer.processValueAfterMatch();                
    }
    
    private void generateCurrentFieldName(JsonParser parser) {
        if (parser.currentToken().equals(JsonToken.FIELD_NAME)) {
            try {
                generator.copyCurrentEvent(parser);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}