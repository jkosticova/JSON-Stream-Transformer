package Prototype.StateArchitecture.State;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import Prototype.StateArchitecture.Transducer.Transducer;

public class DoNothing implements State {
    
    public DoNothing(Transducer transducer) {
        return;
    }
    @Override
    public void process(JsonToken event, JsonParser parser) {
        return;
    }    
}
