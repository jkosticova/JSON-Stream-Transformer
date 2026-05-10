package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.Stack;

public class Memin implements State {
    Transducer transducer;
    private JsonGenerator generator;
    private Stack<String> pathStack;
    private Stack<Integer> indexStack;

    public Memin(Transducer transducer) {
        this.transducer = transducer;
        init();
    }

    private void init() {
        this.generator = this.transducer.getGenerator();
        this.pathStack = this.transducer.getPathStack();
        this.indexStack = this.transducer.getIndexStack();
    }

    public void process(JsonToken event, JsonParser parser) {
        init();

        switch (event) {
            case START_ARRAY:
                if (pathStack.peek().equals("arr")) {
                    Integer i = indexStack.pop();
                    pathStack.push(i.toString());
                    indexStack.push(i + 1);
                }

                indexStack.push(0);
                pathStack.push("arr");

                break;
            case END_ARRAY:
                indexStack.pop();
                pathStack.pop();
                pathStack.pop();

                break;
            case START_OBJECT:
                if (pathStack.peek().equals("arr")) {
                    Integer i = indexStack.pop();
                    pathStack.push(i.toString());
                    indexStack.push(i + 1);
                }

                pathStack.push("obj");

                break;
            case END_OBJECT:
                pathStack.pop();
                pathStack.pop();

                break;
            case FIELD_NAME:
                pathStack.push(parser.getParsingContext().getCurrentName());

                break;
            case VALUE_FALSE:
            case VALUE_NULL:
            case VALUE_TRUE:
            case VALUE_STRING:
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                if (pathStack.peek().equals("arr")) {
                    Integer i = indexStack.pop();
                    pathStack.push(i.toString());
                    indexStack.push(i + 1);
                }

                pathStack.pop();

                break;
        }

        transducer.addToMemory();
        try {
            generator.copyCurrentEvent(parser);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
