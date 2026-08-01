package Prototype.StateArchitecture.Transducer;

import Prototype.Mapper.SpecificationMapper;
import Prototype.PathAutomaton.PathAutomaton;
import Prototype.SpecificationParser.TransformationFormat;
import Prototype.StateArchitecture.State.Gen;
import Prototype.StateArchitecture.State.State;
import Prototype.Writer.JsonWriter;
import Prototype.Writer.RawUtf8Writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;

public class IdentityTransducer implements Transducer {
    private State currentState;
    JsonWriter writer;
    JsonParser parser;
    TransformationFormat specification;

    public IdentityTransducer(SpecificationMapper mapper, InputStream inputStream, OutputStream outputStream) {
        specification = mapper.getTransformationFormat();
        JsonFactory factory = new JsonFactory();
        try {
            parser = factory.createParser(inputStream);
            RawUtf8Writer rawWriter = new RawUtf8Writer(outputStream);
            this.writer = new JsonWriter(rawWriter);            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        currentState = new Gen(this);
    }

    @Override
    public void setPaused(boolean paused) {
    }

    @Override
    public boolean isGenerating() {
        return true;
    }
    
    @Override
    public void setIsGenerating(boolean isGenerating) {
            // do nothing
    }

    @Override
    public boolean noGen() {
        return true;
    }
    
    @Override
    public void setNoGen(boolean noGen) {
            // do nothing
    }

    @Override
    public PathAutomaton getPa() {
        return null;
    }

    @Override
    public JsonWriter getWriter() {
        return this.writer;
    }

    @Override
    public Stack<Integer> getPaStack() {
        return null;
    }

    @Override
    public Stack<Integer> getIndexStack() {
        return null;
    }

    @Override
    public TransformationFormat getSpecification() {
        return this.specification;
    }

    public boolean process() {
        try {
            JsonToken event;

            while (!parser.isClosed()) {
                event = parser.nextToken();

                if (event == null) break;
                currentState.process(parser);
            }

            parser.close();
            writer.flush();
            //generator.close();
        } catch (Exception e) {
            System.out.println("Issue while processing IdentityTransducer: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public State getCurrentState() {
        return this.currentState;
    }

    @Override
    public void getFromMemory() {
    }

    @Override
    public void addToMemory() {
    }

    @Override
    public void setState(State state) {
        this.currentState = state;
    }

    @Override
    public State getEvalState() {
        return null;
    }

    @Override
    public State getMatchState() {
        return null;
    }
    
    @Override
    public State getGenState() {
        return null;
    }

    @Override
    public State getDelState() {
        return null;
    }

    @Override
    public State getFind_iState() {
        return null;
    }

    @Override
    public State getMatch_iState() {
        return null;
    }

    @Override
    public State getMeminState() {
        return null;
    }

    @Override
    public State getMeminDelState() {
        return null;
    }

    @Override
    public State getMemoutState() {
        return null;
    }
}
