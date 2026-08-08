package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

/*
    This state copies the input event to the output.
    It does not manipulate the stack in case of stack transducer
*/
public class Gen implements State {
    private final Transducer transducer;    

    public Gen(Transducer transducer) {
        this.transducer = transducer;    
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setPaused(false);    
    }

    @Override
    public boolean isGenerating() {
        return true;
    }

}
