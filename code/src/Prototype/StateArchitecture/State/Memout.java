package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonParser;

public class Memout implements State {
    Transducer transducer;

    public Memout(Transducer transducer) {
        this.transducer = transducer;
    }

    public void process(JsonParser parser) {
        transducer.setPaused(false);        
            transducer.getFromMemory();            
            transducer.setState(transducer.getGenState());
            transducer.setPaused(false);        
    }

    @Override
    public boolean isGenerating() {
        return false;
    }
}
