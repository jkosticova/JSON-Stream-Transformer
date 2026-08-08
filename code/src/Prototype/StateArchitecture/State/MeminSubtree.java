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

public class MeminSubtree implements State {
    private final Transducer transducer;    
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;        
    private int depth;

    public MeminSubtree(Transducer transducer) {
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
                transducer.addToMemory();                
                if (transducer.getTransducerRole() == Transducer.SRC_TRANSDUCER) {
                    transducer.setState(transducer.getGenState());
                }
                else {
                    transducer.setState(transducer.getMemoutState());
                }
                transducer.setPaused(false);                
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
        transducer.addToMemory();
    }        

    @Override
    public boolean isGenerating() {
        return true;
    }
}
