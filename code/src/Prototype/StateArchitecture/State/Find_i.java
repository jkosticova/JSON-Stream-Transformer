package Prototype.StateArchitecture.State;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.PathAutomaton.SimplePathAutomaton;
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

public class Find_i implements State {
    Transducer transducer;    
    private JsonGenerator generator;
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;
    private TransformationFormat specification;
    private PathAutomaton pa;

    public Find_i(Transducer transducer) {
        this.transducer = transducer;
        init();
    
    }

    private void init() {
        this.generator = this.transducer.getGenerator();
        this.paStack = this.transducer.getPaStack();
        this.indexStack = this.transducer.getIndexStack();
        this.specification = this.transducer.getSpecification();
        
        pa = transducer.getPa();
            
        
    }

    public void process(JsonToken event, JsonParser parser) {
        init();
        Integer paState;
        try {
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

                    if (specification instanceof AddTransformation && ((AddTransformation) specification).getIndex() != null) {
                        Integer state1 = paStack.pop();                        
                        Integer array1 = paStack.pop(); // ARR_MARKER?
                        Integer index1 = indexStack.peek()-1;
                        
                        if (((AddTransformation) specification).getIndex().equals(index1)
                                && pa.isFinal(paStack.peek())) {
                            paStack.push(array1); // ARR_MARKER?
                            paStack.push(state1);
                            transducer.setState(transducer.getMatch_iState());
                            transducer.setPaused(true);
                            return;
                        }

                        paStack.push(array1);
                        paStack.push(state1);
                    }

                    if (specification instanceof CopyTransformation) {                        
                        Integer state1 = paStack.pop();                                                
                        Integer array1 = paStack.pop(); // ARR_MARKER?
                        Integer index1 = indexStack.peek()-1;

                        if (((CopyTransformation) specification).getIndex() != null && ((CopyTransformation) specification).getIndex().equals(index1)
                                && pa.isFinal(paStack.peek())) {
                            paStack.push(array1);
                            paStack.push(state1);
                            transducer.setState(transducer.getMatch_iState());
                            transducer.setPaused(true);
                            return;
                        }
                        paStack.push(array1);
                        paStack.push(state1);
                    } else if (specification instanceof MoveTransformation) {
                        Integer state1 = paStack.pop();                                                
                        Integer array1 = paStack.pop(); // ARR_MARKER?
                        Integer index1 = indexStack.peek()-1;

                        if (((MoveTransformation) specification).getIndex() != null && ((MoveTransformation) specification).getIndex().equals(index1)
                                && pa.isFinal(paStack.peek())) {
                            paStack.push(array1);
                            paStack.push(state1);
                            paStack.push(ARR_MARKER);
                            indexStack.push(0);
                            transducer.setState(transducer.getMatch_iState());
                            transducer.setPaused(true);
                            return;
                        }
                        paStack.push(array1);
                        paStack.push(state1);
                    }

                    indexStack.push(0);
                    paStack.push(ARR_MARKER);

                    break;
                case END_ARRAY:
                    indexStack.pop();
                    paStack.pop();

                    if (pa.isFinal(paStack.peek())) {
                        transducer.setState(transducer.getMatch_iState());
                        transducer.setPaused(true);
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
                    Integer state2 = paStack.pop();                                                
                    Integer array2 = paStack.pop(); // ARR_MARKER?
                    Integer index2 = indexStack.peek()-1;
                                        
                    if (specification instanceof CopyTransformation && ((CopyTransformation) specification).getIndex() != null && ((CopyTransformation) specification).getIndex().equals(index2)
                            && pa.isFinal(paStack.peek())) {
                        paStack.push(array2);
                        paStack.push(state2);
                        transducer.setState(transducer.getMatch_iState());
                        transducer.setPaused(true);
                        return;
                    } else if (specification instanceof MoveTransformation && ((MoveTransformation) specification).getIndex() != null && ((MoveTransformation) specification).getIndex().equals(index2)
                            && pa.isFinal(paStack.peek())) {
                        paStack.push(array2);
                        paStack.push(state2);
                        transducer.setState(transducer.getMatch_iState());
                        transducer.setPaused(true);
                        return;
                    }

                    paStack.push(array2);
                    paStack.push(state2);

                    paStack.push(OBJ_MARKER);

                    break;
                case END_OBJECT:
                    paStack.pop();

                    if (pa.isFinal(paStack.peek())) {
                        transducer.setState(transducer.getMatch_iState());
                        transducer.setPaused(true);
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
                    if (paStack.peek().equals(ARR_MARKER)) {
                        Integer i = indexStack.pop();
                        paStack.pop(); // pop ARR_MARKER
                        paState = paStack.peek();
                        paStack.push(ARR_MARKER); // push ARR_MARKER back
                        paStack.push(pa.transition(paState, i.toString()));
                        indexStack.push(i + 1);
                    }

                    Integer state3 = paStack.pop();                                                
                    Integer array3 = paStack.pop(); // ARR_MARKER?
                    Integer index3 = indexStack.peek()-1;

                    if (specification instanceof CopyTransformation && ((CopyTransformation) specification).getIndex() != null && ((CopyTransformation) specification).getIndex().equals(index3)
                            && pa.isFinal(paStack.peek())) {
                        paStack.push(array3);
                        paStack.push(state3);
                        transducer.setState(transducer.getMatch_iState());
                        transducer.setPaused(true);
                        return;
                    } else if (specification instanceof MoveTransformation && ((MoveTransformation) specification).getIndex() != null && ((MoveTransformation) specification).getIndex().equals(index3)
                            && pa.isFinal(paStack.peek())) {
                        paStack.push(array3);
                        paStack.push(state3);
                        transducer.setState(transducer.getMatch_iState());
                        transducer.setPaused(true);
                        return;
                    }

                    paStack.push(array3);
                    paStack.push(state3);


                    paStack.pop();

                    break;
            }


            if (this.transducer.isGenerating()) {
                generator.copyCurrentEvent(parser);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
