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
    private final JsonGenerator nullGenerator;

    public Sync(BufferTransducer transducer) {
        this.transducer = transducer;
        this.sourceTransducer = transducer.getSourceTransducer();
        this.destinationTransducer = transducer.getDestinationTransducer();
        this.generator = transducer.getGenerator();
        this.specification = transducer.getSpecification();
        this.sourcePa = sourceTransducer.getPa();
        try {
            this.nullGenerator = new JsonFactory().createGenerator(OutputStream.nullOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void process(JsonParser parser) {

        JsonToken event = parser.currentToken();

        // the code switches between null generator and output file generator to avoid
        // allocating temporary buffers on heap
        // (restriction of measurement method)
        sourceTransducer.setGenerator(generator);
        destinationTransducer.setGenerator(generator);

        sourceTransducer.setIsGenerating(true);
        destinationTransducer.setIsGenerating(false);

        // sourceTransducer.setNoGen(false);
        // destinationTransducer.setNoGen(false);

        State sourceState = sourceTransducer.getCurrentState();
        State destinationState = destinationTransducer.getCurrentState();
        
        try {
            if (specification instanceof CopyTransformation) {
                /*
                 * SOURCE MATCHED FIRST
                 * SRC: eval -> match (sync) -> meminSubtree   -> gen     -> gen (sync) -> gen    -> gen
                 * DEST: eval -> eval -> eval -> eval -> match -> findPos -> matchPos -> memout -> gen
                 * -> gen
                 */
                // (match, eval) -> (memin, eval)
                // (paused, not paused)
                if ((sourceState instanceof Match) && (destinationState instanceof Eval)) {
                    // generator.copyCurrentEvent(parser);
                    sourceTransducer.setState(new MeminSubtree(sourceTransducer));
                    // eval state sposobi, ze sa generuje aj pocas matchu
                    // mozno sa snazit namiesto OR dat AND (ze sa generuje, ked oba generuju)                    
                    sourceTransducer.setPaused(false);
                    
                    // (gen, matchPos) -> (gen, memout)
                } else if ((sourceState instanceof Gen) && (destinationState instanceof MatchPos)) {
                    if (((CopyTransformation) specification).getKey() != null) {
                        generator.writeFieldName(((CopyTransformation) specification).getKey());
                    }
                    destinationTransducer.setState(new Memout(destinationTransducer));
                    destinationTransducer.setPaused(true);

                /* DESTINATION MATCHED FIRST */

                // (Eval, MatchPos) -> (Eval, MeminDel)
                } else if ((sourceState instanceof Eval) && (destinationState instanceof MatchPos)) {

                    if (((CopyTransformation) specification).getKey() != null) {
                        String key = ((CopyTransformation) specification).getKey();
                        generator.writeFieldName(key);
                    }

                    destinationTransducer.setState(new MeminSkip(destinationTransducer));
                    // transducer.addToMemory();
                    destinationTransducer.setPaused(true);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    // (Eval, MeminDel) -> (Eval, MeminDel)
                    // [fragment is in memory] -> (Match, Gen)
                } else if ((sourceState instanceof Eval) && (destinationState instanceof MeminSkip)) {

                    if (sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        sourceTransducer.setState(new Match(sourceTransducer));
                        destinationTransducer.setState(new Gen(destinationTransducer));

                        sourceTransducer.setPaused(true);
                        destinationTransducer.setPaused(false);
                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                        return;
                    }

                    sourceState.process(parser);
                    destinationState.process(parser);

                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();

                    // (Match, Memindel)[fragment is in memory] -> (Memin, Gen)
                } else if ((sourceState instanceof Match) && (destinationState instanceof MeminSkip)) {
                    sourceTransducer.setState(new MeminSubtree(sourceTransducer));
                    destinationTransducer.setState(new Gen(destinationTransducer));

                    sourceTransducer.setPaused(false);
                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                    // (Match, Gen) -> (Memin, Gen)
                } else if ((sourceState instanceof Match) && (destinationState instanceof Gen)) {
                    sourceTransducer.setState(new MeminSubtree(sourceTransducer));
                    sourceTransducer.setPaused(false);

                    if (((CopyTransformation) specification).getKey() == null) {
                        transducer.addToMemory();
                    }

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                    // (Memin, Gen)
                } else if ((sourceState instanceof MeminSubtree) && (destinationState instanceof Gen)) {

                    // Memin/Memout + Gen -> generuje source Transducer
                    sourceTransducer.setGenerator(generator);
                    destinationTransducer.setGenerator(generator);
                    sourceTransducer.setIsGenerating(true);
                    destinationTransducer.setIsGenerating(false);

                    destinationState.process(parser);

                    if (!event.isStructStart() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        transducer.addToMemory();
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        generator.copyCurrentEvent(parser);
                        sourceTransducer.setPaused(false);

                        generator.flush();

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                        return;
                    }

                    Integer lastValue = sourceTransducer.getPaStack().pop();
                    if (event.isStructEnd() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        transducer.addToMemory();
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        generator.copyCurrentEvent(parser);
                        sourceTransducer.setPaused(false);

                        generator.flush();

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                        return;
                    }
                    sourceTransducer.getPaStack().push(lastValue);

                    sourceState.process(parser);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();

                } else {

                    sourceTransducer.setIsGenerating(false);
                    destinationTransducer.setIsGenerating(false);

                    sourceState.process(parser);
                    destinationState.process(parser);
                    sourceState = sourceTransducer.getCurrentState();
                    destinationState = destinationTransducer.getCurrentState();

                    // dest - noGen musi byt false
                    if (sourceState instanceof Gen && !destinationTransducer.noGen()) {
                        generator.copyCurrentEvent(parser);
                        // src
                    } else if (destinationState instanceof Gen && !sourceTransducer.noGen()) {
                        generator.copyCurrentEvent(parser);
                        // dest
                    } else if (sourceState instanceof Eval && !destinationTransducer.noGen()) {
                        generator.copyCurrentEvent(parser);
                        // src
                    } else if (destinationState instanceof Eval && !sourceTransducer.noGen()) {
                        generator.copyCurrentEvent(parser);
                    }

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                }
            } else if (specification instanceof MoveTransformation) {
                if ((sourceState instanceof Match) && (destinationState instanceof Eval)) {
                    sourceTransducer.setState(new MeminSkip(sourceTransducer));
                    sourceTransducer.setPaused(false);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } else if ((sourceState instanceof MeminSkip) && (destinationState instanceof Eval)) {
                    try {
                        destinationState.process(parser);

                        if (!event.isStructStart() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                            transducer.addToMemory();
                            sourceTransducer.setState(new Gen(sourceTransducer));

                            return;
                        }

                        Integer lastValue = sourceTransducer.getPaStack().pop();
                        if (event.isStructEnd() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                            transducer.addToMemory();
                            sourceTransducer.setState(new Gen(sourceTransducer));
                            return;
                        }
                        sourceTransducer.getPaStack().push(lastValue);
                        sourceState.process(parser);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else if ((sourceState instanceof Gen) && (destinationState instanceof Match)) {

                    if (((MoveTransformation) specification).getKey() != null) {
                        destinationTransducer.setState(new FindPos(destinationTransducer));
                    } else {
                        destinationTransducer.setState(new FindPos(destinationTransducer));
                    }
                    generator.copyCurrentEvent(parser);
                    destinationTransducer.setPaused(false);

                } else if ((sourceState instanceof Gen) && (destinationState instanceof MatchPos)) {

                    if (((MoveTransformation) specification).getKey() != null) {
                        generator.writeFieldName(((MoveTransformation) specification).getKey());
                    }

                    destinationTransducer.setState(new Memout(destinationTransducer));
                    destinationTransducer.setPaused(true);

                } else if ((sourceState instanceof Eval) && (destinationState instanceof Match)) {

                    if (((MoveTransformation) specification).getKey() != null) {
                        destinationTransducer.setState(new FindPos(destinationTransducer));
                    } else {
                        destinationTransducer.setState(new FindPos(destinationTransducer));
                    }
                    generator.copyCurrentEvent(parser);

                    destinationTransducer.setPaused(false);

                } else if ((sourceState instanceof Eval) && (destinationState instanceof MatchPos)) {
                    destinationTransducer.setState(new MeminSkip(destinationTransducer));
                    // transducer.addToMemory();
                    destinationTransducer.setPaused(true);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } else if ((sourceState instanceof Eval) && (destinationState instanceof MeminSkip)) {

                    sourceState.process(parser);

                    if (sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        sourceTransducer.setState(new Match(sourceTransducer));
                        destinationTransducer.setState(new Gen(destinationTransducer));

                        sourceTransducer.setPaused(true);
                        destinationTransducer.setPaused(false);
                        return;
                    }

                    destinationState.process(parser);
                    destinationTransducer.setPaused(false);

                } else if ((sourceState instanceof Match) && (destinationState instanceof MeminSkip)) {

                    if (((MoveTransformation) specification).getKey() == null) {
                        generator.copyCurrentEvent(parser);
                    } else {
                        generator.writeFieldName(((MoveTransformation) specification).getKey());
                    }

                    sourceTransducer.setState(new Eval(sourceTransducer));
                    destinationTransducer.setState(new Gen(destinationTransducer));

                    sourceTransducer.setPaused(false);

                } else if ((sourceState instanceof Match) && (destinationState instanceof Gen)) {

                    if (((MoveTransformation) specification).getKey() != null) {
                        generator.writeFieldName(((MoveTransformation) specification).getKey());
                    }

                    sourceTransducer.setState(new Eval(sourceTransducer));
                    destinationTransducer.setState(new Gen(destinationTransducer));

                    sourceTransducer.setPaused(false);

                } else if ((sourceState instanceof Eval) && (destinationState instanceof Gen)) {

                    // Eval/Memout + Gen -> generuje source Transducer
                    sourceTransducer.setGenerator(generator);
                    destinationTransducer.setGenerator(generator);
                    sourceTransducer.setIsGenerating(true);
                    destinationTransducer.setIsGenerating(false);

                    if (!event.isStructStart() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        sourceTransducer.setPaused(false);
                        generator.copyCurrentEvent(parser);
                        generator.flush();
                        return;
                    }

                    Integer lastValue = sourceTransducer.getPaStack().pop();
                    if (event.isStructEnd() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        sourceTransducer.setPaused(false);

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                        generator.copyCurrentEvent(parser);
                        generator.flush();
                        return;
                    }
                    sourceTransducer.getPaStack().push(lastValue);

                    Integer lastValueTmp = sourceTransducer.getPaStack().peek();
                    sourceTransducer.getPaStack().push(lastValueTmp);
                    sourceState.process(parser);
                    Integer returnLastValue = sourceTransducer.getPaStack().pop();
                    sourceTransducer.getPaStack().pop();
                    sourceTransducer.getPaStack().push(returnLastValue);
                    destinationState.process(parser);

                } else if ((sourceState instanceof MeminSubtree) && (destinationState instanceof Gen)) {

                    // Memin/Memout + Gen -> generuje source Transducer
                    sourceTransducer.setGenerator(generator);
                    destinationTransducer.setGenerator(generator);
                    sourceTransducer.setIsGenerating(true);
                    destinationTransducer.setIsGenerating(false);
                    destinationState.process(parser);

                    if (!event.isStructStart() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        transducer.addToMemory();
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        generator.copyCurrentEvent(parser);
                        sourceTransducer.setPaused(false);

                        generator.flush();

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                        return;
                    }

                    Integer lastValue = sourceTransducer.getPaStack().pop();
                    if (event.isStructEnd() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        transducer.addToMemory();
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        generator.copyCurrentEvent(parser);
                        sourceTransducer.setPaused(false);

                        generator.flush();

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                        return;
                    }
                    sourceTransducer.getPaStack().push(lastValue);

                    sourceState.process(parser);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();

                } else {

                    sourceTransducer.setIsGenerating(false);
                    destinationTransducer.setIsGenerating(false);

                    sourceState.process(parser);
                    destinationState.process(parser);
                    sourceState = sourceTransducer.getCurrentState();
                    destinationState = destinationTransducer.getCurrentState();

                    // dest - noGen musi byt false
                    if (sourceState instanceof Gen && !destinationTransducer.noGen()) {
                        generator.copyCurrentEvent(parser);
                        // src
                    } else if (destinationState instanceof Gen && !sourceTransducer.noGen()) {
                        generator.copyCurrentEvent(parser);
                        // dest
                    } else if (sourceState instanceof Eval && !destinationTransducer.noGen()) {
                        generator.copyCurrentEvent(parser);
                        // src
                    } else if (destinationState instanceof Eval && !sourceTransducer.noGen()) {
                        generator.copyCurrentEvent(parser);
                    }

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
