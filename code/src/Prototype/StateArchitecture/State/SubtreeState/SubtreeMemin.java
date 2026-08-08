package Prototype.StateArchitecture.State.SubtreeState;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.PathAutomaton.SimplePathAutomaton;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.State;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.Stack;

/* 
    This state traverses the value (subtree), puts it into the memory 
    and at the same time generates it to the output.
    Entering token: the start of given value (START_ARRAY, START_OBJECT, literal value)
    Leaving token: one token after the end of given value
*/
public class SubtreeMemin implements State {
    private final Transducer transducer;    
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;        
    private int depth;

    public SubtreeMemin(Transducer transducer) {
        this.transducer = transducer;
        init();
    }

    private void init() {        
        this.paStack = this.transducer.getPaStack();
        this.indexStack = this.transducer.getIndexStack();        
        this.depth = 0;
    }

    @Override
    public void process(JsonParser parser) {        
        transducer.setPaused(false);                       
        transducer.setGenerating(true);
        
        switch (parser.currentToken()) {
            case START_OBJECT:
            case START_ARRAY:
                depth++;
                break;
            case END_OBJECT:
            case END_ARRAY:
                depth--;
                break;
            default:
                break;
        }

        // current token is last token of given subtree
        if (depth == 0) {
            try {
                // also last token of given subtree must be added to the memory
                //transducer.addToMemory();                
                if (transducer.getFirstMatch() == BufferTransducer.SRC_FIRST) {
                    transducer.setState(transducer.getGenState());
                }
                else if (transducer.getFirstMatch() == BufferTransducer.DEST_FIRST) {
                    transducer.setState(transducer.getMemoutState());
                }
                else {
                    //TODO handle error
                }
                //transducer.setPaused(false);                
                //return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
        transducer.addToMemory();
    }        
 
}
