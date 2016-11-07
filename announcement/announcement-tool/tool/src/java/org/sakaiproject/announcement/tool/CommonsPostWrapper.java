package org.sakaiproject.announcement.tool;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class CommonsPostWrapper {

    private Object postObject;
    private static Constructor postConstructor;
    private static Method setModifiedDateMethod;
    private static Method setContentMethod;
    private static Method setCreatorIdMethod;
    private static Method setSiteIdMethod;
    private static Method setEmbedderMethod;
    private static Method setCommonsIdMethod;
    private static Method setReleaseDateMethod;
    private static Method getIdMethod;

    static {
        try {
            Class postClass = Class.forName("org.sakaiproject.commons.api.datamodel.Post");
            postConstructor = postClass.getConstructor(new Class[] {});
            setModifiedDateMethod = postClass.getMethod("setModifiedDate", new Class[] {long.class});
            setContentMethod = postClass.getMethod("setContent", new Class[] {String.class});
            setCreatorIdMethod = postClass.getMethod("setCreatorId", new Class[] {String.class});
            setSiteIdMethod = postClass.getMethod("setSiteId", new Class[] {String.class});
            setEmbedderMethod = postClass.getMethod("setEmbedder", new Class[] {String.class});
            setCommonsIdMethod = postClass.getMethod("setCommonsId", new Class[] {String.class});
            setReleaseDateMethod = postClass.getMethod("setReleaseDate", new Class[] {long.class});
            getIdMethod = postClass.getMethod("getId", new Class[] {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CommonsPostWrapper() {

        try {
            this.postObject = postConstructor.newInstance(new Object[] {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CommonsPostWrapper(Object postObject) {
        this.postObject = postObject;
    }

    public void setModifiedDate(long millis) {

        try {
            setModifiedDateMethod.invoke(postObject, new Object[] {millis});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setContent(String content) {

        if (postObject == null) {
            System.out.println("postObject is null");
        }
        if (setContentMethod == null) {
            System.out.println("setContentMethod is null");
        }

        try {
            setContentMethod.invoke(postObject, new Object[] {content});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCreatorId(String creatorId) {

        try {
            setCreatorIdMethod.invoke(postObject, new Object[] {creatorId});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSiteId(String siteId) {

        try {
            setSiteIdMethod.invoke(postObject, new Object[] {siteId});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEmbedder(String embedder) {

        try {
            setEmbedderMethod.invoke(postObject, new Object[] {embedder});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setCommonsId(String commonsId) {

        try {
            setCommonsIdMethod.invoke(postObject, new Object[] {commonsId});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setReleaseDate(long millis) {

        try {
            setReleaseDateMethod.invoke(postObject, new Object[] {millis});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getId() {

        try {
            return (String) getIdMethod.invoke(postObject, new Object[] {});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isNull() {
        return postObject == null;
    }

    public Object getObject() {
        return postObject;
    }
}
