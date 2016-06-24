package org.sakaiproject.blti.json;


import java.util.List;
import java.util.Map;

public class AdvancedJSONObject {
    protected StringBuilder stringBuilder;

    public AdvancedJSONObject(List<Map<String, String>> property) {
        stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        for (Map<String, String> map : property) {
            for (String key : map.keySet()) {
                stringBuilder.append("\"").append(key).append("\": \"").append(map.get(key).replaceAll("\"", "\\\"")).append("\"");
                stringBuilder.append(",");
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        stringBuilder.append("}");
    }

    public String toString() {
        return stringBuilder.toString();
    }
}
