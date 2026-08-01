package Prototype.StateArchitecture.State;

import Prototype.StateArchitecture.Transducer.Transducer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/*
This state prunes current subtree, i.e., doesn't copy it to the output.
*/
public class SkipSubtree implements State {
    Transducer transducer;
    private int depth;

    public SkipSubtree(Transducer transducer) {
        this.transducer = transducer;
        this.depth = 0;
    }

    public void process(JsonParser parser) {        
        transducer.setPaused(false);        
        JsonToken event = parser.currentToken();        
        
        switch (event) {
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

        if (depth == 0) {
            try {
                // posledny token podstromu chcek tiez vynechat
                parser.nextToken(); 
                transducer.setState(transducer.getGenState());
                transducer.setPaused(false);                
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
    }

    public boolean isGenerating() {
        return false;
    }
}
