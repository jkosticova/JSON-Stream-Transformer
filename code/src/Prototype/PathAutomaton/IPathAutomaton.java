package Prototype.PathAutomaton;

public interface IPathAutomaton {
    public int transition(int currentState, String inputSegment);  
    public boolean isFinal(int state);
    public String getSegment(int state);
}
