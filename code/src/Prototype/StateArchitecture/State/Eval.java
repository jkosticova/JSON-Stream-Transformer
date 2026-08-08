package Prototype.StateArchitecture.State;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.StateArchitecture.State.JsonWalker.EvalWalker;
import Prototype.StateArchitecture.Transducer.StackTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.util.Stack;

public class Eval implements State {
    private final Transducer transducer;    
    private EvalWalker walker;
    private Stack<Integer> paStack;
    private Stack<Integer> indexStack;    
    private PathAutomaton pa;

    public Eval(Transducer transducer) {
        this.transducer = transducer;
        this.walker = new EvalWalker(transducer);    
    }

    @Override
    public void process(JsonParser parser) {        
        transducer.setGenerating(true);        
        walker.process(parser);                    
    }  
    
}
