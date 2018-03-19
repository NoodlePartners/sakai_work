/**
 * Copyright (c) 2005 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;


import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class LessonBuilder extends AbstractWebService {

    /**
     * Key in the ThreadLocalManager for binding our current placement.
     */
    protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";

    /**
     * Key in the ThreadLocalManager for binding our current tool.
     */
    protected final static String CURRENT_TOOL = "sakai:ToolComponent:current.tool";

    private static Logger LOG = LoggerFactory.getLogger(MessageForums.class);

    /**
     * deletes orphan pages for a site
     * @param sessionid the session to use
     * @param context   the context to use
     * @return the sessionid if active, or "null" if not.
     */
    @WebMethod
    @Path("/deleteOrphanPages")
    @Produces("text/plain")
    @GET
    public String deleteOrphanPages(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "context", partName = "context") @QueryParam("context") String context) {
        Session session = establishSession(sessionid);


        // Wrap this in a big try / catch block so we get better feedback
        // in the logs in the case of an error
        try {
            Site site = siteService.getSite(context);
            // If not admin, check maintainer membership in the source site
            if (!securityService.isSuperUser(session.getUserId()) &&
                    !securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference())) {
                LOG.warn("WS copySite(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
                throw new RuntimeException("WS copySite(): Permission denied. Must be super user to copy a site in which you are not a maintainer.");
            }

            ToolConfiguration tool = site.getToolForCommonId("sakai.lessonbuildertool");

            if (tool == null) {
                return "Tool sakai.lessonbuildertool NOT found in site=" + context;
            }
            // Lets go down and hack our essence into the thread
            ToolSession toolSession = session.getToolSession(tool.getId());
            sessionManager.setCurrentToolSession(toolSession);
            threadLocalManager.set(CURRENT_PLACEMENT, tool);
            threadLocalManager.set(CURRENT_TOOL, tool.getTool());
            return lessonBuilderAccessAPI.deleteOrphanPages(site.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Failure";
    }

    /**
     * lists all lessons for a site
     * @param sessionid the session to use
     * @param siteId    the siteId to use
     * @return the string representation of the lessons within the site in XML
     */
    @WebMethod
    @Path("/getLessonsInSite")
    @Produces("text/plain")
    @GET
    public String getLessonsInSite(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteId", partName = "siteId") @QueryParam("siteId") String siteId) {
        Session session = establishSession(sessionid);

        // Wrap this in a big try / catch block so we get better feedback
        // in the logs in the case of an error
        try {
            Site site = siteService.getSite(siteId);
            if( site == null ) {
                LOG.warn("WS getLessonsInSite(): Site not found.");
                throw new RuntimeException("WS getLessonsInSite(): Site not found.");
            }

            // If not admin, check maintainer membership in the source site
    		if (!securityService.isSuperUser(session.getUserId()) && !securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference()))
    		{
                LOG.warn("WS getLessonsInSite(): Permission denied. Must be super user to getLessonsInSite as part of a site in which you are not a maintainer.");
                throw new RuntimeException("WS getLessonsInSite(): Permission denied. Must be super user to getLessonsInSite as part of a site in which you are not a maintainer.");
            }

            ToolConfiguration tool = site.getToolForCommonId("sakai.lessonbuildertool");

            if (tool == null) {
                return "Tool sakai.lessonbuildertool NOT found in site=" + siteId;
            }

            // Lets go down and hack our essence into the thread
            ToolSession toolSession = session.getToolSession(tool.getId());
            sessionManager.setCurrentToolSession(toolSession);
            threadLocalManager.set(CURRENT_PLACEMENT, tool);
            threadLocalManager.set(CURRENT_TOOL, tool.getTool());

            LOG.info("Before getLessonsInSite");
            return lessonBuilderAccessAPI.getLessonsInSite(siteId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Failure";
    }

    /** list details of a lesson
     * @param sessionid the session to use
     * @param siteId    the siteId to use
     * @param lessonId  the lessonId to use
     * @return the string representation of the items in the lesson XML
     */
    @WebMethod
    @Path("/getLesson")
    @Produces("text/plain")
    @GET
    public String getLesson(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteId", partName = "siteId") @QueryParam("siteId") String siteId,
            @WebParam(name = "lessonId", partName = "lessonId") @QueryParam("lessonId") String lessonId) {
        Session session = establishSession(sessionid);

        // Wrap this in a big try / catch block so we get better feedback
        // in the logs in the case of an error
        try {
            Site site = siteService.getSite(siteId);

            if( site == null ) {
                LOG.warn("WS getLesson(): Site not found.");
                throw new RuntimeException("WS getLesson(): Site not found.");
            }

            // If not admin, check maintainer membership in the source site
    		if (!securityService.isSuperUser(session.getUserId()) && !securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference()))
    		{
                LOG.warn("WS getLesson(): Permission denied. Must be super user to getLesson as part of a site in which you are not a maintainer.");
                throw new RuntimeException("WS getLesson(): Permission denied. Must be super user to getLesson as part of a site in which you are not a maintainer.");
            }

            ToolConfiguration tool = site.getToolForCommonId("sakai.lessonbuildertool");

            if (tool == null) {
                return "Tool sakai.lessonbuildertool NOT found in site=" + siteId;
            }

            // Lets go down and hack our essence into the thread
            ToolSession toolSession = session.getToolSession(tool.getId());
            sessionManager.setCurrentToolSession(toolSession);
            threadLocalManager.set(CURRENT_PLACEMENT, tool);
            threadLocalManager.set(CURRENT_TOOL, tool.getTool());

            return lessonBuilderAccessAPI.getLesson(siteId, lessonId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Failure";
    }

    /** add or update an item on a lesson page
     * @param sessionid the session to use
     * @param siteId    the siteId to use
     * @param parentItemId  the parentItemId of the parent to use
     * @param itemId    the item to use
     * @param type      the type of item to create (see SimplePageItem for types)
     * @param sequence  the sequence of the item on the page
     * @param name      the new name of the item
     * @param htmlOrUrl the html or url to put in the item
     * @return success and the itemid or failure string
     */
    @WebMethod
    @Path("/addOrUpdateItemInLesson")
    @Produces("text/plain")
    @GET
    public String addOrUpdateItemInLesson(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteId", partName = "siteId") @QueryParam("siteId") String siteId,
            @WebParam(name = "parentItemId", partName = "parentItemId") @QueryParam("parentItemId") String parentItemId,
            @WebParam(name = "itemId", partName = "itemId") @QueryParam("itemId") String itemId,
            @WebParam(name = "type", partName = "type") @QueryParam("type") int type,
            @WebParam(name = "sequence", partName = "sequence") @QueryParam("sequence") int sequence,
            @WebParam(name = "name", partName = "name") @QueryParam("name") String name,
            @WebParam(name = "html", partName = "html") @QueryParam("html") String html,
            @WebParam(name = "url", partName = "url") @QueryParam("url") String url,
            @WebParam(name = "customCss", partName = "customCss") @QueryParam("customCss") String customCss) {
        Session session = establishSession(sessionid);

        // Wrap this in a big try / catch block so we get better feedback
        // in the logs in the case of an error
        try {
            Site site = siteService.getSite(siteId);

            if( site == null ) {
                LOG.warn("WS addOrUpdateItemInLesson(): Site not found.");
                throw new RuntimeException("WS addOrUpdateItemInLesson(): Site not found.");
            }

            // If not admin, check maintainer membership in the source site
    		if (!securityService.isSuperUser(session.getUserId()) && !securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference()))
    		{
                LOG.warn("WS addOrUpdateItemInLesson(): Permission denied. Must be super user to addOrUpdateItemInLesson as part of a site in which you are not a maintainer.");
                throw new RuntimeException("WS addOrUpdateItemInLesson(): Permission denied. Must be super user to addOrUpdateItemInLesson as part of a site in which you are not a maintainer.");
            }

            ToolConfiguration tool = site.getToolForCommonId("sakai.lessonbuildertool");

            if (tool == null) {
                return "Tool sakai.lessonbuildertool NOT found in site=" + siteId;
            }

            // Lets go down and hack our essence into the thread
            ToolSession toolSession = session.getToolSession(tool.getId());
            sessionManager.setCurrentToolSession(toolSession);
            threadLocalManager.set(CURRENT_PLACEMENT, tool);
            threadLocalManager.set(CURRENT_TOOL, tool.getTool());

            return lessonBuilderAccessAPI.addOrUpdateItemInLesson(siteId, parentItemId, itemId, type, sequence, name, html, url, customCss);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Failure";
    }

    /** delete an item on a lesson page
     * @param sessionid the session to use
     * @param siteId    the siteId to use
     * @param parentItemId  the parentItemId of the parent to use
     * @param itemId    the item to use
     * @return success or failure string
     */
    @WebMethod
    @Path("/deleteItemInLesson")
    @Produces("text/plain")
    @GET
    public String deleteItemInLesson(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "siteId", partName = "siteId") @QueryParam("siteId") String siteId,
            @WebParam(name = "parentItemId", partName = "parentItemId") @QueryParam("parentItemId") String parentItemId,
            @WebParam(name = "itemId", partName = "itemId") @QueryParam("itemId") String itemId) {
        Session session = establishSession(sessionid);

        // Wrap this in a big try / catch block so we get better feedback
        // in the logs in the case of an error
        try {
            Site site = siteService.getSite(siteId);

            if( site == null ) {
                LOG.warn("WS deleteItemInLesson(): Site not found.");
                throw new RuntimeException("WS deleteItemInLesson(): Site not found.");
            }

            // If not admin, check maintainer membership in the source site
    		if (!securityService.isSuperUser(session.getUserId()) && !securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference()))
    		{
                LOG.warn("WS deleteItemInLesson(): Permission denied. Must be super user to deleteItemInLesson as part of a site in which you are not a maintainer.");
                throw new RuntimeException("WS deleteItemInLesson(): Permission denied. Must be super user to deleteItemInLesson as part of a site in which you are not a maintainer.");
            }

            ToolConfiguration tool = site.getToolForCommonId("sakai.lessonbuildertool");

            if (tool == null) {
                return "Tool sakai.lessonbuildertool NOT found in site=" + siteId;
            }

            // Lets go down and hack our essence into the thread
            ToolSession toolSession = session.getToolSession(tool.getId());
            sessionManager.setCurrentToolSession(toolSession);
            threadLocalManager.set(CURRENT_PLACEMENT, tool);
            threadLocalManager.set(CURRENT_TOOL, tool.getTool());

            return lessonBuilderAccessAPI.deleteItemInLesson(siteId, parentItemId, itemId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Failure";
    }

}
