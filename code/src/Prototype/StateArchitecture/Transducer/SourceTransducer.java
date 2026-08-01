package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.PathAutomaton.PathAutomaton;
import Prototype.PathAutomaton.SimplePathAutomaton;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.*;
import Prototype.Writer.JsonWriter;
import Prototype.Writer.RawUtf8Writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

public class SourceTransducer extends Transducer {

    public SourceTransducer(SpecificationMapper mapper, BufferTransducer parentTransducer) {
      super(mapper, parentTransducer);


    }
    public boolean process() {
        return true;
    }

}
