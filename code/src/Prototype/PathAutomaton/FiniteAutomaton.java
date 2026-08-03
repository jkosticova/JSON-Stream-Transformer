package Prototype.PathAutomaton;

import java.util.*;

public record FiniteAutomaton(
        List<List<Transition>> transitions,
        Set<Integer> finalStates,
        boolean isDeterministic) {
   
}