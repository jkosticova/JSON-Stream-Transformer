package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

public class Gen implements State {
    private final Transducer transducer;
    private JsonGenerator generator;

    public Gen(Transducer transducer) {
        this.transducer = transducer;
        init();
    }

    private void init() {
        this.generator = transducer.getGenerator();
    }

    @Override
    public void process(JsonToken event, JsonParser parser) {
        init();

        try {
            generator.copyCurrentEvent(parser);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
