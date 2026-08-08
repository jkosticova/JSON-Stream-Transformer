package prototype.stateArchitecture.state;

import com.fasterxml.jackson.core.JsonParser;

import prototype.stateArchitecture.transducer.Transducer;

/* 
   This state generates the content of the buffer to the output AT ONCE.
   It uses "buffer.serialize(generator)".      
   Entering token = leaving token.
*/
public class Memout implements State {
    private final Transducer transducer;

    public Memout(Transducer transducer) {
        this.transducer = transducer;
        transducer.setGenerating(false);
    }

    @Override
    public void process(JsonParser parser) {        
        transducer.setGenerating(false);
        transducer.getFromMemory();            
        transducer.setState(transducer.getGenState());        
        transducer.setPaused(true);        
    }
 
}
