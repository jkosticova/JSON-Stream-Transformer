package prototype.stateArchitecture.transducer;


import com.fasterxml.jackson.core.JsonToken;

import prototype.mapper.SpecificationMapper;
import prototype.pathAutomaton.PathAutomaton;
import prototype.pathAutomaton.SimplePathAutomaton;
import prototype.specificationParser.CopyTransformation;
import prototype.specificationParser.MoveTransformation;
import prototype.stateArchitecture.state.Memout;
import prototype.stateArchitecture.state.State;
import prototype.stateArchitecture.state.freeTraversal.MeminSkip;
import prototype.stateArchitecture.state.subtreeTraversal.SubtreeMemin;
import prototype.stateArchitecture.state.subtreeTraversal.SubtreeSkipMemin;

public class BufferStackTransducer extends StackTransducer {

    public static byte SIMPLE_TRANSDUCER = 0;
    public static byte SRC_TRANSDUCER = 1;
    public static byte DEST_TRANSDUCER = 2;
    
    private SubtreeMemin subtreeMeminState;
    private SubtreeSkipMemin SubtreeSkipMeminState;
    private MeminSkip meminSkipState;
    private Memout memoutState;

    private byte transducerRole;
    private BufferSyncTransducer parentTransducer;    

    /* constructor for a single COPY or MOVE transformation */
    public BufferStackTransducer(SpecificationMapper mapper, BufferSyncTransducer parentTransducer, byte role) {
        
        super(mapper, parentTransducer.parser, parentTransducer.generator);

        this.parentTransducer = parentTransducer;                        
        this.transducerRole = role;
                
        // overwrite path and PA for destination transducer
        if (transducerRole == DEST_TRANSDUCER) {                
            if (transformationType.equals("copy")) {
                this.path = ((CopyTransformation) specification).getDestPath();
            }
            else if (transformationType.equals("move")) {
                this.path = ((MoveTransformation) specification).getDestPath();
            }
        }    
        
        PathAutomaton newPa = new SimplePathAutomaton(path);
        this.setPa(newPa);        
        //this.getPaStack().push(INITIAL_PA_STATE);
        
        initStates();
        currentState = evalPathState;

    }

     

    protected void initStates() {
        super.initStates();    
        subtreeMeminState = new SubtreeMemin(this);
        SubtreeSkipMeminState = new SubtreeSkipMemin(this);
        meminSkipState = new MeminSkip(this);
        memoutState = new Memout(this);
    }

    public void processValueAfterMatch() {
        // in case of a fieldname match, we move to the corresponding value
        if (parser.currentToken() == JsonToken.FIELD_NAME) {
            // SRC and DEST transducer - movement must be synchronized by buffer transducer
            // parentTransducer.moveToValue(...) throws TransducerException on
            // failure (see BufferTransducer), which propagates naturally
            // from here - no separate handling needed.            
            parentTransducer.moveToValue(this.transducerRole);
            
        }
        // we process the start of current value with next state
        paused = true;
        // we do not generate the start of the current value
        generating = false;
    }

    public byte getTransducerRole() {
        return this.transducerRole;
    }

    public void setTransducerRole(byte type) {
        this.transducerRole = type;
    }

    public void setFirstMatch(byte firstMatch) {        
        this.parentTransducer.setFirstMatch(firstMatch);        
    }

    public byte getFirstMatch() {        
        return parentTransducer.getFirstMatch();
                
    }

    public void getFromMemory() {
        requireParentTransducer("getFromMemory").getFromMemory();
    }


    public void addToMemory() {
        requireParentTransducer("addToMemory").addToMemory();
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

    /**
     * getFromMemory/addToMemory only make sense for SRC/DEST transducers
     * that were constructed with a parentTransducer (see the COPY/MOVE
     * constructors in StackTransducer). Calling either on a SIMPLE
     * transducer is a caller bug, not an expected runtime condition -
     * previously it surfaced as a bare NullPointerException with no
     * indication of what went wrong or why. This turns that into an
     * explicit, descriptive failure at the point of misuse.
     */
    private BufferSyncTransducer requireParentTransducer(String operation) {
        if (parentTransducer == null) {
            throw new IllegalStateException(
                    operation + "() requires a parentTransducer; this transducer has role "
                            + transducerRole + " and was not constructed as part of a copy/move BufferTransducer");
        }
        return parentTransducer;
    }


}
