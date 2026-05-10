package Prototype.StateArchitecture.State;

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
    private final Stack<String> pathStack;
    private final Stack<Integer> indexStack;
    private final JsonGenerator generator;
    private final TransformationFormat specification;

    public Del(Transducer transducer) {
        this.transducer = transducer;
        this.generator = transducer.getGenerator();
        this.pathStack = transducer.getPathStack();
        this.indexStack = transducer.getIndexStack();
        this.specification = transducer.getSpecification();
    }

    public void process(JsonToken event, JsonParser parser) {
        switch (event) {
            case START_ARRAY:
                if (pathStack.peek().equals("arr")) {
                    Integer i = indexStack.pop();
                    pathStack.push(i.toString());
                    indexStack.push(i + 1);
                }

                if (isNumeric(pathStack.peek()) && specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                    try {
                        generator.copyCurrentEvent(parser);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    transducer.setState(new Gen(transducer));
                    transducer.setPaused(false);
                    return;
                }

                indexStack.push(0);
                pathStack.push("arr");

                break;
            case END_ARRAY:
                if (specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
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
                pathStack.pop();

                if (specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                    transducer.setState(new Gen(transducer));
                    transducer.setPaused(false);
                    return;
                }

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

                if (specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                    transducer.setState(new Gen(transducer));
                    transducer.setPaused(false);
                    return;
                }

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

                if (specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                    if (isNumeric(pathStack.peek())) {
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

                pathStack.pop();

                break;
        }

    }
}
