package Prototype.PathAutomaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Simple dot notation
public class SimplePathAutomaton implements PathAutomaton {
    int INITIAL_PA_STATE = 0;
    private final ArrayList<String> segments;    
    private final int finalState;
    private final int errorState;
 
    public SimplePathAutomaton(String path) {
        this.segments = parse(path);              
        this.finalState = segments.size();
        this.errorState = segments.size() + 1;
    }        
    
    private ArrayList<String> parse(String path) {
        
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        // remove leading "$"
        path = path.substring(1);
        // remove leading "["
        if (path.startsWith("[")) {
            path = path.substring(1);
        }

        return new ArrayList<>(
                // split on "." or "[" occurence   
                Arrays.stream(path.split("[.\\[]"))
                        .map(String::trim)
                        .map(s -> s.replace("[", "").replace("]", ""))
                        .toList()
);             
    }
       
    @Override
    public String getSegment(int state) {        
        if (state == INITIAL_PA_STATE) return null;
        return segments.get(state - 1);
    }
    
    
    @Override
    public boolean isFinal(int state) {
        return (state == finalState);
    }
    
    @Override
    public int transition(int currentState, String inputSegment) {
        // entering error state from last symbol and looping in error state
        if (currentState >= segments.size() || currentState == errorState) {
            return errorState;
        }
        // match
        // replace with Objects.equals(segments.get(currentState), inputSegment) if inputSegment can be NULL
        if (segments.get(currentState).equals(inputSegment)) {
            currentState++;
            return currentState;
        }
        // entering error state from other symbol than the last one
        return errorState;
    }
    
}
