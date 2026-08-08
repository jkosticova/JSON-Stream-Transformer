package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonParser;


/* 
    This state traverses the value (subtree) and puts it into the memory
    Entering token: the start of given value (START_ARRAY, START_OBJECT, literal value)
    Leaving token: one token after the end of given value
*/
public class MeminSkipSubtree implements State {
    private final Transducer transducer;    
    private ProcessSubtree processSubtreeState;

    public MeminSkipSubtree(Transducer transducer) {
        this.transducer = transducer;
        this.processSubtreeState = new ProcessSubtree();        
    }


    @Override
    public void process(JsonParser parser) {        
        transducer.setPaused(false);                       
        transducer.setGenerating(false);
        processSubtreeState.process(parser);
        /*switch (parser.currentToken()) {
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
        }*/
        transducer.addToMemory();                
        // current token is last token of given subtree
        if (processSubtreeState.isSubtreeEnd()) {
            try {                                
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
                
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
        //transducer.addToMemory();
    }        
    
}
