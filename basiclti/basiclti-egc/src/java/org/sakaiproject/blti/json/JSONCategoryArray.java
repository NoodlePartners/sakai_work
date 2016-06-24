package org.sakaiproject.blti.json;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mihai.popescu
 * Date: 09.01.2015
 * Time: 17:37
 */
public class JSONCategoryArray extends JSONArray {
    public JSONCategoryArray(/*List<Category> values*/) {
        stringBuilder = new StringBuilder();
        start();
       /* for (Category category : values) {
            stringBuilder.append("{\"title\": \"").append(category.getTitle()).append("\", \"type\": \"")
                    .append(category.getTypeAsDisplayString()).append("\"}, ");
        }*/
        end();
    }
}
