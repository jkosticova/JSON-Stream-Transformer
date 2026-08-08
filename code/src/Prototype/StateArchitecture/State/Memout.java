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
        transducer.setGenerating(false);
    }

    @Override
    public void process(JsonParser parser) {        
        transducer.getFromMemory();            
        transducer.setState(transducer.getGenState());        
        transducer.setPaused(true);        
    }

    @Override
    public boolean isGenerating() {
        return false;
    }
}
