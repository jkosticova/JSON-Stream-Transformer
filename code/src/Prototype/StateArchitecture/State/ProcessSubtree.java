package Prototype.StateArchitecture.State;

import com.fasterxml.jackson.core.JsonParser;

import Prototype.StateArchitecture.Transducer.Transducer;

public class ProcessSubtree implements State {    
    private int depth;

    public ProcessSubtree() {        
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
        //if (depth == 0) {
        //    return;
        //}
            
                
    }   
    
    public boolean isSubtreeEnd() {
        return (this.depth == 0);        
    }

}
