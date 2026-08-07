package Prototype.StateArchitecture.State;

import Prototype.PathAutomaton.PathAutomaton;
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

/*
This state search for position "index" within the current structure.
Transition to the state MatchPos happens when currentToken is:
- If "index" points to an existing array element (value): 
  => currentToken = value start (START_ARRAY, START_OBJECT or a primitive value)
- If "index" points to an nonexisting array element or the current structure is object
  => currentToken = current structure end (END_ARRAY or END_OBJECT)
When entering MatchPos state, the content of the stacks is aligned 
with the current structure (array or object). 

If the previous match was entered at key, it is necessary to move to the corresponding 
value and possibly align the content of the stacks.
*/
public class FindPos implements State {
    Transducer transducer;        
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;
    private TransformationFormat specification;    
    // we use depth integer instead of using stack to remember current nesting level
    private int depth;
    private Integer searchedIndex = null;      
    
    public FindPos(Transducer transducer) {
        this.transducer = transducer;
        init();
        this.depth = 0;
    
    }

    private void init() {        
        this.paStack = this.transducer.getPaStack();
        this.indexStack = this.transducer.getIndexStack();                
        this.specification = transducer.getSpecification();
        if (specification instanceof AddTransformation) {
            this.searchedIndex = ((AddTransformation) specification).getIndex();
        }
        else if (specification instanceof CopyTransformation) {
            this.searchedIndex = ((CopyTransformation) specification).getIndex();
        }
        else if (specification instanceof MoveTransformation) {
            this.searchedIndex = ((MoveTransformation) specification).getIndex();
        };                                    
    }

    public void process(JsonParser parser) {
        init();                
        // move from fieldname to the corresponding value
        if (this.depth == 0 && parser.getCurrentToken() == JsonToken.FIELD_NAME) {
            moveToValue(parser);
        }
        transducer.setPaused(false);
        try {
            JsonToken event = parser.currentToken();                        
            switch (event) {
                case START_ARRAY:
                    if (this.depth == 0) {
                        paStack.push(ARR_MARKER);
                        indexStack.push(0);
                    }
                case START_OBJECT:                    
                    // i-th array element 
                    if (this.depth == 1 && paStack.peek().equals(ARR_MARKER)) {
                        Integer i = indexStack.pop();
                        if (searchedIndex.equals(i)) {
                            transitionToMatchPos(); 
                            indexStack.push(i + 1);
                            return;                                                                       
                        }
                        indexStack.push(i + 1);
                    }                    
                    depth++;                    
                    break;                
                case END_ARRAY:
                case END_OBJECT:
                    depth--;                    
                    // end of the array / object
                    if (depth == 0) {
                        transitionToMatchPos();
                        return;
                    }
                    break;
                case FIELD_NAME:                    
                    break;
                case VALUE_FALSE:
                case VALUE_NULL:
                case VALUE_TRUE:
                case VALUE_STRING:
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    // ak sme na i-tom prvku pola
                    if (this.depth == 1 && paStack.peek().equals(ARR_MARKER)) {
                        Integer i = indexStack.pop();
                        if (searchedIndex.equals(i)) {
                            transitionToMatchPos();
                            indexStack.push(i+1);
                            return;
                        }
                        indexStack.push(i+1);
                    }                                        
                    break;
            }       
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isGenerating() {
        return true;
    }

    private void transitionToMatchPos() {
        transducer.setState(transducer.getMatchPosState());
        transducer.setPaused(true);    
    }

    
    private void moveToValue(JsonParser parser) {
           try {
            parser.nextToken(); // move to value and if it is a structure, process opening token
            if (parser.currentToken() == JsonToken.START_ARRAY) {
                paStack.push(ARR_MARKER);
                indexStack.push(0);
            }
            else if (parser.currentToken() == JsonToken.START_OBJECT) {
                paStack.push(OBJ_MARKER);
            }
           } catch (IOException e) {            
            e.printStackTrace();
           } 
    }

}
