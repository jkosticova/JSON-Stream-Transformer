package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonParser;

/* This state generates the content of the buffer to the output AT ONCE.
   It uses "buffer.serialize(generator)".      
*/
public class Memout implements State {
    private final Transducer transducer;

    public Memout(Transducer transducer) {
        this.transducer = transducer;
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setPaused(false);        
        transducer.getFromMemory();            
        transducer.setState(transducer.getGenState());        
    }

    @Override
    public boolean isGenerating() {
        return false;
    }
}
