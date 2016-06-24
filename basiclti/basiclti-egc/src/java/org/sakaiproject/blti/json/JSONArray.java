package org.sakaiproject.blti.json;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mihai.popescu
 * Date: 09.01.2015
 * Time: 17:36
 */
public class JSONArray {
    protected StringBuilder stringBuilder;

    public JSONArray() {
        stringBuilder = new StringBuilder();
    }

    public JSONArray(String[] values) {
        stringBuilder = new StringBuilder();
        start();
        for (String value : values) {
            stringBuilder.append("\"").append(value != null ? value.replace("\"", "\\\"") : "").append("\"").append(", ");
        }
        end();
    }

    public JSONArray(List<String> values) {
        stringBuilder = new StringBuilder();
        start();
        for (String value : values) {
            stringBuilder.append("\"").append(value != null ? value.replace("\"", "\\\"") : "").append("\"").append(", ");
        }
        end();
    }



    public void start() {
        stringBuilder.append("[");
    }

    public void end() {
        if (stringBuilder.length() >= 2 && stringBuilder.substring(stringBuilder.length() - 2).equals(", ")) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        stringBuilder.append("]");
    }

    public void addJSONObject(JSONObject object) {
        stringBuilder.append(object.toString()).append(", ");
    }

    public void addJSONObject(SimpleJSONObject object) {
        stringBuilder.append(object.toString()).append(", ");
    }
    public void addJSONObject(AdvancedJSONObject object) {
        stringBuilder.append(object.toString()).append(", ");
    }

    public String toString() {
        return stringBuilder.toString();
    }
}
