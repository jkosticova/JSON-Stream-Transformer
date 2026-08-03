package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.JsonPushdownAutomaton.JsonPushdownAutomaton;
import Prototype.StateArchitecture.JsonPushdownAutomaton.ProcessingResult;
import Prototype.StateArchitecture.JsonPushdownAutomaton.StackConfiguration;

import com.fasterxml.jackson.core.JsonToken;

/*
This state tracks the progress of a JSONPath match.

At the same time it copies input events to the output.
*/
public class Eval implements State {
     private final JsonPushdownAutomaton jsonPda;

    public Eval(JsonPushdownAutomaton jsonPda) {
        this.jsonPda = jsonPda;                
    }

    public void process(JsonToken token, String tokenString) {        
        ProcessingResult result = new ProcessingResult();        
        try {
            switch (token) {
                case JsonToken.FIELD_NAME:
                    StackConfiguration sc = jsonPda.getStackPeek();                    
                    jsonPda.pushScWithTransition(tokenString, StackConfiguration.UNKNOWN_TYPE);
                    break;
                case JsonToken.START_OBJECT:                                         
                    sc = jsonPda.getStackPeek();
                    // object after fieldname
                    // fieldname: { ... }
                    if (sc.getValueType() == StackConfiguration.UNKNOWN_TYPE) {
                        sc.init(StackConfiguration.OBJECT_TYPE);
                    }
                    // object as an array element -> generate fieldName (=index)
                    // "[... { ... } ... ]"
                    else if (sc.getValueType() == StackConfiguration.ARRAY_TYPE) {
                        handleArrayElement(StackConfiguration.OBJECT_TYPE);                        
                    }
                    if (jsonPda.isPathMatch()) {
                        jsonPda.popSc();
                        TransitionToMatch(result);
                    }
                    break;                
                // $ [ ... ] - inicializacia
                case JsonToken.START_ARRAY:                    
                    sc = jsonPda.getStackPeek();
                    // array after fieldname
                    // fieldname : [ ... ]
                    if (sc.getValueType() == StackConfiguration.UNKNOWN_TYPE) {
                        sc.init(StackConfiguration.ARRAY_TYPE);
                    }
                    // array as an array element -> generate fieldName (=index)
                    // "[... [ ... ] ... ]"
                    else if (sc.getValueType() == StackConfiguration.ARRAY_TYPE) {
                        handleArrayElement(StackConfiguration.ARRAY_TYPE);
                        sc = jsonPda.getStackPeek();
                    }
                    if (jsonPda.isPathMatch()) {                        
                        jsonPda.popSc();
                        TransitionToMatch(result);
                    }
                    break;

                case JsonToken.END_OBJECT:
                case JsonToken.END_ARRAY:    
                    jsonPda.popSc();      
                    break;
                // literal value - after $, fieldName, "["
                default:
                    sc = jsonPda.getStackPeek();

                    // primitive after fieldname
                    // fieldname: val
                    if (sc.getValueType() == StackConfiguration.UNKNOWN_TYPE) {
                        sc.init(StackConfiguration.PRIMITIVE_TYPE);
                    }
                    // primitive value as an array element -> generate fieldName (=index)
                    // "[... val ... ]"
                    else if (sc.getValueType() == StackConfiguration.ARRAY_TYPE) {
                        handleArrayElement(StackConfiguration.PRIMITIVE_TYPE);
                        sc = jsonPda.getStackPeek();
                    }
                    if (jsonPda.isPathMatch()) {
                        TransitionToMatch(result);
                    }
                    jsonPda.popSc();
                    break;                               
            }           
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    

    private void TransitionToMatch(ProcessingResult result) {
        jsonPda.setState(jsonPda.getMatchState());
        result.pause = true;        
        jsonPda.stopGenerating();
    }

    /*
     * Perform transition on index of an array element and
     * increment array size correspondingly
     */
    private void handleArrayElement(byte valType) {
        StackConfiguration sc = jsonPda.getStackPeek();        
        jsonPda.pushScWithTransition(sc.getArraySize(), valType);
        sc.incrementArraySize();
    }
    
}
