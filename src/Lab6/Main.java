package Lab6;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String input = """
{
  "person": {
    "name": "Alice",
    "age": 30,
    "contact": {
      "email": "alice@example.com",
      "phones": ["123-4567", "987-6543"]
    },
    "married": false,
    "children": [],
    "address": {
      "street": "123 Maple St",
      "city": "Wonderland",
      "zip": null
    }
  },
  "favorites": {
    "colors": ["blue", "green", "red"],
    "foods": [
      {"name": "Pizza", "vegan": false},
      {"name": "Salad", "vegan": true}
    ]
  },
  "metadata": {},
  "active": true
}
""";
        JsonLexer lexer = new JsonLexer(input);
        List<JsonLexer.Token> tokens = lexer.tokenize();

        JsonParser parser = new JsonParser(tokens);
        JsonValue ast = parser.parse();

        System.out.println("Parsed AST:");
        System.out.println(ast); // You can later add a visitor or custom print method for better formatting
        try (PrintWriter out = new PrintWriter("ast.dot")) {
            StringBuilder sb = new StringBuilder("digraph AST {\n");
            int[] nodeId = {0};
            ast.toDot(sb, null, nodeId);
            sb.append("}\n");
            out.println(sb);
            System.out.println("DOT file generated: ast.dot");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
