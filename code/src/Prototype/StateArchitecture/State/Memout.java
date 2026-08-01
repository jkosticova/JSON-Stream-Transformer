package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

public class Memout implements State {
    Transducer transducer;

    public Memout(Transducer transducer) {
        this.transducer = transducer;
    }

    public void process(JsonParser parser) {
        try {
            transducer.getFromMemory();
            if (this.transducer.isGenerating()) {
                transducer.getWriter().writeCurrentEvent(parser);
            }

            transducer.setState(transducer.getGenState());
            transducer.setPaused(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isGenerating() {
        return false;
    }
}
