package Prototype.StateArchitecture.State;

import com.fasterxml.jackson.core.JsonParser;

import Prototype.StateArchitecture.Transducer.Transducer;

public class ProcessSubtree implements State {
    private Transducer transducer;
    private int depth;

    public ProcessSubtree(Transducer transducer) {
        this.transducer = transducer;
        this.depth = 0;
    }

    @Override
    public void process(JsonParser parser) {                
        
        switch (parser.currentToken()) {
            case START_OBJECT:
            case START_ARRAY:
                depth++;
                break;
            case END_OBJECT:
            case END_ARRAY:
                depth--;
                break;
            default:
                break;
        }
        // current token is last token of given subtree
        if (depth == 0) {
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
