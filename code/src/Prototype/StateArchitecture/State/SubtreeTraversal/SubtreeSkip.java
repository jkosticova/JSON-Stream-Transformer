package Prototype.StateArchitecture.State.SubtreeTraversal;

import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.Transducer.Transducer;

import java.io.IOException;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/*
  This state prunes current subtree, i.e., doesn't copy it to the output.
  Entering token: the start of given value (START_ARRAY, START_OBJECT, literal value)
  Leaving token: one token after the end of given value
*/
public class SubtreeSkip implements State {
    private final Transducer transducer;
    private SubtreeProcess subtreeProcessState;

    public SubtreeSkip(Transducer transducer) {
        this.transducer = transducer;
        this.subtreeProcessState = new SubtreeProcess();
    }

    @Override
    public void process(JsonParser parser) {        
        transducer.setPaused(false);        
        transducer.setGenerating(false);                
        
        subtreeProcessState.process(parser);        
       
        // current token is last token of given subtree
        if (subtreeProcessState.isSubtreeEnd()) {
            try {                
                transducer.setState(transducer.getGenState());
                transducer.setPaused(false);                                
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
    }       


}