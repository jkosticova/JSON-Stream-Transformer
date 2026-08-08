package Prototype.StateArchitecture.State.Eval;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.Transducer.StackTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.util.Stack;

public class EvalPath implements State {
    private final Transducer transducer;
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;
    private PathAutomaton pa;

    public EvalPath(Transducer transducer) {
        this.transducer = transducer;
        this.paStack = transducer.getPaStack();
        this.indexStack = transducer.getIndexStack();
        this.pa = transducer.getPa();
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setGenerating(true);
        transducer.setPaused(false);
        int paState;
        try {
            JsonToken event = parser.currentToken();
            switch (event) {
                case START_ARRAY:
                    if (paStack.peek().equals(ARR_MARKER)) {
                        handleArrayElement();
                    }
                    if (pa.isFinal(paStack.peek())) {
                        transitionToMatch(VALUE_MATCH);
                        indexStack.push(0);
                        paStack.push(ARR_MARKER);
                        return;
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
                        handleArrayElement();
                    }
                    if (pa.isFinal(paStack.peek())) {
                        transitionToMatch(VALUE_MATCH);
                        paStack.push(OBJ_MARKER);
                        return;
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
                    paStack.push(pa.transition(paState, parser.getText()));

                    // fieldname match
                    if (pa.isFinal(paStack.peek())) {
                        transitionToMatch(FIELDNAME_MATCH);
                        return;
                    }

                    break;
                case VALUE_FALSE:
                case VALUE_NULL:
                case VALUE_TRUE:
                case VALUE_STRING:
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    if (paStack.peek().equals(ARR_MARKER)) {
                        handleArrayElement();
                    }
                    if (pa.isFinal(paStack.peek())) {
                        transitionToMatch(VALUE_MATCH);

                        return;
                    }

                    paStack.pop(); // pop new state in case of array or fieldname
                    break;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Perform transducer transition to match state
     */
    private void transitionToMatch(byte matchType) {
        transducer.setState(transducer.getMatchState());
        transducer.setPaused(true);
        transducer.setGenerating(false);
        transducer.matchType = matchType;
    }

    /*
     * Perform path automaton transition on index of an array element and
     * increment array size correspondingly
     */
    private void handleArrayElement() {
        Integer i = indexStack.pop();
        paStack.pop(); // pop ARR_MARKER
        int paState = paStack.peek();
        paStack.push(ARR_MARKER); // push ARR_MARKER back
        paStack.push(pa.transition(paState, i.toString()));
        indexStack.push(i + 1);
    }

}
