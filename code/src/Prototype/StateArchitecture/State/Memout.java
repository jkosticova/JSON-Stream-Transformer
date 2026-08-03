package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.OldTransducer;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

public class Memout implements State {
    OldTransducer transducer;

    public Memout(OldTransducer transducer) {
        this.transducer = transducer;
    }

    public void process(JsonParser parser) {
        try {
            transducer.getFromMemory();
            if (this.transducer.isGenerating()) {
                transducer.getWriter().writeCurrentEvent(parser);
            }

            transducer.setState(transducer.getTraverseState());
            transducer.setPaused(false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isGenerating() {
        return false;
    }
}
