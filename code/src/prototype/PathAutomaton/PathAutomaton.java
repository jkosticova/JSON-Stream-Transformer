package prototype.pathAutomaton;

public interface PathAutomaton {
    public int transition(int currentState, String inputSegment);  
    public int transition(int currentState, int index);  
    public boolean isFinal(int state);
    public String getSegment(int state);
}
