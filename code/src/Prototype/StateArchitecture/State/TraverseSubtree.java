package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.JsonPushdownAutomaton.JsonPushdownAutomaton;
import Prototype.StateArchitecture.JsonPushdownAutomaton.ProcessingResult;

import com.fasterxml.jackson.core.JsonToken;

/*
This state prunes current subtree, i.e., doesn't copy it to the output.
*/
public class TraverseSubtree implements State {    
    private final JsonPushdownAutomaton jsonPda;
    private int depth;

    public TraverseSubtree(JsonPushdownAutomaton jsonPda) {
        this.jsonPda = jsonPda;                
        this.depth = 0;
    }
        
    public void process(JsonToken token, String tokenString) {        
        ProcessingResult result = new ProcessingResult();
        jsonPda.proceed();
                
        switch (token) {
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

        // track subtree
        if (depth == 0) {
            try {
                // posledny token podstromu chcek tiez vynechat                
                result.moveToNext = true; //parser.nextToken(); 
                
                jsonPda.setState(jsonPda.getTraverseState());
                jsonPda.pause();
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }     
        
    }


}
