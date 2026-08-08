package prototype.stateArchitecture.transducer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import prototype.mapper.SpecificationMapper;
import prototype.specificationParser.CopyTransformation;
import prototype.specificationParser.MoveTransformation;
import prototype.specificationParser.TransformationFormat;
import prototype.stateArchitecture.state.Memout;
import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.state.eval.EvalPath;
import prototype.stateArchitecture.state.eval.FindPos;
import prototype.stateArchitecture.state.freeTraversal.Gen;
import prototype.stateArchitecture.state.freeTraversal.MeminSkip;
import prototype.stateArchitecture.state.match.MatchPath;
import prototype.stateArchitecture.state.match.MatchPos;
import prototype.stateArchitecture.state.subtreeTraversal.SubtreeGen;
import prototype.stateArchitecture.state.subtreeTraversal.SubtreeMemin;
import prototype.stateArchitecture.state.subtreeTraversal.SubtreeSkip;
import prototype.stateArchitecture.state.subtreeTraversal.SubtreeSkipMemin;
import prototype.pathAutomaton.PathAutomaton;

import java.util.Stack;

/* Abstract class for StackTransducer and IdentityTransducer
 */

public abstract class Transducer {
    public static final int INITIAL_PA_STATE = 0;
    public static final int OBJECT_ARR_INDEX = -1;

    public static byte SIMPLE_TRANSDUCER = 0;
    public static byte SRC_TRANSDUCER = 1;
    public static byte DEST_TRANSDUCER = 2;

    // transducer state
    protected State currentState;
    protected boolean paused;
    protected boolean generating;

    // other properties - axctually final, but are created in child classes
    protected byte transducerRole;
    protected TransformationFormat specification;
    protected BufferTransducer parentTransducer;
    protected String transformationType;

    // stacks
    Stack<Integer> paStack;
    Stack<Integer> indexStack;

    PathAutomaton pa;
    String path;
    public Byte matchType = State.NO_MATCH;
    public boolean moveToValue = false;

    // I/O
    JsonGenerator generator;
    JsonParser parser;


    // resuable states (must be reused due to measuremennt nethod that counts all memory allocations)
    protected EvalPath evalPathState;
    protected MatchPath matchPathState;
    protected Gen genState;
    protected SubtreeSkip subtreeSkipState;
    protected SubtreeGen subtreeGenState;
    protected FindPos findPosState;
    protected MatchPos matchPosState;
    protected SubtreeMemin subtreeMeminState;
    protected SubtreeSkipMemin SubtreeSkipMeminState;
    protected MeminSkip meminSkipState;
    protected Memout memoutState;

    public Transducer(SpecificationMapper mapper, byte role) {

        paused = false;
        generating = true;

        this.specification = mapper.getTransformationFormat();
        if (this.specification == null) {            
            throw new IllegalArgumentException("SpecificationMapper returned no transformation format");
        }

        this.transformationType = specification.getType();
        this.transducerRole = role;

        // simple and source transducer
        if (this.transducerRole != DEST_TRANSDUCER) {
            this.path = specification.getPath();

        }
        // destination transducer
        else if (transformationType.equals("copy")) {
            this.path = ((CopyTransformation) specification).getDestPath();
        }
        else if (transformationType.equals("move")) {
            this.path = ((MoveTransformation) specification).getDestPath();
        }
        else {            
            throw new IllegalStateException(
                    "DEST_TRANSDUCER is only valid for copy/move transformations, got type: " + transformationType);
        }
    }

    // states must be initalized outside constructor, because they need initialized fields from subclasses' constructors
    protected void initStates() {
        evalPathState = new EvalPath(this);
        matchPathState = new MatchPath(this);
        subtreeSkipState = new SubtreeSkip(this);
        subtreeGenState = new SubtreeGen(this);
        findPosState = new FindPos(this);
        matchPosState = new MatchPos(this);
        genState = new Gen(this);
        subtreeMeminState = new SubtreeMemin(this);
        SubtreeSkipMeminState = new SubtreeSkipMemin(this);
        meminSkipState = new MeminSkip(this);
        memoutState = new Memout(this);
    }

    public abstract boolean process();


    public byte getTransducerRole() {
        return this.transducerRole;
    }

    public void setTransducerRole(byte type) {
        this.transducerRole = type;
    }

    public TransformationFormat getSpecification() {
        return this.specification;
    }

    public boolean getPaused() {
        return this.paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public JsonGenerator getGenerator() {
        return this.generator;
    }

    public void setGenerator(JsonGenerator generator) {
        this.generator = generator;
    }

    public State getEvalPathState() {
        return this.evalPathState;
    }

    public State getMatchPathState() {
        return this.matchPathState;
    }

    public State getGenState() {
        return this.genState;
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

    public State getSubtreeMeminState() {
        return this.subtreeMeminState;
    }

    public State getSubtreeSkipMeminState() {
        return this.SubtreeSkipMeminState;
    }

    public State getMeminSkipState() {
        return this.meminSkipState;
    }

    public State getMemoutState() {
        return this.memoutState;
    }

    public Stack<Integer> getPaStack() {
        return this.paStack;
    }

    public abstract void moveToValue();

    public void setFirstMatch(byte firstMatch) {
        if (this.parentTransducer != null) {
            this.parentTransducer.setFirstMatch(firstMatch);
        }
    }

    public byte getFirstMatch() {
        if (parentTransducer != null) {
            return parentTransducer.getFirstMatch();
        }
        else return BufferTransducer.NONE;
    }

    public void setGenerating(boolean generating) {
        this.generating = generating;
    }

    public boolean getGenerating() {
        return this.generating;
    }

    public abstract void processValueAfterMatch();



    public PathAutomaton getPa() {
        return this.pa;
    }


    public Stack<Integer> getIndexStack() {
        return this.indexStack;
    }



    public String getTransformationType() {
        return this.transformationType;
    }


    public State getCurrentState() {
        return this.currentState;
    }


    public void getFromMemory() {
        requireParentTransducer("getFromMemory").getFromMemory();
    }


    public void addToMemory() {
        requireParentTransducer("addToMemory").addToMemory();
    }


    public void setState(State state) {
        this.currentState = state;
    }

    /**
     * getFromMemory/addToMemory only make sense for SRC/DEST transducers
     * that were constructed with a parentTransducer (see the COPY/MOVE
     * constructors in StackTransducer). Calling either on a SIMPLE
     * transducer is a caller bug, not an expected runtime condition -
     * previously it surfaced as a bare NullPointerException with no
     * indication of what went wrong or why. This turns that into an
     * explicit, descriptive failure at the point of misuse.
     */
    private BufferTransducer requireParentTransducer(String operation) {
        if (parentTransducer == null) {
            throw new IllegalStateException(
                    operation + "() requires a parentTransducer; this transducer has role "
                            + transducerRole + " and was not constructed as part of a copy/move BufferTransducer");
        }
        return parentTransducer;
    }
}
