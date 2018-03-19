package org.sakaiproject.lessonbuildertool;

import java.io.File;
import java.io.InputStream;

public interface ToolApi {
    public String loadCartridge(File f, String d, String siteId);
    public String deleteOrphanPages(String siteId);

    //Add WS version of Direct calls
    public String getLessonsInSite(String siteId);
    public String getLesson(String siteId, String lessonId);

    //Add WS Utility functions
	public String addOrUpdateItemInLesson(String siteId, String parentItemId, String itemId, int type, int sequence, String name, String html, String url, String customCss);
	public String deleteItemInLesson(String siteId, String parentItemId, String itemId);
}

