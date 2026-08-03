package Prototype.StateArchitecture.JsonPushdownAutomaton;

public class ProcessingResult {
    public boolean generate;
    public boolean buffer;
    public boolean moveToNext;
    public boolean pause;

    public ProcessingResult() {
        init();
    }

    public void init() {
        this.generate = true;
        this.buffer = false;
        this.moveToNext = false;
        this.pause = false;
    }
    
}
