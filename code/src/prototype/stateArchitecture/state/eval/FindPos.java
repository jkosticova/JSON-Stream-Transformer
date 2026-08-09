package prototype.stateArchitecture.state.eval;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import prototype.specificationParser.AddTransformation;
import prototype.specificationParser.CopyTransformation;
import prototype.specificationParser.MoveTransformation;
import prototype.specificationParser.TransformationFormat;
import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.transducer.StackTransducer;
import prototype.stateArchitecture.transducer.Transducer;

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
    private final StackTransducer transducer;            
    private TransformationFormat specification;    
    // we use depth integer instead of using stack to remember current nesting level
    private int depth;
    private int searchedIndex = -1;
    
    public FindPos(Transducer transducer){
        if (!(transducer instanceof StackTransducer)) {
            throw new IllegalArgumentException(
                "MeminSkip requires a StackTransducer"
            );
        }
        this.transducer = (StackTransducer) transducer;        
        init();
        this.depth = 0;
    
    }

    private void init() {                
        this.specification = transducer.getSpecification();
        Integer searchedIndexInteger = null;
        if (specification instanceof AddTransformation) {
            searchedIndexInteger = ((AddTransformation) specification).getIndex();
        }
        else if (specification instanceof CopyTransformation) {
            searchedIndexInteger = ((CopyTransformation) specification).getIndex();
        }
        else if (specification instanceof MoveTransformation) {
            searchedIndexInteger = ((MoveTransformation) specification).getIndex();
        };                                    
        if (searchedIndexInteger == null) {
            this.searchedIndex = -1;
        }
        else {
            this.searchedIndex = searchedIndexInteger;
        }
    }

    @Override
    public void process(JsonParser parser) {                 
        transducer.setGenerating(true);
        transducer.setPaused(false);
        try {
            JsonToken event = parser.currentToken();                        
            switch (event) {
                case START_ARRAY:
                    if (this.depth == 0) {
                        transducer.pushArray();                        
                    }                    
                case START_OBJECT:                    
                    // i-th array element 
                    if (this.depth == 1 && transducer.inArray()) {
                        if (transducer.atArrayIndex(searchedIndex)) {                        
                            transducer.increaseArraySize();
                            transitionToMatchPos();
                            return;                        
                        }
                        transducer.increaseArraySize();
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
                    if (this.depth == 1 && transducer.inArray()) {
                        if (transducer.atArrayIndex(searchedIndex)) {                        
                            transducer.increaseArraySize();
                            transitionToMatchPos();
                            return;                        
                        }
                        transducer.increaseArraySize();
                    }                                       
                    break;
            }       
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }    

    private void transitionToMatchPos() {
        transducer.setState(transducer.getMatchPosState());
        transducer.setPaused(true);    
        transducer.setGenerating(false);
    }

    

}
