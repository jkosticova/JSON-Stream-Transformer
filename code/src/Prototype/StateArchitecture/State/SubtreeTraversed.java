package Prototype.StateArchitecture.State;

import com.fasterxml.jackson.core.JsonToken;

import Prototype.StateArchitecture.JsonPushdownAutomaton.JsonPushdownAutomaton;

public class SubtreeTraversed implements State {
     

    @Override
    public void process(JsonToken token, String tokenString) {
        /* do nothing */
        return;        
    }
}
