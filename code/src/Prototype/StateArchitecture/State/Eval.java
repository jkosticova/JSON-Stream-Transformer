package Prototype.StateArchitecture.State;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.Transducer;
import Prototype.Writer.JsonWriter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.util.Stack;


/*
This state tracks the progress of a JSONPath match.

At the same time it copies input events to the output.
*/
public class Eval implements State {
    Transducer transducer;
    private JsonWriter writer;
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;
    private PathAutomaton pa;

    public Eval(Transducer transducer) {
        this.transducer = transducer;
        init();
    }

    private void init() {
        this.writer = this.transducer.getWriter();
        this.paStack = this.transducer.getPaStack();
        this.indexStack = this.transducer.getIndexStack();    
        this.pa = this.transducer.getPa();    
    }

    public void process(JsonParser parser) {        
        int paState;                
        try {
            init();
            JsonToken event = parser.currentToken();
            switch (event) {
                case START_ARRAY:                    
                    if (paStack.peek().equals(ARR_MARKER)) {
                        HandleArrayElement();
                    }

                    if (pa.isFinal(paStack.peek())) {
                        TransitionToMatch();                                                
                    }
                    
                    indexStack.push(0);
                    paStack.push(ARR_MARKER);

                    break;
                case END_ARRAY:
                    indexStack.pop();
                    
                    paStack.pop();
                    paStack.pop();

                    break;
                case START_OBJECT:
                    if (paStack.peek().equals(ARR_MARKER)) {
                        HandleArrayElement();
                    }
                    
                    if (pa.isFinal(paStack.peek())) {
                        TransitionToMatch();                        
                    }
                    
                    paStack.push(OBJ_MARKER);
                    
                    break;
                case END_OBJECT:
                    if (!paStack.peek().equals(ARR_MARKER)) {
                        paStack.pop();
                    }
                    if (!paStack.peek().equals(ARR_MARKER)) {
                        paStack.pop();
                    }

                    break;
                case FIELD_NAME:
                    // field name after start object or start array
                    if (paStack.peek() < 0) {
                        int marker = paStack.pop(); // pop OBJ_MARKER
                        paState = paStack.peek();
                        paStack.push(marker); // push OBJ_MARKER back
                    }
                    // field name within object
                    else {
                        paState = paStack.peek();
                    }
                    paStack.push(pa.transition(paState, parser.getParsingContext().getCurrentName()));

                    if (pa.isFinal(paStack.peek())) {
                        TransitionToMatch();                    
                    }

                    break;
                case VALUE_FALSE:
                case VALUE_NULL:
                case VALUE_TRUE:
                case VALUE_STRING:
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    if (paStack.peek().equals(ARR_MARKER)) {
                        HandleArrayElement();                                                
                    }

                    if (pa.isFinal(paStack.peek())) {
                        TransitionToMatch();
                        // tu sa nepopuje??                     
                    }
                    else {
                        paStack.pop(); // pop new state in case of array or fieldname
                    }
                    break;
            }                      
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isGenerating() {
        return true;
    }

    private void TransitionToMatch() {
        transducer.setState(transducer.getMatchState());
        transducer.setPaused(true);
        transducer.setNoGen(true);        
    }

    private void HandleArrayElement() {
        Integer i = indexStack.pop();
        paStack.pop(); // pop ARR_MARKER
        int paState = paStack.peek();
        paStack.push(ARR_MARKER); // push ARR_MARKER back
        paStack.push(pa.transition(paState, i.toString()));
        indexStack.push(i + 1);
    }
}
