package prototype.stateArchitecture.state.subtreeTraversal;

import com.fasterxml.jackson.core.JsonParser;

import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.transducer.BufferStackTransducer;
import prototype.stateArchitecture.transducer.StackTransducer;
import prototype.stateArchitecture.transducer.Transducer;

/*
  This state generates the current subtree to the output
  Entering token: the start of given value (START_ARRAY, START_OBJECT, literal value)
  Leaving token: one token after the end of given value
*/
public class SubtreeGen implements State {    
    private final StackTransducer transducer;
    private final BufferStackTransducer bTransducer;
    private SubtreeProcess subtreeProcessState;

    public SubtreeGen(StackTransducer transducer) {
        
        this.transducer = transducer;
        if (transducer instanceof BufferStackTransducer bufferStackTransducer) {
            bTransducer = bufferStackTransducer;
        } else {
            bTransducer = null;            
        }

        this.subtreeProcessState = new SubtreeProcess();
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setPaused(false);
        transducer.setGenerating(true);
        this.subtreeProcessState.process(parser);
        if (subtreeProcessState.isSubtreeEnd()) {
            if (bTransducer == null) {
                throw new IllegalArgumentException(
                        "SubtreeGen state requires a BufferStackTransducer");
            }
            try {
                // applies to dest first, src match scenario
                bTransducer.setState(bTransducer.getMemoutState());
                bTransducer.setPaused(false);

                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

}