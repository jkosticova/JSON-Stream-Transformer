package prototype.stateArchitecture.state.match;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import prototype.specificationParser.RenameTransformation;
import prototype.specificationParser.ReplaceTransformation;
import prototype.specificationParser.TransformationFormat;
import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.transducer.BufferStackTransducer;
import prototype.stateArchitecture.transducer.BufferSyncTransducer;
import prototype.stateArchitecture.transducer.StackTransducer;
import prototype.stateArchitecture.transducer.Transducer;

import static prototype.utils.Helper.writeJsonValue;

import java.io.IOException;

/*
    This state captures behavior when a match of a path is found.
    It always 
    - transitions to another state
    - keeps transducer paused
    It applies on all path-evaluating transformations.    
    It uses generator and specificiation to be able to perform specific operations on matching the path.
    
*/

public class MatchPath implements State {
    private final StackTransducer transducer;
    private final BufferStackTransducer bTransducer;
    private final TransformationFormat specification;
    private final JsonGenerator generator;

    public MatchPath(Transducer transducer) {
        if (!(transducer instanceof StackTransducer stackTransducer)) {
            throw new IllegalArgumentException(
                    "MatchPath requires a StackTransducer");
        }

        this.transducer = stackTransducer;

        if (stackTransducer instanceof BufferStackTransducer bufferStackTransducer) {
            this.bTransducer = bufferStackTransducer;
        } else {
            this.bTransducer = null;
        }

        this.specification = transducer.getSpecification();
        this.generator = transducer.getGenerator();
    }

    @Override
    public void process(JsonParser parser) {
        JsonToken event = parser.currentToken();

        // set first match
        if (bTransducer != null && bTransducer.getFirstMatch() == BufferSyncTransducer.NONE) {
            if (bTransducer.getTransducerRole() == BufferStackTransducer.SRC_TRANSDUCER) {
                bTransducer.setFirstMatch(BufferSyncTransducer.SRC_FIRST);
            } else if (bTransducer.getTransducerRole() == BufferStackTransducer.DEST_TRANSDUCER) {
                bTransducer.setFirstMatch(BufferSyncTransducer.DEST_FIRST);
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
                transducer.setState(transducer.getSubtreeSkipState());
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
                transducer.setState(transducer.getSubtreeSkipState());
                break;
            // add and copy yield the same code
            case "add":
                transducer.setState(transducer.getFindPosState());
                // generate current fieldname in case of object member match
                generateCurrentFieldName(parser);
                break;
            case "copy":
                if (bTransducer == null) {
                    throw new IllegalArgumentException(
                            "Copy transformation requires a BufferStackTransducer");
                }
                // source transducer goes to MeminSubtree
                if (bTransducer.getTransducerRole() == BufferStackTransducer.SRC_TRANSDUCER) {
                    bTransducer.setState(bTransducer.getSubtreeMeminState());
                    if (bTransducer.getFirstMatch() == BufferSyncTransducer.SRC_FIRST) {
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
                if (bTransducer == null) {
                    throw new IllegalArgumentException(
                            "Move transformation requires a BufferStackTransducer");
                }
                // source transducer goes to MeminSubtree
                if (bTransducer.getTransducerRole() == BufferStackTransducer.SRC_TRANSDUCER) {
                    if (bTransducer.getFirstMatch() == BufferSyncTransducer.SRC_FIRST) {
                        bTransducer.setState(bTransducer.getSubtreeSkipMeminState());
                    } else if (bTransducer.getFirstMatch() == BufferSyncTransducer.DEST_FIRST) {
                        transducer.setState(transducer.getSubtreeGenState());
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