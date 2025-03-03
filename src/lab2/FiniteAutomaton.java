package lab2;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import java.util.*;

class FiniteAutomaton {
    Set<Character> states;
    Set<Character> alphabet;
    Map<Character, Map<Character, Set<Character>>> transitions;
    char startState;
    Set<Character> finalStates;

    public FiniteAutomaton(Set<Character> states,
                           Set<Character> alphabet,
                           Map<Character, Map<Character, Set<Character>>> transitions,
                           char startState,
                           Set<Character> finalStates) {
        this.states = states;
        this.alphabet = alphabet;
        this.transitions = transitions;
        this.startState = startState;
        this.finalStates = finalStates;
    }
}


class FiniteAutomatonStringVersion {
    Set<String> states;
    Set<Character> alphabet;
    Map<String, Map<Character, Set<String>>> transitions;
    String startState;
    Set<String> finalStates;

    public FiniteAutomatonStringVersion(Set<String> states,
                                        Set<Character> alphabet,
                                        Map<String, Map<Character, Set<String>>> transitions,
                                        String startState,
                                        Set<String> finalStates) {
        this.states = states;
        this.alphabet = alphabet;
        this.transitions = transitions;
        this.startState = startState;
        this.finalStates = finalStates;
    }
}

class RegularGrammarConverter {
    public static Map<Character, List<String>> convertToRegularGrammar(FiniteAutomaton fa) {
        Map<Character, List<String>> grammar = new HashMap<>();
        // Initialize each state in the grammar
        for (char state : fa.states) {
            grammar.put(state, new ArrayList<>());
        }
        // For every transition, add a production to the grammar
        for (char state : fa.states) {
            Map<Character, Set<Character>> transitionMap = fa.transitions.get(state);
            if (transitionMap != null) {
                for (Map.Entry<Character, Set<Character>> entry : transitionMap.entrySet()) {
                    char input = entry.getKey();
                    for (char nextState : entry.getValue()) {
                        grammar.get(state).add(input + " " + nextState);
                    }
                }
            }
        }
        // For every final state, add an epsilon production
        for (char finalState : fa.finalStates) {
            grammar.get(finalState).add("ε");
        }

        return grammar;
    }
}

class DeterminismChecker {
    public static boolean isDeterministic(FiniteAutomaton fa) {
        for (char state : fa.states) {
            Map<Character, Set<Character>> transitionMap = fa.transitions.get(state);
            if (transitionMap != null) {
                for (Set<Character> nextStates : transitionMap.values()) {
                    if (nextStates.size() > 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

class NDFAToDFAConverter {
    public static FiniteAutomatonStringVersion convertToDFA(FiniteAutomaton ndfa) {
        //track the subsets of characters that form each DFA state
        Set<Set<Character>> dfaStates = new HashSet<>();
        Map<Set<Character>, Map<Character, Set<Character>>> dfaTransitions = new HashMap<>();

        //Start subset is the epsilon closure of the NDFA start state
        Set<Character> startSubset = epsilonClosure(ndfa, new HashSet<>(Collections.singletonList(ndfa.startState)));
        dfaStates.add(startSubset);

        //queue to explore new subsets
        Queue<Set<Character>> queue = new LinkedList<>();
        queue.add(startSubset);

        while (!queue.isEmpty()) {
            Set<Character> currentSubset = queue.poll();

            // For each symbol in the NDFA alphabet, find all possible next-states
            for (char input : ndfa.alphabet) {
                //gather all states reachable on 'input' from any state in currentSubset
                Set<Character> nextSubset = new HashSet<>();
                for (char nfaState : currentSubset) {
                    Map<Character, Set<Character>> transitionMap = ndfa.transitions.get(nfaState);
                    if (transitionMap != null && transitionMap.containsKey(input)) {
                        nextSubset.addAll(transitionMap.get(input));
                    }
                }
                // epsilon closure of all these states
                nextSubset = epsilonClosure(ndfa, nextSubset);
                if (!nextSubset.isEmpty()) {
                    dfaTransitions
                            .computeIfAbsent(currentSubset, k -> new HashMap<>())
                            .put(input, nextSubset);
                    if (!dfaStates.contains(nextSubset)) {
                        dfaStates.add(nextSubset);
                        queue.add(nextSubset);
                    }
                }
            }
        }

        // Now we map each subset of chars (e.g. {A,B}) to a string label (e.g. "A,B").
        // Then we'll build a new FiniteAutomatonStringVersion for the DFA.
        Map<Set<Character>, String> subsetToName = new HashMap<>();

        // Sort the subset and join with commas to form the name
        for (Set<Character> subset : dfaStates) {
            List<Character> sortedList = new ArrayList<>(subset);
            Collections.sort(sortedList);
            // Join them with commas, e.g. [A,B] -> "A,B"
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sortedList.size(); i++) {
                sb.append(sortedList.get(i));
                if (i < sortedList.size() - 1) {
                    sb.append(",");
                }
            }
            subsetToName.put(subset, sb.toString());
        }

        // Build sets and transitions for the new DFA
        Set<String> dfaStateNames = new HashSet<>();
        for (Set<Character> s : subsetToName.keySet()) {
            dfaStateNames.add(subsetToName.get(s));
        }

        // Start state name
        String dfaStartState = subsetToName.get(startSubset);

        // Determine final states


        Set<String> dfaFinalStates = new HashSet<>();
        for (Set<Character> subset : dfaStates) {
            // If subset intersects NDFA final states => final
            for (char finalState : ndfa.finalStates) {
                if (subset.contains(finalState)) {
                    dfaFinalStates.add(subsetToName.get(subset));
                    break;
                }
            }
        }

        // Build new transition map for string-labeled states:
        Map<String, Map<Character, Set<String>>> dfaTransitionMap = new HashMap<>();

        for (Map.Entry<Set<Character>, Map<Character, Set<Character>>> entry : dfaTransitions.entrySet()) {
            Set<Character> fromSubset = entry.getKey();
            String fromName = subsetToName.get(fromSubset);

            dfaTransitionMap.putIfAbsent(fromName, new HashMap<>());
            Map<Character, Set<Character>> transitionsOnInput = entry.getValue();

            for (Map.Entry<Character, Set<Character>> tEntry : transitionsOnInput.entrySet()) {
                char input = tEntry.getKey();
                Set<Character> toSubset = tEntry.getValue();
                String toName = subsetToName.get(toSubset);

                dfaTransitionMap.get(fromName)
                        .computeIfAbsent(input, k -> new HashSet<>())
                        .add(toName);
            }
        }

        // Finally, build the new FiniteAutomatonStringVersion
        return new FiniteAutomatonStringVersion(
                dfaStateNames,
                ndfa.alphabet,
                dfaTransitionMap,
                dfaStartState,
                dfaFinalStates
        );
    }

    // Epsilon closure utility
    private static Set<Character> epsilonClosure(FiniteAutomaton ndfa, Set<Character> states) {
        Set<Character> closure = new HashSet<>(states);
        Queue<Character> queue = new LinkedList<>(states);

        while (!queue.isEmpty()) {
            char state = queue.poll();
            Map<Character, Set<Character>> transitionMap = ndfa.transitions.get(state);
            if (transitionMap != null && transitionMap.containsKey('ε')) {
                for (char nextState : transitionMap.get('ε')) {
                    if (!closure.contains(nextState)) {
                        closure.add(nextState);
                        queue.add(nextState);
                    }
                }
            }
        }

        return closure;
    }
}

class GraphVisualization {

    public static void visualizeAutomaton(FiniteAutomaton fa, String title) {
        Graph graph = new SingleGraph(title);

        for (char state : fa.states) {
            String nodeId = String.valueOf(state);
            graph.addNode(nodeId).setAttribute("ui.label", nodeId);

            // Mark final states
            if (fa.finalStates.contains(state)) {
                graph.getNode(nodeId).setAttribute("ui.class", "final");
            }
            // Mark start state
            if (state == fa.startState) {
                graph.getNode(nodeId).setAttribute("ui.class", "start");
            }
        }

        // Add edges (transitions)
        for (char fromState : fa.transitions.keySet()) {
            Map<Character, Set<Character>> transitionMap = fa.transitions.get(fromState);
            for (char input : transitionMap.keySet()) {
                for (char toState : transitionMap.get(input)) {
                    String edgeId = fromState + "-" + input + "-" + toState;
                    if (graph.getEdge(edgeId) == null) {
                        graph.addEdge(edgeId, String.valueOf(fromState), String.valueOf(toState), true)
                                .setAttribute("ui.label", String.valueOf(input));
                    }
                }
            }
        }

        graph.setAttribute("ui.stylesheet", """
            node {
                size: 40px;
                fill-color: #ccc;
                text-size: 20px;
                text-alignment: center;
                text-style: bold;
                text-color: #000;
                stroke-mode: none;
            }
            node.start {
                fill-color: #8f8; /* Light green for start state */
                shape: box;       /* Example: make it a box */
                stroke-mode: plain;
                stroke-color: #080;
                stroke-width: 3px;
            }
            node.final {
                fill-color: #fdd; /* Light red for final state */
                stroke-mode: plain;
                stroke-color: #f00;
                stroke-width: 3px;
            }
            edge {
                text-size: 15px;
                text-alignment: above;
                text-background-mode: plain;
                text-background-color: white;
                text-padding: 2px;
                text-offset: 10px, 10px;
            }
        """);
        System.setProperty("org.graphstream.ui", "swing");
        Viewer viewer = graph.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
    }

    // Method to visualize the new DFA (string-based states)
    public static void visualizeAutomatonString(FiniteAutomatonStringVersion fa, String title) {
        Graph graph = new SingleGraph(title);

        // Add nodes (states)
        for (String state : fa.states) {
            graph.addNode(state).setAttribute("ui.label", state);

            // Mark final
            if (fa.finalStates.contains(state)) {
                graph.getNode(state).setAttribute("ui.class", "final");
            }
            // Mark start
            if (state.equals(fa.startState)) {
                graph.getNode(state).setAttribute("ui.class", "start");
            }
        }

        // Add edges (transitions)
        for (String fromState : fa.transitions.keySet()) {
            Map<Character, Set<String>> transitionMap = fa.transitions.get(fromState);
            for (char input : transitionMap.keySet()) {
                for (String toState : transitionMap.get(input)) {
                    String edgeId = fromState + "-" + input + "-" + toState;
                    if (graph.getEdge(edgeId) == null) {
                        graph.addEdge(edgeId, fromState, toState, true)
                                .setAttribute("ui.label", String.valueOf(input));
                    }
                }
            }
        }

        graph.setAttribute("ui.stylesheet", """
            node {
                size: 40px;
                fill-color: #ccc;
                text-size: 20px;
                text-alignment: center;
                text-style: bold;
                text-color: #000;
                stroke-mode: none;
            }
            node.start {
                fill-color: #8f8; /* Light green for start state */
                shape: box;
                stroke-mode: plain;
                stroke-color: #080;
                stroke-width: 3px;
            }
            node.final {
                fill-color: #fdd; /* Light red for final state */
                stroke-mode: plain;
                stroke-color: #f00;
                stroke-width: 3px;
            }
            edge {
                text-size: 15px;
                text-alignment: above;
                text-background-mode: plain;
                text-background-color: white;
                text-padding: 2px;
                text-offset: 10px, 10px;
            }
        """);
        System.setProperty("org.graphstream.ui", "swing");
        Viewer viewer = graph.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
    }
}

 class Main {
    public static void main(String[] args) {
        Set<Character> states = new HashSet<>(Arrays.asList('A', 'B', 'C', 'D', 'E'));
        Set<Character> alphabet = new HashSet<>(Arrays.asList('a', 'b'));
        Map<Character, Map<Character, Set<Character>>> transitions = new HashMap<>();
        transitions.put('A', new HashMap<>());
        transitions.get('A').put('a', new HashSet<>(Arrays.asList('B')));
        transitions.put('B', new HashMap<>());
        transitions.get('B').put('b', new HashSet<>(Arrays.asList('B', 'C')));
        transitions.put('C', new HashMap<>());
        transitions.get('C').put('b', new HashSet<>(Arrays.asList('A')));
        transitions.get('C').put('a', new HashSet<>(Arrays.asList('D')));
        transitions.put('D', new HashMap<>());
        transitions.get('D').put('a', new HashSet<>(Arrays.asList('E')));
        transitions.put('E', new HashMap<>());
        transitions.get('E').put('a', new HashSet<>(Arrays.asList('A')));

        char startState = 'A';
        Set<Character> finalStates = new HashSet<>(Arrays.asList('D'));

        FiniteAutomaton ndfa = new FiniteAutomaton(states, alphabet, transitions, startState, finalStates);

        Map<Character, List<String>> grammar = RegularGrammarConverter.convertToRegularGrammar(ndfa);
        System.out.println("=== Task (a): Regular Grammar ===");
        for (Map.Entry<Character, List<String>> entry : grammar.entrySet()) {
            System.out.println(entry.getKey() + " -> " + String.join(" | ", entry.getValue()));
        }
        boolean isDeterministic = DeterminismChecker.isDeterministic(ndfa);
        System.out.println("\n=== Task (b): Is the FA Deterministic? ===");
        System.out.println("Is Deterministic? " + isDeterministic);
        FiniteAutomatonStringVersion dfa = NDFAToDFAConverter.convertToDFA(ndfa);
        System.out.println("\n=== Task (c): Converted DFA ===");
        System.out.println("DFA States: " + dfa.states);
        System.out.println("DFA Start State: " + dfa.startState);
        System.out.println("DFA Final States: " + dfa.finalStates);
        System.out.println("DFA Transitions:");
        for (String fromStateName : dfa.transitions.keySet()) {
            System.out.println("  From " + fromStateName + " => " + dfa.transitions.get(fromStateName));
        }

        System.out.println("\n=== Task (d): Visualizing NDFA and DFA ===");
        GraphVisualization.visualizeAutomaton(ndfa, "NDFA");
        GraphVisualization.visualizeAutomatonString(dfa, "DFA");
    }
}
