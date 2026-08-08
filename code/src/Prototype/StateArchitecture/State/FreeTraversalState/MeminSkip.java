package Prototype.StateArchitecture.State.FreeTraversalState;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.PathAutomaton.SimplePathAutomaton;
import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.util.Stack;

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
