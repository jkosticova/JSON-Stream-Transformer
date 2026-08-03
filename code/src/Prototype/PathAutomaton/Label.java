package Prototype.PathAutomaton;

import java.util.*;

/* 
Labels of Path Automaton
- name
- index
- wildcard
- negation (used when converting nfa to dfa)
- list
- array slice

Wildcard label is modeled as a singleton.
All other labels are modeled as records to get equals()/hashCode() for free.
Limitations: see readme.md
*/

public sealed interface Label
        // restrict the list of implementations for better maintainability
        permits NameLabel, IndexLabel, WildcardLabel, NegationLabel, ListLabel, SliceLabel {

    boolean matches(String input);

    boolean matches(int input);
}

record NameLabel(String name) implements Label {

    NameLabel {
        Objects.requireNonNull(name);
    }

    @Override
    public boolean matches(String input) {
        return name.equals(input);
    }

    @Override
    public boolean matches(int input) {
        return false;
    }
}

record IndexLabel(int index) implements Label {

    @Override
    public boolean matches(String input) {
        return false;
    }

    @Override
    public boolean matches(int input) {
        return index == input;
    }
}

// stateless singleton - all wildcard labels behave identically
final class WildcardLabel implements Label {

    // public instance
    public static final WildcardLabel INSTANCE = new WildcardLabel();

    // private constructor
    private WildcardLabel() {
    }

    @Override
    public boolean matches(String input) {
        return true;
    }

    @Override
    public boolean matches(int input) {
        return true;
    }
}

record NegationLabel(Label[] excludedLabels) implements Label {

    NegationLabel {
        Objects.requireNonNull(excludedLabels);
        excludedLabels = excludedLabels.clone();
    }

    @Override
    public boolean matches(String input) {
        for (int i = 0; i < excludedLabels.length; i++) {
            if (excludedLabels[i].matches(input)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matches(int input) {
        for (int i = 0; i < excludedLabels.length; i++) {
            if (excludedLabels[i].matches(input)) {
                return false;
            }
        }
        return true;
    }
}

record ListLabel(int[] indices) implements Label {

    ListLabel {
        Objects.requireNonNull(indices);
        if (indices.length == 0) {
            throw new IllegalArgumentException("List selector cannot be empty");
        }
        int previous = indices[0];

        for (int i = 1; i < indices.length; i++) {
            int current = indices[i];
            if (previous >= current) {
                throw new IllegalArgumentException(
                        "Unordered index lists are not supported");
            }
            previous = current;
        }

        indices = indices.clone();
    }

    @Override
    public boolean matches(int value) {
        // linear search is sufficient since we expect short lists
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(String value) {
        return false;
    }
}

// See RFC for definition: https://www.rfc-editor.org/rfc/rfc9535.html#slice
record SliceLabel(int start, Integer end, int step) implements Label {

    public SliceLabel {
        if (start < 0 || (end != null && end < 0)) {
            throw new IllegalArgumentException(
                    "Negative values in array slice operator are not supported");
        }
        // negative step is not supported since that means returning array elements in
        // reverse order
        // -> the query would be non-order-preserving
        if (step < 0) {
            throw new IllegalArgumentException("Step must be positive");
        }
    }

    // static factory method to handle nullable inputs
    public static SliceLabel of(Integer start, Integer end, Integer step) {
        // RFC: If step >= 0 then the default value for start is 0
        int actualStart = (start != null) ? start : 0;
        // RFC: The default value for step is 1
        int actualStep = (step != null) ? step : 1;
        return new SliceLabel(actualStart, end, actualStep);
    }

    @Override
    public boolean matches(int value) {
        // RFC: "When step is 0, no elements are selected."
        if (step == 0) {
            return false;
        }
        // RFC: The array slice expression ... matches
        // elements from arrays starting at index <start>
        // and ending at (but not including) <end>,
        // while incrementing by step ...
        return value >= start
                && (end == null || value < end)
                && (value - start) % step == 0;
    }

    @Override
    public boolean matches(String input) {
        return false;
    }

}
