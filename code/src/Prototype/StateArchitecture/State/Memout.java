package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

public class Memout implements State {
    Transducer transducer;

    public Memout(Transducer transducer) {
        this.transducer = transducer;
    }

    public void process(JsonToken event, JsonParser parser) {
        try {
            transducer.getFromMemory();
            transducer.getGenerator().copyCurrentEvent(parser);

            transducer.setState(new Gen(transducer));
            transducer.setPaused(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
