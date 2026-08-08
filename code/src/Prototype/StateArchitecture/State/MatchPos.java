package Prototype.StateArchitecture.State;

import Prototype.SpecificationParser.AddTransformation;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

import static Prototype.Utils.Helper.writeJsonValue;

public class MatchPos implements State {
    private final Transducer transducer;    

    public MatchPos(Transducer transducer) {
        this.transducer = transducer;
        
    }

    @Override
    public void process(JsonParser parser) {  
        TransformationFormat specification = transducer.getSpecification();
        JsonGenerator generator = transducer.getGenerator();
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
                transducer.setPaused(true);
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
                transducer.setState(transducer.getMemoutState());
                transducer.setPaused(true);
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
