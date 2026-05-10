package Prototype.StateArchitecture.State;

import Prototype.SpecificationParser.AddTransformation;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.Stack;

import static Prototype.Utils.Helper.GetSimplifiedJSONPathFromStack;

public class Find_i implements State {
    Transducer transducer;
    private final Integer index;
    private JsonGenerator generator;
    private Stack<String> pathStack;
    private Stack<Integer> indexStack;
    private TransformationFormat specification;

    public Find_i(Transducer transducer, Integer index) {
        this.transducer = transducer;
        init();
        this.index = index;
    }

    private void init() {
        this.generator = this.transducer.getGenerator();
        this.pathStack = this.transducer.getPathStack();
        this.indexStack = this.transducer.getIndexStack();
        this.specification = this.transducer.getSpecification();
    }

    public void process(JsonToken event, JsonParser parser) {
        init();

        try {
            switch (event) {
                case START_ARRAY:
                    if (pathStack.peek().equals("arr")) {
                        Integer i = indexStack.pop();
                        pathStack.push(i.toString());
                        indexStack.push(i + 1);
                    }

                    if (specification instanceof AddTransformation && ((AddTransformation) specification).getIndex() != null) {
                        String index1 = pathStack.pop();
                        String array1 = pathStack.pop();

                        if (((AddTransformation) specification).getIndex().toString().equals(index1)
                                && specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                            pathStack.push(array1);
                            pathStack.push(index1);
                            transducer.setState(new Match_i(transducer));
                            transducer.setPaused(true);
                            return;
                        }

                        pathStack.push(array1);
                        pathStack.push(index1);
                    }

                    if (specification instanceof CopyTransformation) {
                        String index1 = pathStack.pop();
                        String array1 = pathStack.pop();

                        if (((CopyTransformation) specification).getIndex() != null && ((CopyTransformation) specification).getIndex().toString().equals(index1)
                                && specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                            pathStack.push(array1);
                            pathStack.push(index1);
                            transducer.setState(new Match_i(transducer));
                            transducer.setPaused(true);
                            return;
                        }
                        pathStack.push(array1);
                        pathStack.push(index1);
                    } else if (specification instanceof MoveTransformation) {
                        String index1 = pathStack.pop();
                        String array1 = pathStack.pop();
                        if (((MoveTransformation) specification).getIndex() != null && ((MoveTransformation) specification).getIndex().toString().equals(index1)
                                && specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                            pathStack.push(array1);
                            pathStack.push(index1);
                            pathStack.push("arr");
                            indexStack.push(0);
                            transducer.setState(new Match_i(transducer));
                            transducer.setPaused(true);
                            return;
                        }
                        pathStack.push(array1);
                        pathStack.push(index1);
                    }

                    indexStack.push(0);
                    pathStack.push("arr");

                    break;
                case END_ARRAY:
                    indexStack.pop();
                    pathStack.pop();

                    if (specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                        transducer.setState(new Match_i(transducer));
                        transducer.setPaused(true);
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
                    String index2 = pathStack.pop();
                    String array2 = pathStack.pop();

                    if (specification instanceof CopyTransformation && ((CopyTransformation) specification).getIndex() != null && ((CopyTransformation) specification).getIndex().toString().equals(index2)
                            && specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                        pathStack.push(array2);
                        pathStack.push(index2);
                        transducer.setState(new Match_i(transducer));
                        transducer.setPaused(true);
                        return;
                    } else if (specification instanceof MoveTransformation && ((MoveTransformation) specification).getIndex() != null && ((MoveTransformation) specification).getIndex().toString().equals(index2)
                            && specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                        pathStack.push(array2);
                        pathStack.push(index2);
                        transducer.setState(new Match_i(transducer));
                        transducer.setPaused(true);
                        return;
                    }

                    pathStack.push(array2);
                    pathStack.push(index2);

                    pathStack.push("obj");

                    break;
                case END_OBJECT:
                    pathStack.pop();

                    if (specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                        transducer.setState(new Match_i(transducer));
                        transducer.setPaused(true);
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

                    String index = pathStack.pop();
                    String array = pathStack.pop();

                    if (specification instanceof CopyTransformation && ((CopyTransformation) specification).getIndex() != null && ((CopyTransformation) specification).getIndex().toString().equals(index)
                            && specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                        pathStack.push(array);
                        pathStack.push(index);
                        transducer.setState(new Match_i(transducer));
                        transducer.setPaused(true);
                        return;
                    } else if (specification instanceof MoveTransformation && ((MoveTransformation) specification).getIndex() != null && ((MoveTransformation) specification).getIndex().toString().equals(index)
                            && specification.getPath().equals(GetSimplifiedJSONPathFromStack(pathStack))) {
                        pathStack.push(array);
                        pathStack.push(index);
                        transducer.setState(new Match_i(transducer));
                        transducer.setPaused(true);
                        return;
                    }

                    pathStack.push(array);
                    pathStack.push(index);


                    pathStack.pop();

                    break;
            }


            generator.copyCurrentEvent(parser);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
