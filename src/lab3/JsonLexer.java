package lab3;

import java.util.*;
import java.util.regex.*;

public class JsonLexer {
    public enum TokenType {
        LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET, COLON, COMMA,
        STRING, NUMBER, BOOLEAN, NULL, UNKNOWN_STRING
    }

    public static class Token {
        public TokenType type;
        public String value;
        public int position;

        public Token(TokenType type, String value, int position) {
            this.type = type;
            this.value = value;
            this.position = position;
        }

        @Override
        public String toString() {
            return String.format("Token(%s, %s, %d)", type, value, position);
        }
    }

    private final String input;
    private int position = 0;

    public JsonLexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (position < input.length()) {
            char currentChar = input.charAt(position);

            if (Character.isWhitespace(currentChar)) {
                position++;
                continue;
            }

            if (matchToken(tokens, "\\{", TokenType.LEFT_BRACE)) continue;
            if (matchToken(tokens, "\\}", TokenType.RIGHT_BRACE)) continue;
            if (matchToken(tokens, "\\[", TokenType.LEFT_BRACKET)) continue;
            if (matchToken(tokens, "\\]", TokenType.RIGHT_BRACKET)) continue;
            if (matchToken(tokens, ":", TokenType.COLON)) continue;
            if (matchToken(tokens, ",", TokenType.COMMA)) continue;
            if (matchString(tokens)) continue;
            if (matchNumber(tokens)) continue;
            if (matchBoolean(tokens)) continue;
            if (matchNull(tokens)) continue;
            if (matchUnknownString(tokens)) continue;

            // Handle unknown characters
            System.err.printf("Illegal character '%c' at position %d%n", currentChar, position);
            position++;
        }
        return tokens;
    }

    private boolean matchToken(List<Token> tokens, String regex, TokenType type) {
        Matcher matcher = Pattern.compile(regex).matcher(input.substring(position));
        if (matcher.lookingAt()) {
            tokens.add(new Token(type, matcher.group(), position));
            position += matcher.end();
            return true;
        }
        return false;
    }

    private boolean matchString(List<Token> tokens) {
        Matcher matcher = Pattern.compile("\"(?:\\\\\"|[^\"])*?\"").matcher(input.substring(position));
        if (matcher.lookingAt()) {
            tokens.add(new Token(TokenType.STRING, matcher.group(), position));
            position += matcher.end();
            return true;
        }
        return false;
    }

    private boolean matchNumber(List<Token> tokens) {
        Matcher matcher = Pattern.compile("-?\\d+(\\.\\d+)?").matcher(input.substring(position));
        if (matcher.lookingAt()) {
            tokens.add(new Token(TokenType.NUMBER, matcher.group(), position));
            position += matcher.end();
            return true;
        }
        return false;
    }

    private boolean matchBoolean(List<Token> tokens) {
        Matcher matcher = Pattern.compile("true|false").matcher(input.substring(position));
        if (matcher.lookingAt()) {
            tokens.add(new Token(TokenType.BOOLEAN, matcher.group(), position));
            position += matcher.end();
            return true;
        }
        return false;
    }

    private boolean matchNull(List<Token> tokens) {
        Matcher matcher = Pattern.compile("null").matcher(input.substring(position));
        if (matcher.lookingAt()) {
            tokens.add(new Token(TokenType.NULL, matcher.group(), position));
            position += matcher.end();
            return true;
        }
        return false;
    }
    private boolean matchUnknownString(List<Token> tokens) {

        Matcher matcher = Pattern.compile("\"[^\"]+\"").matcher(input.substring(position));

        if (matcher.lookingAt()) {
            tokens.add(new Token(TokenType.UNKNOWN_STRING, matcher.group(), position));
            position += matcher.end();
            return true;
        }
        return false;
    }

    public static void main(String[] args) {

        String input = "{ \"name\": \"Alice\", \"age\": 25, \"isStudent\": true, \"scores\": [95, 82, 88], \"address\": null }";

        JsonLexer lexer = new JsonLexer(input);
        List<Token> tokens = lexer.tokenize();

        System.out.println("Tokens:");
        tokens.forEach(System.out::println);
    }
}