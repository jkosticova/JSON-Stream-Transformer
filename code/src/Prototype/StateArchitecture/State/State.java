package Prototype.StateArchitecture.State;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import Prototype.StateArchitecture.JsonPushdownAutomaton.ProcessingResult;

/*
Traversing states: Eval, EvalSkip, FindPos, Gen, Memin, MeminSkip, SkipSubtree
Switching states: Match, MatchPos 
Special states: Memout, Sync

*/

public interface State {
    int INITIAL_PA_STATE = 0;
    int INITIAL_ARR_INDEX = 0;
    int INITIAL_ARR_SIZE = 0;
    int OBJECT_ARR_INDEX = -1;
    int ARR_MARKER = -1;
    int OBJ_MARKER = -2;

    void process(JsonToken token, String tokenEvent);    

    
}
