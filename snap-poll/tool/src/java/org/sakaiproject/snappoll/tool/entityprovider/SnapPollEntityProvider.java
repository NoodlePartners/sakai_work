package org.sakaiproject.snappoll.tool.entityprovider;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.exception.IdUnusedException;

import org.apache.commons.lang.StringUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter @Slf4j
public class SnapPollEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Describeable, ActionsExecutable {

    public final static String ENTITY_PREFIX = "snap-poll";
    private final static String SUPPORT_EMAIL_PROP = "snappoll.supportEmailAddress";
    private final static String SUPPORT_EMAIL_SUBJECT_PROP = "snappoll.supportEmailSubject";
    private final static String SUPPORT_EMAIL_MESSAGE_TEMPLATE_PROP = "snappoll.supportEmailMessageTemplate";
    private final static String THROTTLE_HOURS_PROP = "snappoll.throttleHours";
    private final static String MOD_OF_SHOWS_IN_COURSE_PROP = "snappoll.modShowsInCourse";
    // TODO: This should not be hard coded.  What is the appropriate way to do it?
    private final static String SESSIONS_PAGE_TITLE_PROP = "snappoll.sessionsPageTitle";
    private final static String SESSIONS_PAGE_TITLE_DEFAULT = "Sessions";
    private final static String EXAM_PAGE_TITLE_PROP = "snappoll.examPageTitle";
    private final static String EXAM_PAGE_TITLE_DEFAULT = "Exam";

    // Poll show timeouts are controlled by portal.snapPollTimeout in properties. See SkinnableCharonPortal.java.

    private EmailService emailService;
    private ServerConfigurationService serverConfigurationService;
    private SqlService sqlService;
    private UserDirectoryService userDirectoryService;

    public void init() {

        if (serverConfigurationService.getBoolean("auto.ddl", true)) {
            sqlService.ddl(this.getClass().getClassLoader(), "snappoll_tables");
        }
    }

    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.TXT };
    }

    @EntityCustomAction(action = "showPollNow", viewKey = EntityView.VIEW_LIST)
    public String handleShowPollNow(EntityView view, Map<String, Object> params) {

        String userId = getCheckedUserId();

        String siteId = (String) params.get("siteId");
        String tool = (String) params.get("tool");
        String context = (String) params.get("context");

        if (StringUtils.isEmpty(siteId) || StringUtils.isEmpty(tool) || StringUtils.isEmpty(context)) {
            throw new EntityException("Bad request", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        if (log.isDebugEnabled()) {
            log.debug("handleShowPollNow('" + siteId + "','" + tool + "','" + context + "')");
        }

        // We only have an algorithm for the lessons tool, so do nothing anywhere else
        if (!tool.equals("lessons")) {
            if (log.isDebugEnabled()) {
                log.debug("tool is " + tool + ", not allowed");
            }
            return "false";
        }

        if (!canTakePolls(siteId, userId)) {
            if (log.isDebugEnabled()) {
                log.debug("user can't take polls");
            }
            return "false";
        }
        
        // Work out whether a poll has been shown to this user in the throttle period
        // or we've already shown a poll on this page
        int throttleHours = serverConfigurationService.getInt(THROTTLE_HOURS_PROP, 24);
        long throttleStart = new Date().getTime()/1000L - throttleHours*3600;
        List<Long> counts = sqlService.dbRead(
                "SELECT COUNT(*) FROM SNAP_POLL_SUBMISSION WHERE USER_ID = ? " +
                "AND (SUBMITTED_TIME > ? OR (SITE_ID = ? AND TOOL = ? AND CONTEXT = ?))"
                    , new Object[] {userId, throttleStart, siteId, tool, context}
                    , new SqlReader<Long>() {
                        public Long readSqlResultRecord(ResultSet rs){
                            try {
                                return rs.getLong(1);
                            } catch (SQLException sqle) {
                                return 0L;
                            }
                        }
                    });

        if (counts.size() == 1) {
            if (counts.get(0) > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Already shown");
                }
                return "false";
            }
        } else {
            log.error("Only one count should be returned. Something is wrong.");
        }

        // make sure that the page we're on is one of the sub-pages of "Sessions" lesson_builder_page
        // Ignore any Sessions that are exams
        String sessionsPageTitle = serverConfigurationService.getString(
                SESSIONS_PAGE_TITLE_PROP, SESSIONS_PAGE_TITLE_DEFAULT);
        String examPageTitle = serverConfigurationService.getString(
                EXAM_PAGE_TITLE_PROP, EXAM_PAGE_TITLE_DEFAULT);

        // TODO: we should find a way to cache this, as it is the same for everyone in the course,
        // every time we show a lesson
        List<String> sessionPageIds = sqlService.dbRead(
                  "SELECT p2.pageId FROM lesson_builder_pages p1 " +
                  "INNER JOIN lesson_builder_items i1 " +
                  "ON i1.pageId = p1.pageId AND i1.type=2 " +
                  "INNER JOIN lesson_builder_pages p2 " +
                  "ON p2.pageId = i1.sakaiId " +
                  "WHERE p1.siteId = ? " +
                  "AND p1.title=? " +
                  "AND p2.title NOT LIKE ? " +
                  "AND p2.title NOT LIKE ? " +
                  "AND p2.title NOT LIKE ? " +
                  "AND p2.title NOT LIKE ? " +
                  "ORDER BY i1.sequence",
                new Object[] {siteId, sessionsPageTitle,
                  examPageTitle, "% "+examPageTitle, examPageTitle+" %", "% "+examPageTitle+" %"},
                null);

        int position = -1;
        int i = 0;
        for (String spId : sessionPageIds) {
            if (log.isDebugEnabled()) {
                log.debug("checking: " + spId + " ==? " + context);
            }
            if (spId.equals(context)) {
                position = i;
                break;
            }
            i++;
        }
        if (log.isDebugEnabled()) {
            log.debug("current page was found in list of lesson pages at position: " + position);
        }
        if (position < 0) {
            return "false";
        }

        // Make a hash of the userId, and siteID, and then 
        int modShowsInCourse = serverConfigurationService.getInt(MOD_OF_SHOWS_IN_COURSE_PROP, 3);
        // Do an extra mod on the hashCode to avoid an int overflow
        int showMod = (((userId+siteId).hashCode()%modShowsInCourse)+position)%modShowsInCourse;
        
        if (log.isDebugEnabled()) {
            log.debug("showMod is " + showMod);
        }
        if (showMod == 0) {
            return "true";
        } else {
            return "false";
        }

    }

    @EntityCustomAction(action = "ignore", viewKey = EntityView.VIEW_LIST)
    public void handleIgnore(EntityView view, Map<String, Object> params) {

        String userId = getCheckedUserId();

        String siteId = (String) params.get("siteId");
        String tool = (String) params.get("tool");
        String context = (String) params.get("context");

        if (StringUtils.isEmpty(siteId) || StringUtils.isEmpty(tool) || StringUtils.isEmpty(context)) {
            throw new EntityException("Bad request", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        String id = UUID.randomUUID().toString();
        long epochSeconds = new Date().getTime()/1000L;
        boolean success = sqlService.dbWrite(
            "INSERT INTO SNAP_POLL_SUBMISSION (ID, USER_ID, SITE_ID, TOOL, CONTEXT, IGNORED, SUBMITTED_TIME) VALUES(?,?,?,?,?,?,?)"
                                , new Object[] {id, userId, siteId, tool, context, "1", epochSeconds});

        if (!success) {
            log.error("Failed to store submission.");
        }
    }

    @EntityCustomAction(action = "submitResponse", viewKey = EntityView.VIEW_NEW)
    public void handleSubmitResponse(EntityView view, Map<String, Object> params) {

        String userId = getCheckedUserId();
      
        String siteId = (String) params.get("siteId");
        String response = (String) params.get("response");
        String reason = (String) params.get("reason");
        String tool = (String) params.get("tool");
        String context = (String) params.get("context");

        if (StringUtils.isEmpty(siteId) || StringUtils.isEmpty(tool)
                || StringUtils.isEmpty(response) || StringUtils.isEmpty(context) || StringUtils.isEmpty(reason)) {
            throw new EntityException("Bad request", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        int responseInt = 0;
        try {
            responseInt = Integer.parseInt(response);
            if (responseInt < 1 || responseInt > 5) {
                throw new EntityException("Bad request. Response should be an integer from 1 to 5.", "", HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (NumberFormatException nfe) {
            throw new EntityException("Bad request. Response should be a number.", "", HttpServletResponse.SC_BAD_REQUEST);

        }

        String id = UUID.randomUUID().toString();
        long epochSeconds = new Date().getTime()/1000L;
        boolean success = sqlService.dbWrite(
            "INSERT INTO SNAP_POLL_SUBMISSION (ID, USER_ID, SITE_ID, RESPONSE, REASON, TOOL, CONTEXT, SUBMITTED_TIME) VALUES(?,?,?,?,?,?,?,?)"
                                , new Object[] {id, userId, siteId, response, reason, tool, context, epochSeconds});

        if (!success) {
            log.error("Failed to store submission.");
        }

        if (responseInt < 4) {
            String supportEmailAddress = serverConfigurationService.getString(SUPPORT_EMAIL_PROP, null);
            if (supportEmailAddress == null) {
                if (log.isInfoEnabled()) {
                    log.info(SUPPORT_EMAIL_PROP + " not configured. No email will be sent.");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Sending support email ...");
                }

                try {
                    User user = userDirectoryService.getUser(userId);
                    String from = "no-reply@" + serverConfigurationService.getServerName();
                    String displayName = user.getDisplayName();
                    String subject = serverConfigurationService.getString(SUPPORT_EMAIL_SUBJECT_PROP
                                                    , "Snappoll Response");
                    // TODO: This sort of string replacement is sort of ugly.
                    String messageTemplate = serverConfigurationService.getString(SUPPORT_EMAIL_MESSAGE_TEMPLATE_PROP
                                                    , "{0} responded to a snap poll on {1}: {2} with a {3}. Reason given: {4}");
                    String message
                        = messageTemplate
                            .replace("{0}", displayName)
                            .replace("{1}", getSiteName(siteId))
                            .replace("{2}", getPageName(tool, context))
                            .replace("{3}", response)
                            .replace("{4}", reason);
                    if (log.isDebugEnabled()) {
                        log.debug("email message is " + message);
                    }
                    emailService.send(from, supportEmailAddress, subject, message, null, user.getEmail(), null);
                } catch (UserNotDefinedException unde) {
                    log.error("Failed sending support email from snap poll. No user for user id '" + userId + "'.");
                }
            }
        }
    }

    private String getCheckedUserId() throws EntityException {

        String userId = developerHelperService.getCurrentUserId();

        if (userId == null) {
            throw new EntityException("Not logged in", "", HttpServletResponse.SC_UNAUTHORIZED);
        }

        return userId;
    }

    // Figure out if the user is allowed to take polls
    private boolean canTakePolls(String siteId, String userId) {
        Site site = null;
        try {
            site = SiteService.getSite(siteId);
        } catch (IdUnusedException ex) {
            log.error("Unused site passed to canTakePolls: " + site);
        }
        if (site==null) {
            return false;
        }
        // The site must be a course and the user must be a student
        String siteType = site.getType();
        if (log.isDebugEnabled()) {
            log.debug("siteType is " + siteType);
        }
        if (!siteType.equals("course")) {
            return false;
        }
        boolean isStudent = site.isAllowed(userId, "section.role.student");
        if (log.isDebugEnabled()) {
            log.debug("isStudent is " + isStudent);
        }
        return (isStudent);
    }
    
    // Get the name of a site by siteId
    // Cloned from samigo/samigo-services/src/java/org/sakaiproject/tool/assessment/integration/helper/integrated/AgentHelperImpl.java
    private String getSiteName(String siteId){
        String siteName="";
        try{
            siteName = SiteService.getSite(siteId).getTitle();
        }
        catch (Exception ex){
            log.warn("getSiteName : " + ex.getMessage());
            log.warn("SiteService not available.  " +
                  "This needs to be fixed if you are not running a unit test.");
        }
        return siteName;
    }

    // TODO: This can be done using the API, which will permit caching
    private String getPageName(String tool, String context) {
        if (!tool.equals("lessons")) {
            return "";
        }
        List<String> titles = sqlService.dbRead(
                "SELECT title FROM lesson_builder_pages WHERE pageId = ?",
                new Object[] {context},
                null);
        if (titles.size() == 1) {
            return titles.get(0);
        }
        log.error("Only one title should be returned. Something is wrong.");
        return "";
    }
}
