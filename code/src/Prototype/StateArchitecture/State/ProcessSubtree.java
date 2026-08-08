package Prototype.StateArchitecture.State;

import com.fasterxml.jackson.core.JsonParser;


/*
 We use a simple counter for traversing the subtree.
 Alternatively, the stack could be used (this would align with the formal algorithm).
*/
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
    }   
    
    public boolean isSubtreeEnd() {
        return (this.depth == 0);        
    }

}
