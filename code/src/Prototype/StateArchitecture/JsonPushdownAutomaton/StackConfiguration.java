package Prototype.StateArchitecture.JsonPushdownAutomaton;

/*
Tripples stored in the stack
- path automaton state
- value type
- array size (null for objects and primitives)
*/
public class StackConfiguration {

    //primitive types for fields are used so that they are not allocated on the heap
    // (no enums, objects etc.)
    public static final byte OBJECT_TYPE = 1;
    public static final byte ARRAY_TYPE = 2;
    public static final byte PRIMITIVE_TYPE = 3;
    public static final byte UNKNOWN_TYPE = 0;

    public static final int INITIAL_ARRAY_SIZE = 0;
    public static final int DUMMY_ARRAY_SIZE = -1;

    private int paState;
    private byte valueType; 
    private int arraySize;

    // uninitialized
    public StackConfiguration(int state) {
        this.paState = state;
        this.valueType = UNKNOWN_TYPE;
        this.arraySize = DUMMY_ARRAY_SIZE;
    }

    // initialized
    public StackConfiguration(int state, byte valueType) {
        this.paState = state;
        init(valueType);
    }

    // initalization (it's public since we use postponed initialization in Evaluator)
    public void init(byte valueType) {
        this.valueType = valueType;
        this.arraySize = (valueType == ARRAY_TYPE)
                ? INITIAL_ARRAY_SIZE
                : DUMMY_ARRAY_SIZE;
    }

    public int getPaState() {
        return this.paState;
    }

    // setter is necessary specifically to be able to re-use stack configurations in Evaluator
    public void setPaState(int state) {
        this.paState = state;
    }

    public byte getValueType() {
        return valueType;
    }

    public int getArraySize() {
        return this.arraySize;
    }

    public void incrementArraySize() {        
        arraySize++;
    }

}