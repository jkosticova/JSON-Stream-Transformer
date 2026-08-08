package Prototype.StateArchitecture.State;

import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.StackTransducer;

import com.fasterxml.jackson.core.JsonParser;

public class Sync implements State {
    private final StackTransducer sourceTransducer;
    private final StackTransducer destinationTransducer;
    private final TransformationFormat specification;

    public Sync(BufferTransducer transducer) {
        this.sourceTransducer = transducer.getSourceTransducer();
        this.destinationTransducer = transducer.getDestinationTransducer();
        this.specification = transducer.getSpecification();
    }

    @Override
    public void process(JsonParser parser) {

        State sourceState = sourceTransducer.getCurrentState();
        State destinationState = destinationTransducer.getCurrentState();

        try {
            if (specification instanceof CopyTransformation) {

                /* SOURCE MATCHED FIRST */

                // handled in baseline state

                /* DESTINATION MATCHED FIRST */

                if ((sourceState instanceof Match) && (destinationState instanceof MeminSkip)) {

                    destinationTransducer.setState(destinationTransducer.getGenState());
                    // !!! performs move to value and value shouldn't be place into memory by
                    // MeminSkip
                    // therefore we move dest transducer to gen before
                    sourceState.process(parser);

                    // sourceTransducer.setPaused(false);
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
                       synchronizeIndividualRuns(parser);
                    }
                }
            } else if (specification instanceof MoveTransformation) {

                if ((sourceState instanceof Eval) && (destinationState instanceof MeminSkip)) {
                    sourceState.process(parser);
                    if (sourceTransducer.getCurrentState() instanceof Match) {
                        destinationTransducer.setState(destinationTransducer.getGenState());
                        sourceState.process(parser);
                    } else {
                        destinationState.process(parser);
                    }
                } else {
                    synchronizeIndividualRuns(parser);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void synchronizeIndividualRuns(JsonParser parser) {
        // fix paused state
        boolean srcPaused = sourceTransducer.getPaused();
        boolean destPaused = destinationTransducer.getPaused();
        if (!destPaused && !srcPaused) {
            // match ma side effect moveToNext!!!!
            sourceTransducer.getCurrentState().process(parser);
            destinationTransducer.getCurrentState().process(parser);

        }
        // process the same token by the paused transducers only
        else {
            if (srcPaused) {
                sourceTransducer.getCurrentState().process(parser);
            }
            if (destPaused) {
                destinationTransducer.getCurrentState().process(parser);
            }
        }

    }

}
