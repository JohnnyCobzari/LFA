package lab5;

import java.util.*;

public class Grammar {
    private List<String> nonTerminals;
    private List<String> terminals;
    private Map<String, List<String>> rules;
    private String start;

    private static final String EPSILON = "ε"; // You can change it depending on your EPSILON symbol

    public Grammar(List<String> nonTerminals, List<String> terminals, Map<String, List<String>> rules, String start) {
        this.nonTerminals = new ArrayList<>(nonTerminals);
        this.terminals = new ArrayList<>(terminals);
        this.rules = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : rules.entrySet()) {
            this.rules.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        this.start = start;
    }

    public void printRules() {
        for (String nonTerminal : rules.keySet()) {
            System.out.println(nonTerminal + " -> " + String.join(" | ", rules.get(nonTerminal)));
        }
    }

    public boolean isCNF() {
        for (String nonTerminal : rules.keySet()) {
            for (String production : rules.get(nonTerminal)) {
                if (production.length() == 0 || production.length() > 2) {
                    return false;
                }
                if (production.length() == 1 && !terminals.contains(production)) {
                    return false;
                }
                if (production.length() == 2) {
                    for (char symbol : production.toCharArray()) {
                        if (terminals.contains(String.valueOf(symbol))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public void eliminateEpsilonProductions() {
        Set<String> nullable = new HashSet<>();

        for (String nonTerminal : nonTerminals) {
            for (String production : rules.get(nonTerminal)) {
                if (production.equals(EPSILON)) {
                    nullable.add(nonTerminal);
                }
            }
        }

        boolean changes = true;
        while (changes) {
            changes = false;
            for (String nonTerminal : nonTerminals) {
                if (!nullable.contains(nonTerminal)) {
                    for (String production : rules.get(nonTerminal)) {
                        boolean allNullable = true;
                        for (char symbol : production.toCharArray()) {
                            if (!nullable.contains(String.valueOf(symbol))) {
                                allNullable = false;
                                break;
                            }
                        }
                        if (allNullable) {
                            nullable.add(nonTerminal);
                            changes = true;
                            break;
                        }
                    }
                }
            }
        }

        Map<String, List<String>> newRules = new HashMap<>();
        for (String nonTerminal : rules.keySet()) {
            List<String> newProds = new ArrayList<>();
            for (String production : rules.get(nonTerminal)) {
                if (!production.equals(EPSILON)) {
                    newProds.addAll(expandNullableProduction(production, nullable));
                }
            }
            newRules.put(nonTerminal, new ArrayList<>(new HashSet<>(newProds)));
        }
        rules = newRules;
    }

    private List<String> expandNullableProduction(String production, Set<String> nullable) {
        List<String> expansions = new ArrayList<>();
        expansions.add("");

        for (char symbol : production.toCharArray()) {
            List<String> newExpansions = new ArrayList<>();
            if (nullable.contains(String.valueOf(symbol))) {
                for (String expansion : expansions) {
                    newExpansions.add(expansion + symbol);
                    newExpansions.add(expansion);
                }
            } else {
                for (String expansion : expansions) {
                    newExpansions.add(expansion + symbol);
                }
            }
            expansions = newExpansions;
        }

        List<String> result = new ArrayList<>();
        for (String exp : expansions) {
            if (!exp.isEmpty()) {
                result.add(exp);
            }
        }
        return result;
    }

    public void eliminateRenaming() {
        boolean changes = true;
        while (changes) {
            changes = false;
            for (String nonTerminal : new ArrayList<>(nonTerminals)) {
                List<String> unitProductions = new ArrayList<>();
                for (String production : rules.get(nonTerminal)) {
                    if (nonTerminals.contains(production)) {
                        unitProductions.add(production);
                    }
                }

                for (String unit : unitProductions) {
                    List<String> unitProductionsRules = rules.get(unit);
                    if (unitProductionsRules != null) {
                        rules.get(nonTerminal).addAll(unitProductionsRules);
                        rules.get(nonTerminal).remove(unit);
                        rules.put(nonTerminal, new ArrayList<>(new HashSet<>(rules.get(nonTerminal))));
                        changes = true;
                    }
                }

                rules.get(nonTerminal).removeIf(p -> nonTerminals.contains(p));
            }
        }
    }

    public void eliminateInaccessibleSymbols() {
        Set<String> accessible = new HashSet<>();
        accessible.add(start);

        boolean changes = true;
        while (changes) {
            changes = false;
            Set<String> newAccessible = new HashSet<>(accessible);
            for (String nonTerminal : accessible) {
                List<String> productions = rules.get(nonTerminal);
                if (productions != null) {
                    for (String production : productions) {
                        for (char symbol : production.toCharArray()) {
                            if (nonTerminals.contains(String.valueOf(symbol)) && !accessible.contains(String.valueOf(symbol))) {
                                newAccessible.add(String.valueOf(symbol));
                                changes = true;
                            }
                        }
                    }
                }
            }
            accessible = newAccessible;
        }

        List<String> updatedNonTerminals = new ArrayList<>();
        for (String nt : nonTerminals) {
            if (accessible.contains(nt)) {
                updatedNonTerminals.add(nt);
            }
        }
        nonTerminals = updatedNonTerminals;

        Map<String, List<String>> newRules = new HashMap<>();
        for (String nt : accessible) {
            newRules.put(nt, rules.get(nt));
        }
        rules = newRules;
    }

    public void eliminateNonProductiveSymbols() {
        Set<String> productive = new HashSet<>();
        productive.add(start);

        boolean changes = true;
        while (changes) {
            changes = false;
            for (String nonTerminal : nonTerminals) {
                if (!productive.contains(nonTerminal)) {
                    for (String production : rules.get(nonTerminal)) {
                        boolean allProductive = true;
                        for (char symbol : production.toCharArray()) {
                            if (!terminals.contains(String.valueOf(symbol)) && !productive.contains(String.valueOf(symbol))) {
                                allProductive = false;
                                break;
                            }
                        }
                        if (allProductive) {
                            productive.add(nonTerminal);
                            changes = true;
                            break;
                        }
                    }
                }
            }
        }

        List<String> updatedNonTerminals = new ArrayList<>();
        for (String nt : nonTerminals) {
            if (productive.contains(nt)) {
                updatedNonTerminals.add(nt);
            }
        }
        nonTerminals = updatedNonTerminals;

        Map<String, List<String>> newRules = new HashMap<>();
        for (String nt : productive) {
            List<String> filteredProductions = new ArrayList<>();
            for (String production : rules.get(nt)) {
                boolean valid = true;
                for (char symbol : production.toCharArray()) {
                    if (!terminals.contains(String.valueOf(symbol)) && !productive.contains(String.valueOf(symbol))) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    filteredProductions.add(production);
                }
            }
            newRules.put(nt, filteredProductions);
        }
        rules = newRules;
    }

    private String createNewNonTerminal() {
        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZαβγδζηθικλμνξοπρστυφχψω";
        for (char letter : alphabet.toCharArray()) {
            if (!nonTerminals.contains(String.valueOf(letter))) {
                nonTerminals.add(String.valueOf(letter));
                return String.valueOf(letter);
            }
        }
        for (char letter : alphabet.toCharArray()) {
            for (int num = 0; num < 100; num++) {
                String newSymbol = letter + String.valueOf(num);
                if (!nonTerminals.contains(newSymbol)) {
                    nonTerminals.add(newSymbol);
                    return newSymbol;
                }
            }
        }
        throw new RuntimeException("Exhausted non-terminal symbols!");
    }

    public void toCNF(boolean printSteps) {
        if (isCNF()) {
            return;
        }

        eliminateEpsilonProductions();
        if (printSteps) {
            System.out.println("1. After eliminating epsilon productions:");
            printRules();
            System.out.println();
        }

        eliminateRenaming();
        if (printSteps) {
            System.out.println("2. After eliminating renaming productions:");
            printRules();
            System.out.println();
        }

        eliminateInaccessibleSymbols();
        if (printSteps) {
            System.out.println("3. After eliminating inaccessible symbols:");
            printRules();
            System.out.println();
        }

        eliminateNonProductiveSymbols();
        if (printSteps) {
            System.out.println("4. After eliminating non-productive symbols:");
            printRules();
            System.out.println();
        }

        Map<String, String> rhsToNonTerminal = new HashMap<>();
        Map<String, Set<String>> newRules = new HashMap<>();
        List<String> oldNonTerminals = new ArrayList<>(rules.keySet());

        for (String nonTerminal : rules.keySet()) {
            newRules.put(nonTerminal, new HashSet<>());
            for (String production : rules.get(nonTerminal)) {
                while (production.length() > 2) {
                    String firstTwo = production.substring(0, 2);
                    String newNonTerminal = rhsToNonTerminal.getOrDefault(firstTwo, null);
                    if (newNonTerminal == null) {
                        newNonTerminal = createNewNonTerminal();
                        rhsToNonTerminal.put(firstTwo, newNonTerminal);
                        newRules.put(newNonTerminal, new HashSet<>(List.of(firstTwo)));
                    }
                    production = newNonTerminal + production.substring(2);
                }
                newRules.get(nonTerminal).add(production);
            }
        }

        for (String nonTerminal : new ArrayList<>(newRules.keySet())) {
            Set<String> productions = newRules.get(nonTerminal);
            Set<String> tempProductions = new HashSet<>(productions);
            for (String production : tempProductions) {
                if (production.length() == 2) {
                    StringBuilder newProduction = new StringBuilder();
                    boolean changed = false;
                    for (char symbol : production.toCharArray()) {
                        if (terminals.contains(String.valueOf(symbol))) {
                            String newNonTerminal = rhsToNonTerminal.getOrDefault(String.valueOf(symbol), null);
                            if (newNonTerminal == null) {
                                newNonTerminal = createNewNonTerminal();
                                rhsToNonTerminal.put(String.valueOf(symbol), newNonTerminal);
                                newRules.put(newNonTerminal, new HashSet<>(List.of(String.valueOf(symbol))));
                            }
                            newProduction.append(newNonTerminal);
                            changed = true;
                        } else {
                            newProduction.append(symbol);
                        }
                    }
                    if (changed) {
                        productions.remove(production);
                        productions.add(newProduction.toString());
                    }
                }
            }
        }

        Map<String, List<String>> finalRules = new LinkedHashMap<>();
        for (String nt : oldNonTerminals) {
            finalRules.put(nt, new ArrayList<>(newRules.getOrDefault(nt, new HashSet<>())));
        }
        for (String nt : newRules.keySet()) {
            if (!finalRules.containsKey(nt)) {
                finalRules.put(nt, new ArrayList<>(newRules.get(nt)));
            }
        }
        rules = finalRules;

        if (printSteps) {
            System.out.println("5. After converting to CNF:");
            printRules();
            System.out.println();
        }
    }

    public List<String> getNonTerminals() {
        return nonTerminals;
    }

    public List<String> getTerminals() {
        return terminals;
    }

    public Map<String, List<String>> getRules() {
        return rules;
    }

}
