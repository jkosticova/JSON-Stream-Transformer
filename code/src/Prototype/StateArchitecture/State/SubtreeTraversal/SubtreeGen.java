package prototype.stateArchitecture.state.subtreeTraversal;

import com.fasterxml.jackson.core.JsonParser;

import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.transducer.Transducer;

/*
  This state generates the current subtree to the output
  Entering token: the start of given value (START_ARRAY, START_OBJECT, literal value)
  Leaving token: one token after the end of given value
*/
public class SubtreeGen implements State {
    private final Transducer transducer;
    private SubtreeProcess subtreeProcessState;

    public SubtreeGen(Transducer transducer) {
        this.transducer = transducer;
        this.subtreeProcessState = new SubtreeProcess();
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setPaused(false);
        transducer.setGenerating(true);
        this.subtreeProcessState.process(parser);
        if (subtreeProcessState.isSubtreeEnd()) {
            try {
                // applies to dest first, src match scenario
                transducer.setState(transducer.getMemoutState());
                transducer.setPaused(false);

                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

}