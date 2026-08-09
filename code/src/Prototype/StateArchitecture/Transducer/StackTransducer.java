package prototype.stateArchitecture.transducer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import prototype.mapper.SpecificationMapper;
import prototype.pathAutomaton.*;
import prototype.stateArchitecture.state.*;
import prototype.stateArchitecture.state.eval.EvalPath;
import prototype.stateArchitecture.state.eval.FindPos;
import prototype.stateArchitecture.state.match.MatchPath;
import prototype.stateArchitecture.state.match.MatchPos;
import prototype.stateArchitecture.state.subtreeTraversal.SubtreeGen;
import prototype.stateArchitecture.state.subtreeTraversal.SubtreeSkip;

import java.io.IOException;
import java.util.Arrays;

public class StackTransducer extends Transducer {
    public static final int INITIAL_PA_STATE = 0;
    public static final int OBJECT_ARR_INDEX = -1;

    // primitive stacks
    private int[] paStack = new int[32];
    private int paStackSize = 0;
    private int[] indexStack = new int[32];
    private int indexStackSize = 0;
    


    // reusable states
    protected EvalPath evalPathState;
    protected MatchPath matchPathState;    
    protected SubtreeSkip subtreeSkipState;
    protected SubtreeGen subtreeGenState;
    protected FindPos findPosState;
    protected MatchPos matchPosState;
    
    private PathAutomaton pa;
    String path;


    /* constructor for a single STACK transformation */
    public StackTransducer(SpecificationMapper mapper, JsonParser parser, JsonGenerator generator) {
        
        super(mapper, parser, generator);
            
        // Created separately (rather than in one try) so that if the
        // generator fails to open after the parser succeeded, we still
        // close the parser instead of leaking the open InputStream.
        /*
        JsonFactory factory = new JsonFactory();
        try {
            parser = factory.createParser(inputStream);
        } catch (IOException e) {
            throw new TransducerException("Could not open input stream for parsing", e);
        }

        try {
            generator = factory.createGenerator(outputStream).useDefaultPrettyPrinter();
        } catch (IOException e) {
            closeQuietly(parser);
            throw new TransducerException("Could not open output stream for generation", e);
        }*/

        // stacks        
        this.path = specification.getPath();

        
        
        this.pa = new SimplePathAutomaton(path);        
        pushPaStack(INITIAL_PA_STATE);
                
        initStates();
        currentState = evalPathState;
    }

    
    
    protected void initStates() {
        super.initStates();
        evalPathState = new EvalPath(this);
        matchPathState = new MatchPath(this);
        subtreeSkipState = new SubtreeSkip(this);
        subtreeGenState = new SubtreeGen(this);
        findPosState = new FindPos(this);
        matchPosState = new MatchPos(this);                
    }

    
    // used only for stack transformations
    // (StackTransducers created with the COPY/MOVE constructor above are
    // driven state-by-state via Sync/BufferTransducer instead - this
    // override is only ever invoked on a transducer that owns its own
    // parser/generator, so closing both streams below is safe.)
    @Override
    public boolean process() {
        boolean success = true;
        try {
            JsonToken event = null;

            // while the input is being read
            while (!parser.isClosed()) {
                if (!paused) {
                    event = parser.nextToken();
                }
                // EOF && empty stack
                if (event == null || paStackSize == 0) {
                    break;
                }
                this.getCurrentState().process(parser);
                // generovanie
                if (this.generating) {
                    generator.copyCurrentEvent(parser);
                }
                generator.flush();
            }
            generator.flush();
        } catch (IOException e) {
            System.err.println("Issue while processing StackTransducer: " + e);
            success = false;
        } finally {
            // Always attempt to release both streams, whether processing
            // succeeded or failed, so a failure here doesn't leak file
            // handles on top of the original problem.
            IoHandler.closeQuietly(parser);
            IoHandler.closeQuietly(generator);
        }
        return success;
    }

    private void ensurePaStackCapacity() {
        if (paStackSize < paStack.length) {
            return;
        }
        paStack = Arrays.copyOf(paStack, paStack.length * 2);
    }

    private void ensureIndexStackCapacity() {
        if (indexStackSize < indexStack.length) {
            return;
        }

        indexStack = Arrays.copyOf(indexStack, indexStack.length * 2);
    }
    
    private void pushPaStack(int val) {
         ensurePaStackCapacity();
         paStack[paStackSize++] = val;
    }
    public void pushIndexStack(int index) {
    ensureIndexStackCapacity();
    indexStack[indexStackSize++] = index;
}

public int popIndexStack() {
    if (indexStackSize == 0) {
        throw new IllegalStateException("Array index stack is empty");
    }

    return indexStack[--indexStackSize];
}

public int popPaStack() {
    if (paStackSize == 0) {
        throw new IllegalStateException("Array index stack is empty");
    }

    return paStack[--paStackSize];
}


public int indexStackPeek() {
    if (indexStackSize == 0) {
        throw new IllegalStateException("Array index stack is empty");
    }

    return indexStack[indexStackSize - 1];
}

public int paStackPeek() {
    if (paStackSize == 0) {
        throw new IllegalStateException("Array index stack is empty");
    }

    return paStack[paStackSize - 1];
}

    public void pushArray() {
        pushPaStack(State.ARR_MARKER);
        pushIndexStack(0);
    }

    public void pushObject() {
        pushPaStack(State.OBJ_MARKER);        
    }
    
    public boolean inArray() {
        return paStackPeek() == State.ARR_MARKER;
    }

    public boolean isFinal() {
        return pa.isFinal(paStackPeek());
    }

    private int getCurrentPaState() {
        int paState;
        // field name after start object or start array
        if (paStackPeek() < 0) {
            int marker = popPaStack(); // pop OBJ_MARKER
            paState = paStackPeek();
            pushPaStack(marker); // push OBJ_MARKER back
        }
        // field name within object
        else {
            paState = paStackPeek();
        }
        return paState;
    }

    public void transitionOnKey(String key) {
        int paState = this.getCurrentPaState();
        pushPaStack(pa.transition(paState, key));   
    }

    public void popArray() {
        popIndexStack();
        popPaStack();
        popPaStack();        

    }

    public void popObject() {
        if (paStackPeek() != State.ARR_MARKER) {
            popPaStack();
        }
        if (paStackPeek() != State.ARR_MARKER) {
            popPaStack();
        }
    }

    public void popPrimitive() {    
        popPaStack();
    }

    // !!!!
    public boolean atArrayIndex(int searchedIndex) {
        return inArray() && searchedIndex == indexStackPeek();
    }
    public void increaseArraySize() {
        int i = popIndexStack();
        pushIndexStack(i+1);
    }
            
    /*
     * Perform path automaton transition on index of an array element and
     * increment array size correspondingly
     */

    public void handleArrayElement() {
        int i = popIndexStack();
        popPaStack(); // pop ARR_MARKER
        int paState = paStackPeek();
        pushPaStack(State.ARR_MARKER); // push ARR_MARKER back        
        // use transition version for int so that we avoid allocating a String on heap
        pushPaStack(pa.transition(paState, i));
        pushIndexStack(i + 1);
    }

    // move to value and if it is a structure, process opening token
    public void moveToValue() {
        try {
            parser.nextToken(); 
        } catch (IOException e) {            
            throw new TransducerException("Failed to advance parser while moving to value", e);
        }

        if (parser.currentToken() == JsonToken.START_ARRAY) {
            pushPaStack(State.ARR_MARKER);
            pushIndexStack(0);
        }
        else if (parser.currentToken() == JsonToken.START_OBJECT) {
            pushPaStack(State.OBJ_MARKER);
        }
    }                

    /* Destination BufferStackTransducer needs to redefine pa according to destPath*/
    protected PathAutomaton getPa() {
        return this.pa;
    }

    protected void setPa(PathAutomaton pa) {
        this.pa = pa;
    }
    
    
    public State getEvalPathState() {
        return this.evalPathState;
    }

    public State getMatchPathState() {
        return this.matchPathState;
    }

    public State getSubtreeSkipState() {
        return this.subtreeSkipState;
    }

    public State getSubtreeGenState() {
        return this.subtreeGenState;
    }

    public State getFindPosState() {
        return this.findPosState;
    }

    public State getMatchPosState() {
        return this.matchPosState;
    }



    public void processValueAfterMatch() {
        // in case of a fieldname match, we move to the corresponding value
        if (parser.currentToken() == JsonToken.FIELD_NAME) {                
            moveToValue();
        }
        // we process the start of current value with next state
        paused = true;
        // we do not generate the start of the current value
        generating = false;
    }

    

}
