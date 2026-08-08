package Prototype.StateArchitecture.State.SubtreeTraversal;

import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.Transducer.Transducer;

import java.io.IOException;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/*
This state prunes current subtree, i.e., doesn't copy it to the output.
*/
public class SubtreeSkip implements State {
    private final Transducer transducer;
    private int depth;
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;

    public SubtreeSkip(Transducer transducer) {
        this.transducer = transducer;
        this.depth = 0;
        this.paStack = this.transducer.getPaStack();
        this.indexStack = this.transducer.getIndexStack();        
    }

    @Override
    public void process(JsonParser parser) {        
        transducer.setPaused(false);        
        transducer.setGenerating(false);                
        
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
                transducer.setState(transducer.getGenState());
                transducer.setPaused(false);                                
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
    }       


}