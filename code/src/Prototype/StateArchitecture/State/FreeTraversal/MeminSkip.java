package prototype.stateArchitecture.state.freeTraversal;

import com.fasterxml.jackson.core.JsonParser;


import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.transducer.Transducer;

/*
    This state copies the input events to the memory.
    It does not generate the output.        
*/
public class MeminSkip implements State {
    private final Transducer transducer;
    
    public MeminSkip(Transducer transducer) {
        this.transducer = transducer;    
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setPaused(false);        
        transducer.setGenerating(false);
        transducer.addToMemory();
    }
   
}
