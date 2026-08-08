package Prototype.StateArchitecture.State;

import com.fasterxml.jackson.core.JsonParser;

public interface State {
    int INITIAL_PA_STATE = 0;
    int INITIAL_ARR_INDEX = 0;
    int INITIAL_ARR_SIZE = 0;
    int OBJECT_ARR_INDEX = -1;
    int ARR_MARKER = -1;
    int OBJ_MARKER = -2;

    // match type
    byte NO_MATCH = 0;
    byte FIELDNAME_MATCH = 1;
    byte VALUE_MATCH = 2;

    void process(JsonParser parser);    
    boolean isGenerating();
}
