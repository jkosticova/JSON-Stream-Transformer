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
import Prototype.Writer.JsonWriter;
import Prototype.Writer.RawUtf8Writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.io.OutputStream;

public class Sync implements State {
    private final BufferTransducer transducer;
    private final SourceTransducer sourceTransducer;
    private final DestinationTransducer destinationTransducer;
    private final JsonWriter writer;
    private final TransformationFormat specification;    
    private final PathAutomaton sourcePa;    
    private final JsonWriter nullWriter;

    public Sync(BufferTransducer transducer) {
        this.transducer = transducer;
        this.sourceTransducer = transducer.getSourceTransducer();
        this.destinationTransducer = transducer.getDestinationTransducer();
        this.writer = transducer.getWriter();
        this.specification = transducer.getSpecification();        
        this.sourcePa = sourceTransducer.getPa();        
        RawUtf8Writer rawWriter = new RawUtf8Writer(OutputStream.nullOutputStream());
        this.nullWriter = new JsonWriter(rawWriter);    
    }

    @Override
    public void process(JsonParser parser) {

        JsonToken event = parser.currentToken();
        sourceTransducer.setWriter(nullWriter);
        destinationTransducer.setWriter(nullWriter);

        
        sourceTransducer.setIsGenerating(false);                    
        destinationTransducer.setIsGenerating(false);
        
        sourceTransducer.setNoGen(false);                    
        destinationTransducer.setNoGen(false);

        State sourceState = sourceTransducer.getCurrentState();
        State destinationState = destinationTransducer.getCurrentState();

        if (specification instanceof CopyTransformation) {
            // source match, dest eval -> source memin
            if ((sourceState instanceof Match) && (destinationState instanceof Eval)) {
                try {
                    writer.writeCurrentEvent(parser);

                    sourceTransducer.setState(new Memin(sourceTransducer));
                    sourceTransducer.setPaused(false);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            // source memin, dest eval       
            } else if ((sourceState instanceof Memin) && (destinationState instanceof Eval)) {
                try {                    
                    // Memin + Eval/Gen -> generuje source Transducer
                    sourceTransducer.setWriter(writer);
                    destinationTransducer.setWriter(writer);
                    sourceTransducer.setIsGenerating(true);                    
                    destinationTransducer.setIsGenerating(false);
                    
                    destinationState.process(parser);
                    
                    // koniec memin
                    if (!event.isStructStart() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {                        
                        transducer.addToMemory(); 
                        sourceTransducer.setState(new Gen(sourceTransducer));
                        writer.writeCurrentEvent(parser);

                        return;
                    }

                    Integer lastValue = sourceTransducer.getPaStack().pop();
                    if (event.isStructEnd() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        transducer.addToMemory();
                        sourceTransducer.setState(new Gen(sourceTransducer));
                        writer.writeCurrentEvent(parser);

                        return;
                    }
                    sourceTransducer.getPaStack().push(lastValue);

                    sourceState.process(parser);
                    

                    //Transducer preferredTransducerState = getPreferredState(sourceTransducer.getCurrentState(), destinationTransducer.getCurrentState());

                    //assert preferredTransducerState != null;
                    //TokenBuffer tokenBuffer = (TokenBuffer) destinationTransducer.getwriter();

                    //tokenBuffer.serialize(writer);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    writer.flush();
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
                    writer.writeCurrentEvent(parser);

                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Gen) && (destinationState instanceof Match_i)) {
                try {
                    if (((CopyTransformation) specification).getKey() != null) {
                        writer.writeFieldName(((CopyTransformation) specification).getKey());
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
                    writer.writeCurrentEvent(parser);

                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Eval) && (destinationState instanceof Match_i)) {
                try {
                    if (((CopyTransformation) specification).getKey() != null) {
                        String key = ((CopyTransformation) specification).getKey();
                        writer.writeFieldName(key);                       
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

                    sourceState.process(parser);
                    destinationState.process(parser);

                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    writer.flush();
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
                    // Memin/Memout + Gen -> generuje source Transducer
                    sourceTransducer.setWriter(writer);
                    destinationTransducer.setWriter(writer);
                    sourceTransducer.setIsGenerating(true);                    
                    destinationTransducer.setIsGenerating(false);
                    
                    destinationState.process(parser);

                    if (!event.isStructStart() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        transducer.addToMemory();
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        writer.writeCurrentEvent(parser);
                        sourceTransducer.setPaused(false);

                        writer.flush();

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                        return;
                    }

                    Integer lastValue = sourceTransducer.getPaStack().pop();
                    if (event.isStructEnd() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        transducer.addToMemory();
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        writer.writeCurrentEvent(parser);
                        sourceTransducer.setPaused(false);

                        writer.flush();

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                        return;
                    }
                    sourceTransducer.getPaStack().push(lastValue);

                    sourceState.process(parser);
                    

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    
                    sourceTransducer.setIsGenerating(false);
                    destinationTransducer.setIsGenerating(false);
                                        
                    sourceState.process(parser);
                    destinationState.process(parser);
                    sourceState = sourceTransducer.getCurrentState();
                    destinationState = destinationTransducer.getCurrentState();

                    // dest - noGen musi byt false
                    if (sourceState instanceof Gen && !destinationTransducer.noGen()) {
                        writer.writeCurrentEvent(parser);
                    // src
                    } else if (destinationState instanceof Gen && !sourceTransducer.noGen()) {
                        writer.writeCurrentEvent(parser);
                    // dest
                    } else if (sourceState instanceof Eval && !destinationTransducer.noGen()) {                    
                        writer.writeCurrentEvent(parser);
                    // src
                    } else if (destinationState instanceof Eval && !sourceTransducer.noGen()) {
                        writer.writeCurrentEvent(parser);
                    }
                    
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    writer.flush();
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

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    writer.flush();
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
                    writer.writeCurrentEvent(parser);

                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Gen) && (destinationState instanceof Match_i)) {
                try {
                    if (((MoveTransformation) specification).getKey() != null) {
                        writer.writeFieldName(((MoveTransformation) specification).getKey());
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
                    writer.writeCurrentEvent(parser);

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
                    sourceState.process(parser);

                    if (sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        sourceTransducer.setState(new Match(sourceTransducer));
                        destinationTransducer.setState(new Gen(destinationTransducer));

                        sourceTransducer.setPaused(true);
                        destinationTransducer.setPaused(false);
                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());
                        return;
                    }

                    destinationState.process(parser);
                    destinationTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Match) && (destinationState instanceof MeminDel)) {
                try {
                    if (((MoveTransformation) specification).getKey() == null) {
                        writer.writeCurrentEvent(parser);
                    } else {
                        writer.writeFieldName(((MoveTransformation) specification).getKey());
                    }

                    sourceTransducer.setState(new Eval(sourceTransducer));
                    destinationTransducer.setState(new Gen(destinationTransducer));

                    sourceTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Match) && (destinationState instanceof Gen)) {
                try {
                    if (((MoveTransformation) specification).getKey() != null) {
                        writer.writeFieldName(((MoveTransformation) specification).getKey());
                    }

                    sourceTransducer.setState(new Eval(sourceTransducer));
                    destinationTransducer.setState(new Gen(destinationTransducer));

                    sourceTransducer.setPaused(false);
                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Eval) && (destinationState instanceof Gen)) {
                try {
                    // Eval/Memout + Gen -> generuje source Transducer
                    sourceTransducer.setWriter(writer);
                    destinationTransducer.setWriter(writer);
                    sourceTransducer.setIsGenerating(true);                    
                    destinationTransducer.setIsGenerating(false);
                    
                    if (!event.isStructStart() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        sourceTransducer.setPaused(false);

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                        writer.writeCurrentEvent(parser);
                        writer.flush();
                        return;
                    }

                    Integer lastValue = sourceTransducer.getPaStack().pop();
                    if (event.isStructEnd() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        sourceTransducer.setPaused(false);

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                        writer.writeCurrentEvent(parser);
                        writer.flush();
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

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((sourceState instanceof Memin) && (destinationState instanceof Gen)) {
                try {
                    // Memin/Memout + Gen -> generuje source Transducer
                    sourceTransducer.setWriter(writer);
                    destinationTransducer.setWriter(writer);
                    sourceTransducer.setIsGenerating(true);                    
                    destinationTransducer.setIsGenerating(false);
                    destinationState.process(parser);

                    if (!event.isStructStart() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        transducer.addToMemory();
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        writer.writeCurrentEvent(parser);
                        sourceTransducer.setPaused(false);

                        writer.flush();

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                        return;
                    }

                    Integer lastValue = sourceTransducer.getPaStack().pop();
                    if (event.isStructEnd() && sourcePa.isFinal(sourceTransducer.getPaStack().peek())) {
                        transducer.addToMemory();
                        sourceTransducer.setState(new Memout(sourceTransducer));
                        writer.writeCurrentEvent(parser);
                        sourceTransducer.setPaused(false);

                        writer.flush();

                        transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                        return;
                    }
                    sourceTransducer.getPaStack().push(lastValue);

                    sourceState.process(parser);                   

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    sourceTransducer.setIsGenerating(false);
                    destinationTransducer.setIsGenerating(false);
                                        
                    sourceState.process(parser);
                    destinationState.process(parser);
                    sourceState = sourceTransducer.getCurrentState();
                    destinationState = destinationTransducer.getCurrentState();

                    // dest - noGen musi byt false
                    if (sourceState instanceof Gen && !destinationTransducer.noGen()) {
                        writer.writeCurrentEvent(parser);
                    // src
                    } else if (destinationState instanceof Gen && !sourceTransducer.noGen()) {
                        writer.writeCurrentEvent(parser);
                    // dest
                    } else if (sourceState instanceof Eval && !destinationTransducer.noGen()) {                    
                        writer.writeCurrentEvent(parser);
                    // src
                    } else if (destinationState instanceof Eval && !sourceTransducer.noGen()) {
                        writer.writeCurrentEvent(parser);
                    }
                    


                    //Transducer preferredTransducerState = getPreferredState(sourceTransducer.getCurrentState(), destinationTransducer.getCurrentState());

                    
                    //TokenBuffer tokenBuffer = (TokenBuffer) preferredTransducerState.getwriter();

                    //tokenBuffer.serialize(writer);

                    transducer.setPaused(sourceTransducer.getPaused() || destinationTransducer.getPaused());

                    writer.flush();

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
