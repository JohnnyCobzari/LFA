package lab5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TestGrammar {
    private Grammar grammar;
    private static final String EPSILON = "ε";

    @BeforeEach
    void setUp() {
        List<String> nonTerminals = Arrays.asList("S", "A", "B", "C");
        List<String> terminals = Arrays.asList("a", "d");

        Map<String, List<String>> rules = new HashMap<>();
        rules.put("S", Arrays.asList("dB", "A"));
        rules.put("A", Arrays.asList("d", "dS", "aAdAB"));
        rules.put("B", Arrays.asList("a", "aS","A",EPSILON));
        rules.put("C", Arrays.asList("Aa"));

        grammar = new Grammar(nonTerminals, terminals, rules, "S");
    }

    @Test
    void testInitialGrammar() {
        assertEquals(Arrays.asList("S", "A", "B", "C", "E"), grammar.getNonTerminals());
        assertEquals(Arrays.asList("a", "b"), grammar.getTerminals());
        assertTrue(grammar.getRules().containsKey("S"));
        assertTrue(grammar.getRules().containsKey("A"));
        assertTrue(grammar.getRules().containsKey("B"));
        assertTrue(grammar.getRules().containsKey("C"));
        assertTrue(grammar.getRules().containsKey("E"));
    }

    @Test
    void testEliminateEpsilonProductions() {
        grammar.eliminateEpsilonProductions();
        assertFalse(grammar.getRules().get("C").contains(EPSILON));
    }

    @Test
    void testEliminateRenamingProductions() {
        grammar.eliminateEpsilonProductions();
        grammar.eliminateRenaming();
        for (List<String> productions : grammar.getRules().values()) {
            for (String production : productions) {
                assertFalse(grammar.getNonTerminals().contains(production));
            }
        }
    }

    @Test
    void testEliminateInaccessibleSymbols() {
        grammar.eliminateEpsilonProductions();
        grammar.eliminateRenaming();
        grammar.eliminateInaccessibleSymbols();
        for (String nt : grammar.getNonTerminals()) {
            assertTrue(grammar.getRules().containsKey(nt));
        }
    }

    @Test
    void testEliminateNonProductiveSymbols() {
        grammar.eliminateEpsilonProductions();
        grammar.eliminateRenaming();
        grammar.eliminateInaccessibleSymbols();
        grammar.eliminateNonProductiveSymbols();
        for (List<String> productions : grammar.getRules().values()) {
            for (String production : productions) {
                for (char symbol : production.toCharArray()) {
                    assertTrue(grammar.getTerminals().contains(String.valueOf(symbol))
                            || grammar.getNonTerminals().contains(String.valueOf(symbol)));
                }
            }
        }
    }

    @Test
    void testIsCNF() {
        assertFalse(grammar.isCNF());
        grammar.toCNF(false);
        assertTrue(grammar.isCNF());
    }

    @Test
    void testToCNF() {
        grammar.toCNF(false);
        for (Map.Entry<String, List<String>> entry : grammar.getRules().entrySet()) {
            for (String production : entry.getValue()) {
                assertTrue(production.length() <= 2);
                if (production.length() == 2) {
                    for (char symbol : production.toCharArray()) {
                        assertTrue(grammar.getNonTerminals().contains(String.valueOf(symbol)));
                    }
                }
                if (production.length() == 1) {
                    assertTrue(grammar.getTerminals().contains(production) || production.equals(EPSILON));
                }
            }
        }
    }
}
