package org.sakaiproject.announcement.tool;

import java.lang.reflect.Method;

public class CommonsManagerWrapper {

    private Object commonsManagerObject;
    private Method deletePostMethod;
    private Method getPostMethod;
    private Method savePostMethod;

    public CommonsManagerWrapper(Object commonsManagerObject) {

        if (commonsManagerObject != null) {
            this.commonsManagerObject = commonsManagerObject;

            try {
                Class clazz = commonsManagerObject.getClass();
                getPostMethod = clazz.getMethod("getPost", new Class[] {String.class, boolean.class});
                Class postClass = Class.forName("org.sakaiproject.commons.api.datamodel.Post");
                savePostMethod = clazz.getMethod("savePost", new Class[] {postClass});
                deletePostMethod = clazz.getMethod("deletePost", new Class[] {String.class});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean deletePost(String postId) {

        try {
            return (Boolean) deletePostMethod.invoke(commonsManagerObject, new Object[] {postId});
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public CommonsPostWrapper getPost(String postId) {

        try {
            return new CommonsPostWrapper(getPostMethod.invoke(commonsManagerObject, new Object[] {postId}));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CommonsPostWrapper savePost(CommonsPostWrapper postWrapper) {

        try {
            return new CommonsPostWrapper(savePostMethod.invoke(commonsManagerObject
                                                                    , new Object[] {postWrapper.getObject()}));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isNull() {
        return commonsManagerObject == null;
    }
}
