package Lab4;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RegexGenerator {
    private final int maxIterations;
    private final int maxStarRepeat;
    private final int maxPlusRepeat;
    private final Random random;

    public RegexGenerator(int maxIterations, int maxStarRepeat, int maxPlusRepeat) {
        this.maxIterations = maxIterations;
        this.maxStarRepeat = maxStarRepeat;
        this.maxPlusRepeat = maxPlusRepeat;
        this.random = new Random();
    }

    public RegexGenerator() {
        this(1000, 5, 5);
    }

    public List<String> parseRegex(String regexStr) {
        List<String> tokens = tokenize(regexStr);
        return processTokens(tokens);
    }

    private List<String> tokenize(String regexStr) {
        List<String> tokens = new ArrayList<>();
        int i = 0;

        while (i < regexStr.length()) {
            if (regexStr.charAt(i) == '(') {
                int level = 1;
                int j = i + 1;
                while (j < regexStr.length() && level > 0) {
                    if (regexStr.charAt(j) == '(') {
                        level++;
                    } else if (regexStr.charAt(j) == ')') {
                        level--;
                    }
                    j++;
                }

                if (level == 0) {
                    String groupContent = regexStr.substring(i + 1, j - 1);

                    String repetition = "";
                    if (j < regexStr.length() && isRepetitionOperator(regexStr.charAt(j))) {
                        repetition = String.valueOf(regexStr.charAt(j));
                        j++;
                    }

                    tokens.add("(" + groupContent + ")" + repetition);
                    i = j;
                } else {
                    tokens.add(String.valueOf(regexStr.charAt(i)));
                    i++;
                }
            } else if (i < regexStr.length() - 1 && isRepetitionOperator(regexStr.charAt(i + 1))) {
                tokens.add(regexStr.substring(i, i + 2));
                i += 2;
            } else {
                tokens.add(String.valueOf(regexStr.charAt(i)));
                i++;
            }
        }

        return tokens;
    }

    private boolean isRepetitionOperator(char c) {
        return c == '*' || c == '+' || c == '?' || c == '²' || c == '³' || c == '⁴' ||
                c == '⁵' || c == '⁶' || c == '⁷' || c == '⁸' || c == '⁹';
    }

    private List<String> processTokens(List<String> tokens) {
        List<String> results = new ArrayList<>();
        results.add("");

        for (String token : tokens) {
            List<String> newResults = new ArrayList<>();

            if (token.startsWith("(") && token.endsWith(")")) {
                String groupContent = token.substring(1, token.length() - 1);
                if (groupContent.contains("|")) {
                    String[] alternatives = groupContent.split("\\|");
                    String alt = alternatives[random.nextInt(alternatives.length)];
                    List<String> altResults = processTokens(tokenize(alt));
                    for (String r : results) {
                        for (String altR : altResults) {
                            newResults.add(r + altR);
                        }
                    }
                } else {
                    List<String> groupResults = processTokens(tokenize(groupContent));
                    for (String r : results) {
                        for (String gr : groupResults) {
                            newResults.add(r + gr);
                        }
                    }
                }
            } else if (token.startsWith("(") && hasRepetitionOperator(token)) {
                char repeatOp = token.charAt(token.length() - 1);
                String groupContent = token.substring(1, token.length() - 2);

                int repeatCount;
                if (repeatOp == '*') {
                    repeatCount = random.nextInt(maxStarRepeat + 1);
                } else if (repeatOp == '+') {
                    repeatCount = random.nextInt(maxPlusRepeat) + 1;
                } else if (repeatOp == '?') {
                    repeatCount = random.nextInt(2);
                } else {
                    repeatCount = switch (repeatOp) {
                        case '²' -> 2;
                        case '³' -> 3;
                        case '⁴' -> 4;
                        case '⁵' -> 5;
                        case '⁶' -> 6;
                        case '⁷' -> 7;
                        case '⁸' -> 8;
                        case '⁹' -> 9;
                        default -> 0;
                    };
                }

                if (groupContent.contains("|")) {
                    String[] alternatives = groupContent.split("\\|");
                    String choice = alternatives[random.nextInt(alternatives.length)];
                    List<String> groupResults = processTokens(tokenize(choice));

                    for (String r : results) {
                        StringBuilder temp = new StringBuilder(r);
                        for (int i = 0; i < repeatCount; i++) {
                            if (!groupResults.isEmpty()) {
                                temp.append(groupResults.get(0));
                            }
                        }
                        newResults.add(temp.toString());
                    }
                } else {
                    List<String> groupResults = processTokens(tokenize(groupContent));
                    for (String r : results) {
                        StringBuilder temp = new StringBuilder(r);
                        for (int i = 0; i < repeatCount; i++) {
                            if (!groupResults.isEmpty()) {
                                temp.append(groupResults.get(0));
                            }
                        }
                        newResults.add(temp.toString());
                    }
                }
            } else if (token.length() == 2 && isRepetitionOperator(token.charAt(1))) {
                char c = token.charAt(0);
                char op = token.charAt(1);
                int repeatCount;
                if (op == '²') repeatCount = 2;
                else if (op == '³') repeatCount = 3;
                else if (op == '⁴') repeatCount = 4;
                else if (op == '⁵') repeatCount = 5;
                else if (op == '⁶') repeatCount = 6;
                else if (op == '⁷') repeatCount = 7;
                else if (op == '⁸') repeatCount = 8;
                else if (op == '⁹') repeatCount = 9;
                else if (op == '*') repeatCount = random.nextInt(maxStarRepeat + 1);
                else if (op == '+') repeatCount = random.nextInt(maxPlusRepeat) + 1;
                else repeatCount = random.nextInt(2); // for '?'

                for (String r : results) {
                    newResults.add(r + String.valueOf(c).repeat(repeatCount));
                }
            } else if (token.equals("μ")) {
                newResults = new ArrayList<>(results);
            } else {
                for (String r : results) {
                    newResults.add(r + token);
                }
            }

            results = newResults;
        }

        return results;
    }

    private boolean hasRepetitionOperator(String token) {
        if (token.length() < 2) return false;
        char lastChar = token.charAt(token.length() - 1);
        return isRepetitionOperator(lastChar);
    }

    public List<String> generate(List<String> regexPatterns, int count) {
        List<String> allResults = new ArrayList<>();

        for (String pattern : regexPatterns) {
            List<String> patternStrings = new ArrayList<>();
            int iterations = 0;

            while (patternStrings.size() < count && iterations < maxIterations) {
                List<String> generated = parseRegex(pattern);
                if (!generated.isEmpty()) {
                    patternStrings.add(generated.get(0));
                }
                iterations++;
            }

            System.out.println("Pattern: " + pattern);
            System.out.println("  " + patternStrings);
            allResults.addAll(patternStrings);
        }

        return allResults;
    }

    public static void main(String[] args) {
        RegexGenerator generator = new RegexGenerator(1000, 3, 4);

        List<String> patterns = List.of(
                "M?N²(O|P)³Q*R+",
                "(X|Y|Z)³8+(9|0)",
                "(H|i)(J|K)L*N?"
        );

        List<String> validStrings = generator.generate(patterns, 3);
    }
}