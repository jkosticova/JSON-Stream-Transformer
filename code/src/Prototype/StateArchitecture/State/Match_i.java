package Prototype.StateArchitecture.State;

import Prototype.SpecificationParser.AddTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import static Prototype.Utils.Helper.writeJsonValue;

public class Match_i implements State {
    private final Transducer transducer;
    private final JsonGenerator generator;
    private final TransformationFormat specification;

    public Match_i(Transducer transducer) {
        this.transducer = transducer;
        this.generator = transducer.getGenerator();
        this.specification = transducer.getSpecification();
    }

    @Override
    public void process(JsonToken event, JsonParser parser) {
        transducer.setNoGen(true);
        switch (specification.getType()) {
            case "add":
                try {
                    if (((AddTransformation) specification).getKey() != null) {
                        generator.writeFieldName(((AddTransformation) specification).getKey());
                        writeJsonValue(generator, ((AddTransformation) specification).getValue());
                        if (this.transducer.isGenerating()) {
                           generator.copyCurrentEvent(parser);
                        }                        
                    } else {
                        writeJsonValue(generator, ((AddTransformation) specification).getValue());
                        if (this.transducer.isGenerating()) {
                            generator.copyCurrentEvent(parser);
                        }                        
                    }
                    transducer.setState(transducer.getGenState());
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
