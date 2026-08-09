package prototype.stateArchitecture.state.freeTraversal;

import com.fasterxml.jackson.core.JsonParser;

import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.transducer.Transducer;




/*
    This state copies the input event to the output.
    It does not manipulate the stack in case of stack transducer
*/
public class Gen implements State {
    private final Transducer transducer;    

    public Gen(Transducer transducer) {
        this.transducer = transducer;    
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setPaused(false);    
        transducer.setGenerating(true);
    }
    

}
