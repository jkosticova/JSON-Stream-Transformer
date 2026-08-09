package prototype.stateArchitecture.state.freeTraversal;

import com.fasterxml.jackson.core.JsonParser;


import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.transducer.BufferStackTransducer;
import prototype.stateArchitecture.transducer.StackTransducer;
import prototype.stateArchitecture.transducer.Transducer;

/*
    This state copies the input events to the memory.
    It does not generate the output.        
*/
public class MeminSkip implements State {
    private final BufferStackTransducer transducer;
    
    public MeminSkip(Transducer transducer) {
    if (!(transducer instanceof BufferStackTransducer)) {
        throw new IllegalArgumentException(
            "MeminSkip requires a BufferStackTransducer"
        );
    }

    this.transducer = (BufferStackTransducer) transducer;
}
    @Override
    public void process(JsonParser parser) {
        transducer.setPaused(false);        
        transducer.setGenerating(false);
        transducer.addToMemory();
    }
   
}
