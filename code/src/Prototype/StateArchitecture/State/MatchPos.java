package Prototype.StateArchitecture.State;

import Prototype.SpecificationParser.AddTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.Transducer;
import Prototype.Writer.JsonWriter;

import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;

public class MatchPos implements State {
    private final Transducer transducer;
    private final JsonWriter writer;
    private final TransformationFormat specification;

    public MatchPos(Transducer transducer) {
        this.transducer = transducer;
        this.writer = transducer.getWriter();
        this.specification = transducer.getSpecification();
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setNoGen(true);
        switch (specification.getType()) {
            case "add":
                try {
                    if (((AddTransformation) specification).getKey() != null) {
                        writer.writeFieldName(((AddTransformation) specification).getKey());
                        writer.writeRaw(((AddTransformation) specification).getValue());
                        /*if (this.transducer.isGenerating()) {
                           writer.writeCurrentEvent(parser);
                        } */                       
                    } else {
                        writer.writeRaw(((AddTransformation) specification).getValue());
                        /*
                        if (this.transducer.isGenerating()) {
                            writer.writeCurrentEvent(parser);
                        } 
                        */                           
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
    public boolean isGenerating() {
        return false;
    }
}
