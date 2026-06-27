package Prototype.StateArchitecture.State;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public interface State {
    int INITIAL_PA_STATE = 0;
    int INITIAL_ARR_INDEX = 0;
    int INITIAL_ARR_SIZE = 0;
    int OBJECT_ARR_INDEX = -1;
    int ARR_MARKER = -1;
    int OBJ_MARKER = -2;

    void process(JsonToken event, JsonParser parser);
}
