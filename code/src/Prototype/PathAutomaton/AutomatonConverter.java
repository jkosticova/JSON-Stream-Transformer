package Prototype.PathAutomaton;

import java.util.*;

/* 
Converts NFA to DFA
Conversion works specifically for NFA created within PathAutomaton Constructor
(i.e., this is not generic NFA->DFA conversion)
*/
public class AutomatonConverter {

    public static FiniteAutomaton toDfa (FiniteAutomaton nfa) {
        
        // list of state sets (closures)
        List<Set<Integer>> dfaStateSets = new ArrayList<>();
        Map<Set<Integer>, Integer> stateMap = new HashMap<>();

        Set<Integer> initialStateSet = new HashSet<>();
        // initial state set contains nfa initial state 0
        initialStateSet.add(0);

        dfaStateSets.add(initialStateSet);
        stateMap.put(initialStateSet, 0);

        // output
        List<List<Transition>> dfaTransitions = new ArrayList<>();
        Set<Integer> dfaFinalStates = new HashSet<>();

        // DFA state (index of current dfa state set)
        int currentDfaState = 0;

        // while there remain some non processed DFA state sets
        while (currentDfaState < dfaStateSets.size()) {

            // get corresponding state set for current DFA state
            Set<Integer> currentStateSet = dfaStateSets.get(currentDfaState);

            List<Transition> currentTransitions = new ArrayList<>();
            dfaTransitions.add(currentTransitions);

            // check if current DFA state is final
            for (int state : currentStateSet) {
                if (nfa.finalStates().contains(state)) {                
                    dfaFinalStates.add(currentDfaState);
                    break;
                }
            }

            // collect all ougoing transitions (regardless the label)
            Set<Transition> mergedTransitions = new HashSet<>();

            for (int state : currentStateSet) {
                mergedTransitions.addAll(nfa.transitions().get(state));
            }

            // separate exact labels and special labels
            Map<Label, Set<Integer>> labelTargets = new HashMap<>();

            boolean hasWildcard = false;
            Set<Integer> wildcardTargets = new HashSet<>();
            Set<Label> excludedLabels = new HashSet<>();

            for (Transition t : mergedTransitions) {
                if (t.label instanceof WildcardLabel) {
                    hasWildcard = true;
                    wildcardTargets.add(t.nextState);
                } else {
                    labelTargets
                            .computeIfAbsent(t.label, k -> new HashSet<>())
                            .add(t.nextState);
                    // collect excluded labels just in case there was a wildcard
                    excludedLabels.add(t.label);
                }
            }

            // create DFA transitions for normal labels
            // entry = key-value pair
            for (Map.Entry<Label, Set<Integer>> entry : labelTargets.entrySet()) {

                Set<Integer> targetSet = new HashSet<>(entry.getValue());

                // if there was wildcard, also wildcard targets must be added
                if (hasWildcard) {
                    targetSet.addAll(wildcardTargets);
                }

                int targetState = getOrCreateState(
                        targetSet,
                        dfaStateSets,
                        stateMap);

                currentTransitions.add(
                        new Transition(entry.getKey(), targetState));
            }

            // convert wildcard + excluded labels into negation label            
            if (hasWildcard) {

                Label[] excludedArray = excludedLabels.toArray(new Label[0]);
                NegationLabel negation = new NegationLabel(excludedArray);

                int targetState = getOrCreateState(
                        wildcardTargets,
                        dfaStateSets,
                        stateMap);

                currentTransitions.add(
                        new Transition(negation, targetState));
            }
            currentDfaState++;
        }        
        // result FA is deterministic -> isDeterministic = true
        return new FiniteAutomaton(dfaTransitions, dfaFinalStates, true);
    }

    private static int getOrCreateState(
            Set<Integer> stateSet,
            List<Set<Integer>> dfaStateSets,
            Map<Set<Integer>, Integer> stateMap) {

        Integer index = stateMap.get(stateSet);

        if (index != null) {
            return index;
        }

        index = dfaStateSets.size();

        Set<Integer> immutable = Set.copyOf(stateSet);

        dfaStateSets.add(immutable);
        stateMap.put(immutable, index);

        return index;
    }
}