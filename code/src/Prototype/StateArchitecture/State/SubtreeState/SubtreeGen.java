package Prototype.StateArchitecture.State.SubtreeState;

import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.Transducer.Transducer;

import java.io.IOException;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/*
This state generates the current subtree to the output
*/
public class SubtreeGen implements State {
    private final Transducer transducer;
    private SubtreeProcess processSubtreeState;

    public SubtreeGen(Transducer transducer) {
        this.transducer = transducer;
        this.processSubtreeState = new SubtreeProcess();
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setPaused(false);
        transducer.setGenerating(true);
        this.processSubtreeState.process(parser);
        if (processSubtreeState.isSubtreeEnd()) {
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