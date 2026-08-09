package prototype.stateArchitecture.state;

import java.nio.Buffer;

import com.fasterxml.jackson.core.JsonParser;

import prototype.stateArchitecture.transducer.BufferStackTransducer;
import prototype.stateArchitecture.transducer.Transducer;

/* 
   This state generates the content of the buffer to the output AT ONCE.
   It uses "buffer.serialize(generator)".      
   Entering token = leaving token.
*/
public class Memout implements State {
    private final BufferStackTransducer transducer;

    public Memout(Transducer transducer) {
        if (transducer instanceof BufferStackTransducer bufferStackTransducer) {
            this.transducer = bufferStackTransducer;
        } else {
            throw new IllegalArgumentException(
                    "SubtreeMemin state requires a BufferStackTransducer");
        }
        transducer.setGenerating(false);
    }

    @Override
    public void process(JsonParser parser) {        
        transducer.setGenerating(false);
        transducer.getFromMemory();            
        // implicit transition - now sufficies, but can be generalized in the future
        transducer.setState(transducer.getGenState());        
        transducer.setPaused(true);        
    }
 
}
