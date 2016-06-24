package org.sakaiproject.blti.json;

public class SimpleJSONObject {
    protected StringBuilder stringBuilder;

    public SimpleJSONObject(String key, String value) {
        stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append("\"").append(key).append("\": \"").append(value.replaceAll("\"", "\\\"")).append("\"");
        stringBuilder.append("}");
    }

    public String toString() {
        return stringBuilder.toString();
    }
}
