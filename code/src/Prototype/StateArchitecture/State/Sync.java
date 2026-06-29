package Prototype.StateArchitecture.State;

import Prototype.PathAutomaton.PathAutomaton;
import Prototype.PathAutomaton.SimplePathAutomaton;
import Prototype.SpecificationParser.CopyTransformation;
import Prototype.SpecificationParser.MoveTransformation;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.Transducer.BufferTransducer;
import Prototype.StateArchitecture.Transducer.DestinationTransducer;
import Prototype.StateArchitecture.Transducer.SourceTransducer;
import Prototype.StateArchitecture.Transducer.Transducer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;

public class Sync implements State {
    private final BufferTransducer transducer;
    private final SourceTransducer sourceTransducer;
    private final DestinationTransducer destinationTransducer;
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
    public void process(JsonToken event, JsonParser parser) {

        TokenBuffer sourceOutputStream = new TokenBuffer((ObjectCodec) null, false);
        TokenBuffer destinationOutputStream = new TokenBuffer((ObjectCodec) null, false);

        sourceTransducer.setGenerator(sourceOutputStream);
        destinationTransducer.setGenerator(destinationOutputStream);


        State sourceState = sourceTransducer.getCurrentState();
        State destinationState = destinationTransducer.getCurrentState();

        if (specification instanceof CopyTransformation) {
            // source match, dest eval -> source memin
            if ((sourceState instanceof Match) && (destinationState instanceof Eval)) {
                try {
                    generator.copyCurrentEvent(parser);

                    sourceTransducer.setState(new Memin(sourceTransducer));
                    sourceTransducer.setPaused(false);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            // source memin, dest eval       
            } else if ((sourceState instanceof Memin) && (destinationState instanceof Eval)) {
                try {
                    destinationState.process(event, parser);

                    // koniec memin
                    if (!event.isStructStart() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {                        
                        transducer.addToMemory(); 
                        sourceTransducer.setState(new Gen(sourceTransducer));
                        generator.copyCurrentEvent(parser);

                        return;
                    }

                    Integer lastValue = sourceTransducer.getPaStack().pop();
                    if (event.isStructEnd() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        transducer.addToMemory();
                        sourceTransducer.setState(new Gen(sourceTransducer));
                        generator.copyCurrentEvent(parser);

                        return;
                    }
                    sourceTransducer.getPaStack().push(lastValue);

                    sourceState.process(event, parser);

                    Transducer preferredTransducerState = getPreferredState(sourceTransducer.getCurrentState(), destinationTransducer.getCurrentState());

                    assert preferredTransducerState != null;
                    TokenBuffer tokenBuffer = (TokenBuffer) preferredTransducerState.getGenerator();

                    tokenBuffer.serialize(generator);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            // source uz je v pamati a generuje, dest match
            } else if ((sourceState instanceof Gen) && (destinationState instanceof Match)) {
                try {
                    if (((CopyTransformation) specification).getKey() != null)
                        destinationTransducer.setState(new Find_i(destinationTransducer));
                    else {
                        destinationTransducer.setState(new Find_i(destinationTransducer));
                    }
                    generator.copyCurrentEvent(parser);

                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Gen) && (destinationState instanceof Match_i)) {
                try {
                    if (((CopyTransformation) specification).getKey() != null) {
                        generator.writeFieldName(((CopyTransformation) specification).getKey());
                    }

                    destinationTransducer.setState(new Memout(destinationTransducer));
                    destinationTransducer.setPaused(true);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            // source is evaluated, set is already matched
            } else if ((sourceState instanceof Eval) && (destinationState instanceof Match)) {
                try {
                    if (((CopyTransformation) specification).getKey() != null) {
                        destinationTransducer.setState(new Find_i(destinationTransducer));
                    } else {
                        destinationTransducer.setState(new Find_i(destinationTransducer));
                    }
                    generator.copyCurrentEvent(parser);

                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Eval) && (destinationState instanceof Match_i)) {
                try {
                    if (((CopyTransformation) specification).getKey() != null) {
                        String key = ((CopyTransformation) specification).getKey();
                        generator.writeFieldName(key);                       
                    }

                    destinationTransducer.setState(new MeminDel(destinationTransducer));
                    //transducer.addToMemory();
                    destinationTransducer.setPaused(true);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Eval) && (destinationState instanceof MeminDel)) {
                try {
                    if (sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        sourceTransducer.setState(new Match(sourceTransducer));
                        destinationTransducer.setState(new Gen(destinationTransducer));

                        sourceTransducer.setPaused(true);
                        destinationTransducer.setPaused(false);
                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                        return;
                    }

                    sourceState.process(event, parser);
                    destinationState.process(event, parser);

                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Match) && (destinationState instanceof MeminDel)) {
                sourceTransducer.setState(new Memin(sourceTransducer));
                destinationTransducer.setState(new Gen(destinationTransducer));

                sourceTransducer.setPaused(false);
                destinationTransducer.setPaused(false);
                transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
            } else if ((sourceState instanceof Match) && (destinationState instanceof Gen)) {
                sourceTransducer.setState(new Memin(sourceTransducer));
                sourceTransducer.setPaused(false);

                if (((CopyTransformation) specification).getKey() == null) {
                    transducer.addToMemory();
                }

                transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
            } else if ((sourceState instanceof Memin) && (destinationState instanceof Gen)) {
                try {
                    destinationState.process(event, parser);

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

                    sourceState.process(event, parser);

                    Transducer preferredTransducerState = getPreferredState(sourceTransducer.getCurrentState(), destinationTransducer.getCurrentState());

                    assert preferredTransducerState != null;
                    TokenBuffer tokenBuffer = (TokenBuffer) preferredTransducerState.getGenerator();

                    tokenBuffer.serialize(generator);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    sourceState.process(event, parser);
                    destinationState.process(event, parser);

                    Transducer preferredTransducerState = getPreferredState(sourceTransducer.getCurrentState(), destinationTransducer.getCurrentState());

                    assert preferredTransducerState != null;
                    TokenBuffer tokenBuffer = (TokenBuffer) preferredTransducerState.getGenerator();

                    tokenBuffer.serialize(generator);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (specification instanceof MoveTransformation) {
            if ((sourceState instanceof Match) && (destinationState instanceof Eval)) {
                sourceTransducer.setState(new MeminDel(sourceTransducer));
                sourceTransducer.setPaused(false);

                transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
            } else if ((sourceState instanceof MeminDel) && (destinationState instanceof Eval)) {
                try {
                    destinationState.process(event, parser);

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

                    sourceState.process(event, parser);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Gen) && (destinationState instanceof Match)) {
                try {
                    if (((MoveTransformation) specification).getKey() != null) {
                        destinationTransducer.setState(new Find_i(destinationTransducer));
                    } else {
                        destinationTransducer.setState(new Find_i(destinationTransducer));
                    }
                    generator.copyCurrentEvent(parser);

                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Gen) && (destinationState instanceof Match_i)) {
                try {
                    if (((MoveTransformation) specification).getKey() != null) {
                        generator.writeFieldName(((MoveTransformation) specification).getKey());
                    }

                    destinationTransducer.setState(new Memout(destinationTransducer));
                    destinationTransducer.setPaused(true);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Eval) && (destinationState instanceof Match)) {
                try {
                    if (((MoveTransformation) specification).getKey() != null) {
                        destinationTransducer.setState(new Find_i(destinationTransducer));
                    } else {
                        destinationTransducer.setState(new Find_i(destinationTransducer));
                    }
                    generator.copyCurrentEvent(parser);

                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Eval) && (destinationState instanceof Match_i)) {
                destinationTransducer.setState(new MeminDel(destinationTransducer));
                //transducer.addToMemory();
                destinationTransducer.setPaused(true);

                transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
            } else if ((sourceState instanceof Eval) && (destinationState instanceof MeminDel)) {
                try {
                    sourceState.process(event, parser);

                    if (sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        sourceTransducer.setState(new Match(sourceTransducer));
                        destinationTransducer.setState(new Gen(destinationTransducer));

                        sourceTransducer.setPaused(true);
                        destinationTransducer.setPaused(false);
                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                        return;
                    }

                    destinationState.process(event, parser);
                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Match) && (destinationState instanceof MeminDel)) {
                try {
                    if (((MoveTransformation) specification).getKey() == null) {
                        generator.copyCurrentEvent(parser);
                    } else {
                        generator.writeFieldName(((MoveTransformation) specification).getKey());
                    }

                    sourceTransducer.setState(new Eval(sourceTransducer));
                    destinationTransducer.setState(new Gen(destinationTransducer));

                    sourceTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Match) && (destinationState instanceof Gen)) {
                try {
                    if (((MoveTransformation) specification).getKey() != null) {
                        generator.writeFieldName(((MoveTransformation) specification).getKey());
                    }

                    sourceTransducer.setState(new Eval(sourceTransducer));
                    destinationTransducer.setState(new Gen(destinationTransducer));

                    sourceTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Eval) && (destinationState instanceof Gen)) {
                try {
                    if (!event.isStructStart() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        sourceTransducer.setPaused(false);

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

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
                    sourceState.process(event, parser);
                    Integer returnLastValue = sourceTransducer.getPaStack().pop();
                    sourceTransducer.getPaStack().pop();
                    sourceTransducer.getPaStack().push(returnLastValue);
                    destinationState.process(event, parser);

                    Transducer preferredTransducerState = getPreferredState(sourceTransducer.getCurrentState(), destinationTransducer.getCurrentState());

                    assert preferredTransducerState != null;
                    TokenBuffer tokenBuffer = (TokenBuffer) preferredTransducerState.getGenerator();

                    tokenBuffer.serialize(generator);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Memin) && (destinationState instanceof Gen)) {
                try {
                    destinationState.process(event, parser);

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

                    sourceState.process(event, parser);

                    Transducer preferredTransducerState = getPreferredState(sourceTransducer.getCurrentState(), destinationTransducer.getCurrentState());

                    assert preferredTransducerState != null;
                    TokenBuffer tokenBuffer = (TokenBuffer) preferredTransducerState.getGenerator();

                    tokenBuffer.serialize(generator);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    sourceState.process(event, parser);
                    destinationState.process(event, parser);

                    Transducer preferredTransducerState = getPreferredState(sourceTransducer.getCurrentState(), destinationTransducer.getCurrentState());

                    assert preferredTransducerState != null;
                    TokenBuffer tokenBuffer = (TokenBuffer) preferredTransducerState.getGenerator();

                    tokenBuffer.serialize(generator);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    generator.flush();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Transducer getPreferredState(State sourceState, State destinationState) {
        if (sourceState instanceof Gen) {
            return destinationTransducer;
        } else if (destinationState instanceof Gen) {
            return sourceTransducer;
        } else if (sourceState instanceof Eval) {
            return destinationTransducer;
        } else if (destinationState instanceof Eval) {
            return sourceTransducer;
        }

        return null;
    }
}
