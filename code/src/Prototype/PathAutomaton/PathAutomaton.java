package Prototype.PathAutomaton;

public interface PathAutomaton {
    public int transition(int currentState, String inputSegment);  
    public boolean isFinal(int state);
    public String getSegment(int state);
}
