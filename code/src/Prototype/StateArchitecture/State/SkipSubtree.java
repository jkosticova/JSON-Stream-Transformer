package Prototype.StateArchitecture.State;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.Transducer;
import Prototype.Writer.JsonWriter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.Stack;

/*
This state prunes current subtree, i.e., doesn't copy it to the output.
TODO: It should be sufficient to remember the depth.
*/
public class SkipSubtree implements State {
    Transducer transducer;
    private final Stack<Integer> paStack;
    private final Stack<Integer> indexStack;    
    private final TransformationFormat specification;
    private final PathAutomaton pa;
    private JsonWriter writer;

    public SkipSubtree(Transducer transducer) {
        this.transducer = transducer;        
        this.paStack = transducer.getPaStack();
        this.indexStack = transducer.getIndexStack();
        this.specification = transducer.getSpecification();
        this.pa = transducer.getPa();
        this.writer = transducer.getWriter();
    }

    public void process(JsonParser parser) {
        Integer paState;
        JsonToken event = parser.currentToken();
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
                
                indexStack.push(0);
                paStack.push(ARR_MARKER);

                break;
            case END_ARRAY:
                if (pa.isFinal(paStack.peek())) {
                    try {
                        writer.writeCurrentEvent(parser);
                        transducer.setState(transducer.getGenState());
                        transducer.setPaused(false);
                        // TODO!!!!!
                        parser.nextToken();
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }


                indexStack.pop();
                paStack.pop();

                if (pa.isFinal(paStack.peek())) {
                    transducer.setState(transducer.getGenState());
                    transducer.setPaused(false);
                    // TODO!!!!!
                    try {
                        parser.nextToken();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
                    transducer.setState(transducer.getGenState());
                    transducer.setPaused(false);
                    // TODO!!!!!
                    try {
                        parser.nextToken();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
                            if (transducer.isGenerating()) {
                                writer.writeCurrentEvent(parser);
                            }
                         } catch (IOException e) {
                             throw new RuntimeException(e);
                         }
                     }
                    transducer.setState(transducer.getGenState());
                    // TODO!!!!!
                    try {
                        parser.nextToken();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    transducer.setPaused(false);
                    return;
                }

                paStack.pop();

                break;
        }

    }

    public boolean isGenerating() {
        return false;
    }
}
