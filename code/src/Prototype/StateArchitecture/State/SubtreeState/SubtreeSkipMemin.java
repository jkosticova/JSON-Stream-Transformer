package Prototype.StateArchitecture.State.SubtreeState;

import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonParser;

/* 
    This state traverses the value (subtree) and puts it into the memory
    Entering token: the start of given value (START_ARRAY, START_OBJECT, literal value)
    Leaving token: one token after the end of given value
*/
public class SubtreeSkipMemin implements State {
    private final Transducer transducer;    
    private SubtreeProcess processSubtreeState;

    public SubtreeSkipMemin(Transducer transducer) {
        this.transducer = transducer;
        this.processSubtreeState = new SubtreeProcess();        
    }

    @Override
    public void process(JsonParser parser) {        
        transducer.setPaused(false);                       
        transducer.setGenerating(false);

        processSubtreeState.process(parser);
        
        transducer.addToMemory();                

        // current token is last token of given subtree
        if (processSubtreeState.isSubtreeEnd()) {
            try {                                
                if (transducer.getFirstMatch() == BufferTransducer.SRC_FIRST) {
                    transducer.setState(transducer.getGenState());
                }
                else if (transducer.getFirstMatch() == BufferTransducer.DEST_FIRST) {
                    transducer.setState(transducer.getMemoutState());
                }
                else {
                    //TODO handle error
                }                                
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
    }        
    
}
