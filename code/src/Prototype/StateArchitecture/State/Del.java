package Prototype.StateArchitecture.State;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.PathAutomaton.SimplePathAutomaton;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.Stack;

import static Prototype.Utils.Helper.GetSimplifiedJSONPathFromStack;
import static Prototype.Utils.Helper.isNumeric;

public class Del implements State {
    Transducer transducer;
    private final Stack<Integer> paStack;
    private final Stack<Integer> indexStack;
    private final JsonGenerator generator;
    private final TransformationFormat specification;
    private final PathAutomaton pa;

    public Del(Transducer transducer) {
        this.transducer = transducer;
        this.generator = transducer.getGenerator();
        this.paStack = transducer.getPaStack();
        this.indexStack = transducer.getIndexStack();
        this.specification = transducer.getSpecification();
        this.pa = new SimplePathAutomaton(specification.getPath(), null);
    }

    public void process(JsonToken event, JsonParser parser) {
        Integer paState;
        switch (event) {
            case START_ARRAY:
                if (paStack.peek().equals(ARR_MARKER)) {
                    Integer i = indexStack.pop();
                    paStack.pop(); // pop ARR_MARKER
                    paState = paStack.peek();
                    paStack.push(ARR_MARKER); // push ARR_MARKER back
                    paStack.push(pa.transition(paState, i.toString()));                        
                    indexStack.push(i + 1);
                }

                /*if (pa.isFinal(paStack.peek())) {
                    try {
                        generator.copyCurrentEvent(parser);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    transducer.setState(new Gen(transducer));
                    transducer.setPaused(false);
                    return;
                }*/

                indexStack.push(0);
                paStack.push(ARR_MARKER);

                break;
            case END_ARRAY:
                if (pa.isFinal(paStack.peek())) {
                    try {
                        generator.copyCurrentEvent(parser);
                        transducer.setState(new Gen(transducer));
                        transducer.setPaused(false);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }


                indexStack.pop();
                paStack.pop();

                if (pa.isFinal(paStack.peek())) {
                    transducer.setState(new Gen(transducer));
                    transducer.setPaused(false);
                    return;
                }

                paStack.pop();

                break;
            case START_OBJECT:
                if (paStack.peek().equals(ARR_MARKER)) {
                    Integer i = indexStack.pop();
                    paStack.pop(); // pop ARR_MARKER
                    paState = paStack.peek();
                    paStack.push(ARR_MARKER); // push ARR_MARKER back
                    paStack.push(pa.transition(paState, i.toString()));                        
                    indexStack.push(i + 1);
                }

                paStack.push(OBJ_MARKER);

                break;
            case END_OBJECT:
                paStack.pop();

                if (pa.isFinal(paStack.peek())) {
                    transducer.setState(new Gen(transducer));
                    transducer.setPaused(false);
                    return;
                }

                paStack.pop();

                break;
            case FIELD_NAME:
                paStack.pop(); // pop OBJ_MARKER
                paState = paStack.peek();
                paStack.push(OBJ_MARKER); // push OBJ_MARKER back
                paStack.push(pa.transition(paState, parser.getParsingContext().getCurrentName()));

                break;
            case VALUE_FALSE:
            case VALUE_NULL:
            case VALUE_TRUE:
            case VALUE_STRING:
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                // get marker
                Integer top = paStack.pop();
                Integer marker = null;
                if (top > 0) {
                    marker = paStack.peek();
                }
                paStack.push(top);

                if (paStack.peek().equals(ARR_MARKER)) {
                    Integer i = indexStack.pop();
                    paStack.pop(); // pop ARR_MARKER
                    paState = paStack.peek();
                    paStack.push(ARR_MARKER); // push ARR_MARKER back
                    paStack.push(pa.transition(paState, i.toString()));                        
                    indexStack.push(i + 1);
                }

                   if (pa.isFinal(paStack.peek())) {
                       // copy rest of the values for arrays only
                       if (marker !=null && marker.equals(ARR_MARKER)) {
                         try {
                             generator.copyCurrentEvent(parser);
                         } catch (IOException e) {
                             throw new RuntimeException(e);
                         }
                     }
                    transducer.setState(new Gen(transducer));
                    transducer.setPaused(false);
                    return;
                }

                paStack.pop();

                break;
        }

    }
}
