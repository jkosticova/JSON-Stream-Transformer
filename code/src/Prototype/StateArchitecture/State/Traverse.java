package Prototype.StateArchitecture.State;
import Prototype.StateArchitecture.JsonPushdownAutomaton.JsonPushdownAutomaton;
import com.fasterxml.jackson.core.JsonToken;


public class Traverse implements State {     

    @Override
    public void process(JsonToken token, String tokenString) {
        /* do nothing */
        return;        
    }
    
}
