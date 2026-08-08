package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.Transducer;

import java.io.IOException;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/*
This state generates the current subtree to the output
*/
public class GenSubtree implements State {
    private final Transducer transducer;
    private ProcessSubtree processSubtreeState;

    public GenSubtree(Transducer transducer) {        
        this.transducer = transducer;
        this.processSubtreeState = new ProcessSubtree(transducer);
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setPaused(false);        
        transducer.setGenerating(true);                
        this.processSubtreeState.process(parser);
    }

    

}