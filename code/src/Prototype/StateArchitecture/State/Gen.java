package Prototype.StateArchitecture.State;
import Prototype.StateArchitecture.Transducer.Transducer;

import com.fasterxml.jackson.core.JsonParser;


public class Gen implements State {
    private final Transducer transducer;

    public Gen(Transducer transducer) {
        this.transducer = transducer;
    }

    @Override
    public void process(JsonParser parser) {

        /* do nothing */
    }

    public boolean isGenerating() {
        return true;
    }
}
