package Prototype.StateArchitecture.State;

import Prototype.SpecificationParser.AddTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.OldTransducer;
import Prototype.Writer.JsonWriter;

import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;

public class MatchPos implements State {
    private final OldTransducer transducer;
    private final JsonWriter writer;
    private final TransformationFormat specification;

    public MatchPos(OldTransducer transducer) {
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
                    } else {
                        writer.writeRaw(((AddTransformation) specification).getValue());       
                    }
                    transducer.setState(transducer.getTraverseState());
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
