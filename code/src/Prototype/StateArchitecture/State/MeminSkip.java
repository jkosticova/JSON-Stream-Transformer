package Prototype.StateArchitecture.State;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.PathAutomaton.SimplePathAutomaton;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.util.Stack;

/*
    This state copies the input events to the memory.
    It does not generate the output.
    It does not manipulate the stack in case of stack transducer
*/
public class MeminSkip implements State {
    private final Transducer transducer;
    /*private Stack<Integer> paStack;
    private Stack<Integer> indexStack;
    private PathAutomaton pa;*/

    public MeminSkip(Transducer transducer) {
        this.transducer = transducer;
        //init();
    }

    /*private void init() {
        this.paStack = this.transducer.getPaStack();
        this.indexStack = this.transducer.getIndexStack();
        this.pa = transducer.getPa();
    }*/

    @Override
    public void process(JsonParser parser) {
        transducer.setPaused(false);        
        transducer.setGenerating(false);
        transducer.addToMemory();
    }
   
}
