package Prototype.PathAutomaton;

/* 
A transition of a finite state automaton.
- the current state is handled implicitly as the index within the collection of transitions
*/
public final class Transition {
    public final Label label;
    public final int nextState;

    public Transition(Label label, int nextState) {
        this.label = label;
        this.nextState = nextState;
    }
}
