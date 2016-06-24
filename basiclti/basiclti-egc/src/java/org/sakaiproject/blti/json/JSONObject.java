package org.sakaiproject.blti.json;

/**
 * Created with IntelliJ IDEA.
 * User: mihai.popescu
 * Date: 09.01.2015
 * Time: 17:36
 */
public class JSONObject {
    private StringBuilder stringBuilder;

    public JSONObject() {
        stringBuilder = new StringBuilder();
    }

    public void start() {
        stringBuilder.append("{");
    }

    public void end() {
        if (stringBuilder.length() >= 2 && stringBuilder.substring(stringBuilder.length() - 2).equals(", ")) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        stringBuilder.append("}");
    }

    public void addItem(String name, String value) {
        stringBuilder.append("\"").append(name).append("\": \"").append(value != null ? value.replace("\"", "\\\"") : "").append("\", ");
    }

    public void addItem(String name, Boolean value) {
        stringBuilder.append("\"").append(name).append("\": \"").append(value ? "true" : "false").append("\", ");
    }

    public void addArray(String name, JSONArray value) {
        stringBuilder.append("\"").append(name).append("\": ").append(value.toString()).append(", ");
    }

    public void addItem(String key, JSONObject value) {
        stringBuilder.append("\"").append(key).append("\": ").append(value.toString()).append(", ");
    }

    public void addArray(String name, String value) {
        stringBuilder.append("\"").append(name).append("\": ").append(value).append(", ");
    }

    public void addArray(String name, String value, Boolean answersList) {
        String newValue;
        if (value.indexOf("style=\"") != -1) {
            newValue = value.replaceAll("style=\"", "style=\\\\\"");
            newValue = newValue.replaceAll("\">", "\\\\\">");
        } else
            newValue = value;

        stringBuilder.append("\"").append(name).append("\": ").append(newValue).append(", ");
    }

    public String toString() {
        return stringBuilder.toString();
    }
}
