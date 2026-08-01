package Prototype.StateArchitecture.State;
import Prototype.StateArchitecture.Transducer.Transducer;
import Prototype.Writer.JsonWriter;

import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;

public class Gen implements State {
    private final Transducer transducer;
    private JsonWriter writer;

    public Gen(Transducer transducer) {
        this.transducer = transducer;
        init();
    }

    private void init() {
        this.writer = transducer.getWriter();
    }

    @Override
    public void process(JsonParser parser) {
        init();

        /*try {
            if (this.transducer.isGenerating()) {
                writer.writeCurrentEvent(parser);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }

    public boolean isGenerating() {
        return true;
    }
}
