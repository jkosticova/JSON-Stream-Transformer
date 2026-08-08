package Prototype.StateArchitecture.State;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.StackTransducer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.OutputStream;

public class Sync implements State {
    private final BufferTransducer transducer;
    private final StackTransducer sourceTransducer;
    private final StackTransducer destinationTransducer;
    private final JsonGenerator generator;
    private final TransformationFormat specification;
    private final PathAutomaton sourcePa;

    public Sync(BufferTransducer transducer) {
        this.transducer = transducer;
        this.sourceTransducer = transducer.getSourceTransducer();
        this.destinationTransducer = transducer.getDestinationTransducer();
        this.generator = transducer.getGenerator();
        this.specification = transducer.getSpecification();
        this.sourcePa = sourceTransducer.getPa();
    }

    @Override
    public void process(JsonParser parser) {

        JsonToken event = parser.currentToken();

        sourceTransducer.setGenerator(generator);
        destinationTransducer.setGenerator(generator);

        State sourceState = sourceTransducer.getCurrentState();
        State destinationState = destinationTransducer.getCurrentState();

        try {
            if (specification instanceof CopyTransformation) {
                
                /* SOURCE MATCHED FIRST */

                // handled in baseline state

                /* DESTINATION MATCHED FIRST */

                // sprava sa podobne ako pri source first, ale tam bol source State gen
                if ((sourceState instanceof Eval) && (destinationState instanceof MatchPos)) {

                    if (((CopyTransformation) specification).getKey() != null) {
                        String key = ((CopyTransformation) specification).getKey();
                        generator.writeFieldName(key);
                    }

                    destinationTransducer.setState(new MeminSkip(destinationTransducer));                    
                    destinationTransducer.setPaused(true);
                
                } else if ((sourceState instanceof Match) && (destinationState instanceof MeminSkip)) {
                    // !!! match vola move to value
                    // fieldname musi dest transducer este dat do pamati cez meminSkip
                    sourceState.process(parser);                    
                    destinationTransducer.setState(destinationTransducer.getGenState());                    
                    sourceTransducer.setPaused(false);
                    destinationTransducer.setPaused(false);
                } else {                    
                    // fix paused state
                    boolean srcPaused = sourceTransducer.getPaused();
                    boolean destPaused = destinationTransducer.getPaused();
                    if (!destPaused && !srcPaused) {
                        // match ma side effect moveToNext!!!!
                        sourceState.process(parser);
                        destinationState.process(parser);

                    }
                    // process the same token by the paused transducers only
                    else {
                        if (srcPaused) {
                            sourceState.process(parser);
                        }
                        if (destPaused) {
                            destinationState.process(parser);
                        }
                    }
                    sourceState = sourceTransducer.getCurrentState();
                    destinationState = destinationTransducer.getCurrentState();
                }
            } else if (specification instanceof MoveTransformation) {
                
                
                //       } else 
                    {                    
                    // fix paused state
                    boolean srcPaused = sourceTransducer.getPaused();
                    boolean destPaused = destinationTransducer.getPaused();
                    if (!destPaused && !srcPaused) {
                        // match ma side effect moveToNext!!!!
                        sourceState.process(parser);
                        destinationState.process(parser);

                    }
                    // process the same token by the paused transducers only
                    else {
                        if (srcPaused) {
                            sourceState.process(parser);
                        }
                        if (destPaused) {
                            destinationState.process(parser);
                        }
                    }
                    sourceState = sourceTransducer.getCurrentState();
                    destinationState = destinationTransducer.getCurrentState();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isGenerating() {
        return true;
    }
}
