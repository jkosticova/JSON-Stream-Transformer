package Prototype.StateArchitecture.State;

import Prototype.SpecificationParser.AddTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

import static Prototype.Utils.Helper.writeJsonValue;

public class MatchPos implements State {
    private final Transducer transducer;
    private final JsonGenerator generator;
    private final TransformationFormat specification;

    public MatchPos(Transducer transducer) {
        this.transducer = transducer;
        this.generator = transducer.getGenerator();
        this.specification = transducer.getSpecification();
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setNoGen(true);
        switch (specification.getType()) {
            case "add":
                // current token is either a fieldname or a value
                try {
                    if (((AddTransformation) specification).getKey() != null) {
                        generator.writeFieldName(((AddTransformation) specification).getKey());
                        writeJsonValue(generator, ((AddTransformation) specification).getValue());                                             
                    } else {
                        writeJsonValue(generator, ((AddTransformation) specification).getValue());                                                
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

    @Override
    public boolean isGenerating() {
        return false;
    }
}
