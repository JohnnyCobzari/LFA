package Lab6;

import java.util.*;

abstract class JsonValue {
    abstract void toDot(StringBuilder sb, String parentId, int[] nodeId);
}

class JsonObject extends JsonValue {
    Map<String, JsonValue> members = new LinkedHashMap<>();

    @Override
    void toDot(StringBuilder sb, String parentId, int[] nodeId) {
        String thisId = "node" + nodeId[0]++;
        sb.append(thisId).append(" [label=\"Object\"];\n");
        if (parentId != null) sb.append(parentId).append(" -> ").append(thisId).append(";\n");

        for (Map.Entry<String, JsonValue> entry : members.entrySet()) {
            String keyId = "node" + nodeId[0]++;
            sb.append(keyId).append(" [label=\"").append(entry.getKey()).append("\"];\n");
            sb.append(thisId).append(" -> ").append(keyId).append(";\n");
            entry.getValue().toDot(sb, keyId, nodeId);
        }
    }
}


class JsonArray extends JsonValue {
    List<JsonValue> elements = new ArrayList<>();

    @Override
    void toDot(StringBuilder sb, String parentId, int[] nodeId) {
        String thisId = "node" + nodeId[0]++;
        sb.append(thisId).append(" [label=\"Array\"];\n");
        if (parentId != null) sb.append(parentId).append(" -> ").append(thisId).append(";\n");

        for (JsonValue val : elements) {
            val.toDot(sb, thisId, nodeId);
        }
    }
}


class JsonString extends JsonValue {
    String value;
    JsonString(String value) { this.value = value; }

    @Override
    void toDot(StringBuilder sb, String parentId, int[] nodeId) {
        String thisId = "node" + nodeId[0]++;
        sb.append(thisId).append(" [label=\"String: ").append(value).append("\"];\n");
        if (parentId != null) sb.append(parentId).append(" -> ").append(thisId).append(";\n");
    }
}

class JsonNumber extends JsonValue {
    double value;
    JsonNumber(String value) { this.value = Double.parseDouble(value); }

    @Override
    void toDot(StringBuilder sb, String parentId, int[] nodeId) {
        String thisId = "node" + nodeId[0]++;
        sb.append(thisId).append(" [label=\"Number: ").append(value).append("\"];\n");
        if (parentId != null) sb.append(parentId).append(" -> ").append(thisId).append(";\n");
    }
}

class JsonBoolean extends JsonValue {
    boolean value;
    JsonBoolean(String value) { this.value = Boolean.parseBoolean(value); }

    @Override
    void toDot(StringBuilder sb, String parentId, int[] nodeId) {
        String thisId = "node" + nodeId[0]++;
        sb.append(thisId).append(" [label=\"Boolean: ").append(value).append("\"];\n");
        if (parentId != null) sb.append(parentId).append(" -> ").append(thisId).append(";\n");
    }
}

class JsonNull extends JsonValue {
    @Override
    void toDot(StringBuilder sb, String parentId, int[] nodeId) {
        String thisId = "node" + nodeId[0]++;
        sb.append(thisId).append(" [label=\"Null\"];\n");
        if (parentId != null) sb.append(parentId).append(" -> ").append(thisId).append(";\n");
    }
}

