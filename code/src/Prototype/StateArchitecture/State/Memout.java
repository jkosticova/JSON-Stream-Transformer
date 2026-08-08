package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;

import java.nio.Buffer;

import com.fasterxml.jackson.core.JsonParser;

/* 
   This state generates the content of the buffer to the output AT ONCE.
   It uses "buffer.serialize(generator)".      
   Entering token = leaving token.
*/
public class Memout implements State {
    private final Transducer transducer;

    public Memout(Transducer transducer) {
        this.transducer = transducer;
        transducer.setGenerating(false);
    }

    @Override
    public void process(JsonParser parser) {        
        transducer.setGenerating(false);
        transducer.getFromMemory();            
        transducer.setState(transducer.getGenState());        
        transducer.setPaused(true);        
    }
 
}
