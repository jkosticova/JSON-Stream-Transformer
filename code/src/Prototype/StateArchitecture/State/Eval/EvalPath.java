package prototype.stateArchitecture.state.eval;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.state.match.MatchPath;
import prototype.stateArchitecture.transducer.BufferStackTransducer;
import prototype.stateArchitecture.transducer.StackTransducer;
import prototype.stateArchitecture.transducer.Transducer;
import prototype.stateArchitecture.transducer.TransducerException;
import prototype.pathAutomaton.PathAutomaton;

import java.io.IOException;
import java.util.Stack;

public class EvalPath implements State {
    private final StackTransducer transducer;
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;
    private PathAutomaton pa;

    public EvalPath(Transducer transducer) {
        if (!(transducer instanceof StackTransducer)) {
        throw new IllegalArgumentException(
            "MeminSkip requires a StackTransducer"
            );
        }   
        this.transducer = (StackTransducer)transducer;
        this.paStack = this.transducer.getPaStack();
        this.indexStack = this.transducer.getIndexStack();
        this.pa = this.transducer.getPa();
    }

    @Override
    public void process(JsonParser parser) {
        transducer.setGenerating(true);
        transducer.setPaused(false);
        int paState;

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

                // getText() is the only call in this method that declares a
                // checked exception (IOException, from the underlying
                // stream) - scoped narrowly here so a genuine I/O failure
                // is reported clearly, without a broad try/catch hiding
                // unrelated bugs (e.g. stack underflow) elsewhere in the
                // switch under a generic RuntimeException.
                String fieldName;
                try {
                    fieldName = parser.getText();
                } catch (IOException e) {
                    throw new TransducerException("Failed to read field name during path evaluation", e);
                }

                paStack.push(pa.transition(paState, fieldName));

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
    }

    /*
     * Perform transducer transition to match state
     */
    private void transitionToMatch(byte matchType) {
        transducer.setState(transducer.getMatchPathState());
        transducer.setPaused(true);
        transducer.setGenerating(false);        
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
