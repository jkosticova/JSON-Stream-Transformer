package Prototype.StateArchitecture.State;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.PathAutomaton.SimplePathAutomaton;
import Prototype.SpecificationParser.AddTransformation;
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
    private final JsonGenerator generator;
    private final TransformationFormat specification;

    public Match(Transducer transducer) {
        this.transducer = transducer;
        this.generator = transducer.getGenerator();
        this.specification = transducer.getSpecification();    
    }

    @Override
    public void process(JsonToken event, JsonParser parser) {
        transducer.setNoGen(true);
        switch (specification.getType()) {
            case "rename":
                try {
                    generator.writeFieldName(((RenameTransformation) specification).getKey());
                    transducer.setState(transducer.getGenState());
                    transducer.setPaused(false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "remove":
                transducer.setState(transducer.getDelState());
                transducer.setPaused(false);
                break;
            case "replace":
                try {
                    if (((ReplaceTransformation) specification).getKey() != null) {
                        generator.writeFieldName(((ReplaceTransformation) specification).getKey());
                    // current key is copied only in case of object field name, it doesn't make sense for other cases
                    } else if (transducer.getPaStack().peek()>=0 && event == JsonToken.FIELD_NAME) {
                        generator.copyCurrentEvent(parser);
                    }

                    writeJsonValue(generator, ((ReplaceTransformation) specification).getValue());
                    transducer.setState(transducer.getDelState());
                    transducer.setPaused(false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "add":
                try {
                    if (((AddTransformation) specification).getKey() != null) {
                        transducer.setState(transducer.getFind_iState());
                    } else {
                        transducer.setState(transducer.getFind_iState());
                    }
                    if (this.transducer.isGenerating()) {
                        generator.copyCurrentEvent(parser);
                    }

                    transducer.setPaused(false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                break;
        }
    }
}
