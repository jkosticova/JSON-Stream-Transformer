package Prototype.StateArchitecture.State;

import Prototype.SpecificationParser.RenameTransformation;
import Prototype.SpecificationParser.ReplaceTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import static Prototype.Utils.Helper.writeJsonValue;

public class Match implements State {
    private final Transducer transducer;
    private final TransformationFormat specification;
    private final JsonGenerator generator;

    public Match(Transducer transducer) {
        this.transducer = transducer;
        this.specification = transducer.getSpecification();
        this.generator = transducer.getGenerator();
    }

    @Override
    public void process(JsonParser parser) {
        JsonToken event = parser.currentToken();        
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
                // generate current fieldname in case of object member match
                generateCurrentFieldName(parser);
            case "copy":
                transducer.setPaused(false);
                transducer.setState(transducer.getFindPosState());
                break;
            default:
                break;
        }
        // in case of a fieldname match, we move to the corresponding value
        if (event.equals(JsonToken.FIELD_NAME)) {
            transducer.moveToValue();
        }                
        transducer.setPaused(true);
        transducer.setGenerating(false);
        
        // we process the current value again (not fieldname!!!)
        // Rename -> Gen (fieldname NOT generated)
        // Remove -> SkipSubtree (fieldname NOT generated)
        // Replace -> SkipSubtree (fieldname NOT generated)
        // Add -> FindPos (fieldname generated in CASE)
        // Copy -> FindPos (generated ???)
        
    }

    @Override
    public boolean isGenerating() {
        return false;
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