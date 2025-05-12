package Lab6;

import java.util.List;

public class JsonParser {
    private final List<JsonLexer.Token> tokens;
    private int index = 0;

    public JsonParser(List<JsonLexer.Token> tokens) {
        this.tokens = tokens;
    }

    public JsonValue parse() {
        return parseValue();
    }

    private JsonValue parseValue() {
        JsonLexer.Token token = peek();

        switch (token.type) {
            case LEFT_BRACE: return parseObject();
            case LEFT_BRACKET: return parseArray();
            case STRING: return new JsonString(consume().value.replaceAll("^\"|\"$", ""));
            case NUMBER: return new JsonNumber(consume().value);
            case BOOLEAN: return new JsonBoolean(consume().value);
            case NULL: consume(); return new JsonNull();
            default:
                throw new RuntimeException("Unexpected token: " + token);
        }
    }

    private JsonObject parseObject() {
        consume(JsonLexer.TokenType.LEFT_BRACE);
        JsonObject obj = new JsonObject();

        while (!check(JsonLexer.TokenType.RIGHT_BRACE)) {
            String key = consume(JsonLexer.TokenType.STRING).value.replaceAll("^\"|\"$", "");
            consume(JsonLexer.TokenType.COLON);
            JsonValue value = parseValue();
            obj.members.put(key, value);

            if (!check(JsonLexer.TokenType.RIGHT_BRACE)) {
                consume(JsonLexer.TokenType.COMMA);
            }
        }

        consume(JsonLexer.TokenType.RIGHT_BRACE);
        return obj;
    }

    private JsonArray parseArray() {
        consume(JsonLexer.TokenType.LEFT_BRACKET);
        JsonArray arr = new JsonArray();

        while (!check(JsonLexer.TokenType.RIGHT_BRACKET)) {
            arr.elements.add(parseValue());
            if (!check(JsonLexer.TokenType.RIGHT_BRACKET)) {
                consume(JsonLexer.TokenType.COMMA);
            }
        }

        consume(JsonLexer.TokenType.RIGHT_BRACKET);
        return arr;
    }

    private JsonLexer.Token consume() {
        return tokens.get(index++);
    }

    private JsonLexer.Token consume(JsonLexer.TokenType type) {
        JsonLexer.Token token = tokens.get(index++);
        if (token.type != type) {
            throw new RuntimeException("Expected " + type + " but got " + token.type);
        }
        return token;
    }

    private boolean check(JsonLexer.TokenType type) {
        return index < tokens.size() && tokens.get(index).type == type;
    }

    private JsonLexer.Token peek() {
        return tokens.get(index);
    }
}
