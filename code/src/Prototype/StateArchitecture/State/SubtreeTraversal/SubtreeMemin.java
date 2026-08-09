package prototype.stateArchitecture.state.subtreeTraversal;

import com.fasterxml.jackson.core.JsonParser;

import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.transducer.BufferStackTransducer;
import prototype.stateArchitecture.transducer.BufferSyncTransducer;
import prototype.stateArchitecture.transducer.Transducer;

/* 
    This state traverses the value (subtree), puts it into the memory 
    and at the same time generates it to the output.
    Entering token: the start of given value (START_ARRAY, START_OBJECT, literal value)
    Leaving token: one token after the end of given value
*/
public class SubtreeMemin implements State {
    private final BufferStackTransducer transducer;    
    
    private SubtreeProcess subtreeProcessState;
    
    public SubtreeMemin(Transducer transducer) {
        
        if (transducer instanceof BufferStackTransducer bufferStackTransducer) {
            this.transducer = bufferStackTransducer;
        } else {
            throw new IllegalArgumentException(
                    "SubtreeMemin state requires a BufferStackTransducer");
        }
        this.subtreeProcessState = new SubtreeProcess();
    
    }

    

    @Override
    public void process(JsonParser parser) {        
        transducer.setPaused(false);                       
        transducer.setGenerating(true);

        subtreeProcessState.process(parser);
        
        transducer.addToMemory();

        // current token is last token of given subtree
        if (subtreeProcessState.isSubtreeEnd()) {
            try {
                // also last token of given subtree must be added to the memory
                //transducer.addToMemory();                
                if (transducer.getFirstMatch() == BufferSyncTransducer.SRC_FIRST) {
                    transducer.setState(transducer.getGenState());
                }
                else if (transducer.getFirstMatch() == BufferSyncTransducer.DEST_FIRST) {
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
