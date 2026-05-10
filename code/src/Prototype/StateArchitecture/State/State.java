package Prototype.StateArchitecture.State;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public interface State {
    void process(JsonToken event, JsonParser parser);
}
