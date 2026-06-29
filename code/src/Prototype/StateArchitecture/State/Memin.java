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

public class Memin implements State {
    Transducer transducer;
    private JsonGenerator generator;
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;    
    private PathAutomaton pa;

    public Memin(Transducer transducer) {
        this.transducer = transducer;
        init();
    }

    private void init() {
        this.generator = this.transducer.getGenerator();
        this.paStack = this.transducer.getPaStack();
        this.indexStack = this.transducer.getIndexStack();
        this.pa = transducer.getPa();
    }

    public void process(JsonToken event, JsonParser parser) {
        init();
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
                if (paStack.peek().equals(ARR_MARKER)) {
                    Integer i = indexStack.pop();
                        paStack.pop(); // pop ARR_MARKER
                        paState = paStack.peek();
                        paStack.push(ARR_MARKER); // push ARR_MARKER back
                        paStack.push(pa.transition(paState, i.toString()));                        
                        indexStack.push(i + 1);
                }

                paStack.pop();

                break;
        }

        transducer.addToMemory();
        try {
            if (this.transducer.isGenerating()) {
                generator.copyCurrentEvent(parser);
            }            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
