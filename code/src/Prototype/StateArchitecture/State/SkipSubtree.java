package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.Transducer;

import java.io.IOException;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/*
This state prunes current subtree, i.e., doesn't copy it to the output.
*/
public class SkipSubtree implements State {
    private final Transducer transducer;
    private int depth;
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;

    public SkipSubtree(Transducer transducer) {
        this.transducer = transducer;
        this.depth = 0;
        this.paStack = this.transducer.getPaStack();
        this.indexStack = this.transducer.getIndexStack();        
    }

    @Override
    public void process(JsonParser parser) {        
        transducer.setPaused(false);        
        
        // explicit move to value in case of fieldname match
        if (this.depth == 0 && parser.getCurrentToken() == JsonToken.FIELD_NAME) {
            moveToValue(parser);
        }
        
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
                // last token of given subtree must be skipped
                parser.nextToken(); 
                transducer.setState(transducer.getGenState());
                transducer.setPaused(false);                
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
    }

    
    @Override
    public boolean isGenerating() {
        return false;
    }

    private void moveToValue(JsonParser parser) {
           try {
            parser.nextToken(); // move to value and if it is a structure, process opening token
            if (parser.currentToken() == JsonToken.START_ARRAY) {
                paStack.push(ARR_MARKER);
                indexStack.push(0);
            }
            else if (parser.currentToken() == JsonToken.START_OBJECT) {
                paStack.push(OBJ_MARKER);
            }
           } catch (IOException e) {            
            e.printStackTrace();
           } 
    }
}