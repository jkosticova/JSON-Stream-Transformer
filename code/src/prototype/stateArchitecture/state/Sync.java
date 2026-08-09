package prototype.stateArchitecture.state;

import com.fasterxml.jackson.core.JsonParser;

import prototype.specificationParser.CopyTransformation;
import prototype.specificationParser.MoveTransformation;
import prototype.specificationParser.TransformationFormat;
import prototype.stateArchitecture.transducer.BufferSyncTransducer;
import prototype.stateArchitecture.transducer.StackTransducer;
import prototype.stateArchitecture.state.eval.EvalPath;
import prototype.stateArchitecture.state.freeTraversal.MeminSkip;
import prototype.stateArchitecture.state.match.MatchPath;

/* 
   This is a synchronization state of BufferTransducer. It provides 
   - explicit synchronization if the action depends on both transducer states
   - synchronization of individual runs if the action of a single transducer 
     depends only on its own state and transducerRole / firstMatch indicators
*/

public class Sync implements State {
    private final StackTransducer sourceTransducer;
    private final StackTransducer destinationTransducer;
    private final TransformationFormat specification;

    public Sync(BufferSyncTransducer transducer) {
        this.sourceTransducer = transducer.getSourceTransducer();
        this.destinationTransducer = transducer.getDestinationTransducer();
        this.specification = transducer.getSpecification();
    }

    @Override
    public void process(JsonParser parser) {

        State sourceState = sourceTransducer.getCurrentState();
        State destinationState = destinationTransducer.getCurrentState();

        if (specification instanceof CopyTransformation) {

            // explicit synchronization
            // DEST matched first, end of MeminSkip
            if ((sourceState instanceof MatchPath) && (destinationState instanceof MeminSkip)) {

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
            // explicit synchronization
            // DEST matched first, end of MeminSkip
            // synchronization must happen one step before match, otherwise
            // also matched symbol would be put into memory (we don't want this for move
            // transf.)
            if ((sourceState instanceof EvalPath) && (destinationState instanceof MeminSkip)) {
                sourceState.process(parser);
                if (sourceTransducer.getCurrentState() instanceof MatchPath) {
                    destinationTransducer.setState(destinationTransducer.getGenState());
                    sourceState.process(parser);
                } else {
                    destinationState.process(parser);
                }
            } else {
                synchronizeIndividualRuns(parser);
            }

        }

    }
    /*
      Synchronization of individual runs
    */
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
