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
    private int depth;
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;

    public GenSubtree(Transducer transducer) {
        this.transducer = transducer;
        this.depth = 0;
        this.paStack = this.transducer.getPaStack();
        this.indexStack = this.transducer.getIndexStack();        
    }

    @Override
    public void process(JsonParser parser) {        
        transducer.setPaused(false);        
        transducer.setGenerating(true);                
        
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