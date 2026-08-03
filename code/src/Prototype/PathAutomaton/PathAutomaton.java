package Prototype.PathAutomaton;

import java.util.*;

/* 
Path automaton for low-memory / low-latency JSONPath fragment
- internally owns a FiniteAutomaton
Transition methods must not perform heap allocations 
otherwise the measurement of overall memory allocations wouldn't work as desired   
*/

public class PathAutomaton {
    public static final String PATH_INITIAL_SYMBOL = "$";
    public static final int INITIAL_STATE = 0;
    public static final int REJECT_STATE = -1;

    public enum Axis {
        CHILD,
        DESCENDANT
    }

    private final FiniteAutomaton fa;

    private static class Segment {

        private final Axis axis;
        private final String selector;

        public Segment(Axis axis, String selector) {
            this.axis = axis;
            this.selector = selector;
        }

        public Axis getAxis() {
            return axis;
        }

        public String getSelector() {
            return selector;
        }
    }

    public PathAutomaton(String path) {

        List<Segment> segments = parseJsonPath(path);
        FiniteAutomaton nfa = buildFiniteAutomaton(segments);

        if (nfa.isDeterministic()) {
            this.fa = nfa;
        } else {
            this.fa = AutomatonConverter.toDfa(nfa);
        }
    }

    // parses simplified JSONPath into collection of segments
    // generated (GPT-5.5)
    private static List<Segment> parseJsonPath(String path) {
        ArrayList<Segment> segments = new ArrayList<>();
        Axis axis = Axis.CHILD;
        int i = 0;

        // remove leading "$"
        if (!path.isEmpty() && path.charAt(0) == '$') {
            i = 1;
        }

        while (i < path.length()) {
            char c = path.charAt(i);

            // .name, ..name, 
            if (c == '.') {
                // descendant
                if (i + 1 < path.length() && path.charAt(i + 1) == '.') {
                    axis = Axis.DESCENDANT;
                    i += 2;
                    // child
                } else {
                    axis = Axis.CHILD;
                    i++;
                }
                int start = i;
                while (i < path.length()
                        && path.charAt(i) != '.'
                        && path.charAt(i) != '[') {
                    i++;
                }
                String fieldName = path.substring(start, i);
                if (!isFieldNameOrWildCard(fieldName))
                {
                    throw new IllegalArgumentException("Not a name:" + fieldName);
                }
                segments.add(new Segment(axis, fieldName));
                axis = Axis.CHILD;

            // array index or quoted name in [ ]
            } else if (c == '[') {
                // simplified JSONPath - no nested brackets or filters
                int end = path.indexOf(']', i);
                if (end == -1) {
                    throw new IllegalArgumentException("Missing ] in JSONPath");
                }

                String selector = path.substring(i + 1, end);
                segments.add(new Segment(axis, selector));

                i = end + 1;
                axis = Axis.CHILD;
            }
    
        }
        return segments;
    }

    private FiniteAutomaton buildFiniteAutomaton(List<Segment> segments) {

        boolean isDeterministic = true;
        int currentState = INITIAL_STATE;
        List<List<Transition>> tempTransitions = new ArrayList<>(segments.size());

        // iterate over all segments
        for (Segment currentSegment : segments) {
            // transitions for current segment
            List<Transition> transitionsForCurrentState = new ArrayList<>(2);
            // descendant - add looping within the current state
            if (currentSegment.getAxis() == Axis.DESCENDANT) {
                // descendant axis induces nondeterminism
                isDeterministic = false;
                // loop for any input
                Transition t = new Transition(WildcardLabel.INSTANCE, currentState);
                transitionsForCurrentState.add(t);
            }
            // transition to the next state
            int nextState = ++currentState;
            String selector = currentSegment.getSelector();
            Label label = createLabel(selector);
            Transition t = new Transition(label, nextState);

            transitionsForCurrentState.add(t);
            tempTransitions.add(transitionsForCurrentState);
        }
        // add empty transition set for last (final) state
        tempTransitions.add(new ArrayList<>());
        // finalStates set consists of currentState only
        return new FiniteAutomaton(tempTransitions, Set.of(currentState), isDeterministic);

    }

    // create label based on segment selector
    private Label createLabel(String selector) {
        // wildcard selector
        if (selector.equals("*")) {
            return WildcardLabel.INSTANCE;
        }
        // list selector
        if (selector.contains(",")) {
            String[] parts = selector.split(",", -1);

            int[] indices = new int[parts.length];
            int previous = -1;

            for (int i = 0; i < parts.length; i++) {
                try {
                    indices[i] = Integer.parseInt(parts[i]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Names in list selectors are not supported: " + selector, e);
                }

                if (indices[i] <= previous) {
                    throw new IllegalArgumentException(
                            "Index list must be strictly increasing: " + selector);
                }

                previous = indices[i];
            }
            return new ListLabel(indices);
        }
        // slice selector
        if (selector.contains(":")) {
            String[] parts = selector.split(":", -1);
            if (parts.length != 2 && parts.length != 3) {
                throw new IllegalArgumentException("Invalid slice selector: " + selector);
            }
            // all parts default to null (meaning they are not present in the selector)
            Integer start = null;
            Integer end = null;
            Integer step = null;
            try {
                if (!parts[0].isEmpty()) {
                    start = Integer.parseInt(parts[0]);
                }
                if (!parts[1].isEmpty()) {
                    end = Integer.parseInt(parts[1]);
                }
                if (parts.length == 3 && !parts[2].isEmpty()) {
                    step = Integer.parseInt(parts[2]);
                }
                return SliceLabel.of(start, end, step);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Invalid slicing (cannot convert nonempty string to int): " + selector, e);
            }

        }
        // name or index selector
        if (selector.matches("[0-9]+")) {
            return new IndexLabel(Integer.parseInt(selector));
        }
        return new NameLabel(selector);
    }

    public int transition(int state, String input) {
        return transitionCore(state, input, 0, true);
    }

    public int transition(int state, int input) {
        return transitionCore(state, null, input, false);
    }

    private int transitionCore(int state, String strInput, int intInput, boolean isString) {

        // rejecting state has a self-loop
        if (state == REJECT_STATE) {
            return REJECT_STATE;
        }

        // DFA - at most one transition matches
        List<Transition> transitions = fa.transitions().get(state);

        for (int i = 0; i < transitions.size(); i++) {
            Transition transition = transitions.get(i);

            boolean matched = isString
                    ? transition.label.matches(strInput)
                    : transition.label.matches(intInput);

            if (matched) {
                return transition.nextState;
            }
        }

        return REJECT_STATE;
    }

    public boolean isFinalState(int i) {
        return this.fa.finalStates().contains(i);
    }

    // TODO: needs refinement based on RFC
    private static boolean isFieldNameOrWildCard(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(s);
            return false;   // it's an index
        } catch (NumberFormatException e) {
            return true;    // it's a field name
    }
}

}
