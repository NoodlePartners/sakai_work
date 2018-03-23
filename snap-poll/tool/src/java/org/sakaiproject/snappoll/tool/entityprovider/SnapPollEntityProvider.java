package org.sakaiproject.snappoll.tool.entityprovider;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
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
public class SnapPollEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Outputable, Describeable, ActionsExecutable {

    public final static String ENTITY_PREFIX = "snap-poll";
    private final static String SUPPORT_EMAIL_PROP = "snappoll.supportEmailAddress";
    private final static String SUPPORT_EMAIL_SUBJECT_PROP = "snappoll.supportEmailSubject";
    private final static String SUPPORT_EMAIL_MESSAGE_TEMPLATE_PROP = "snappoll.supportEmailMessageTemplate";
    private final static String THROTTLE_HOURS_PROP = "snappoll.throttleHours";
    private final static String MOD_OF_SHOWS_IN_COURSE_PROP = "snappoll.modShowsInCourse";
    private final static String SESSIONS_NAME_PROP = "snappoll.sessions_name";
    private final static String EXAM_PAGE_TITLE_PROP = "snappoll.examPageTitle";
    private final static String EXAM_PAGE_TITLE_DEFAULT = "Exam";
    private final static String REPORT_USER_ID_PROP = "api.user";
    private final static String LESSONS = "lessons";
    private final static String FALSE = "false";
    private final static String TRUE = "true";

    // Poll show timeouts are controlled by portal.snapPollTimeout in properties.
	// See SkinnableCharonPortal.java.

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
        return new String[] { Formats.JSON, Formats.TXT };
    }

    @EntityCustomAction(action = "showPollNow", viewKey = EntityView.VIEW_LIST)
    public String handleShowPollNow(EntityView view, Map<String, Object> params) {

        String userId = getCheckedUserId();

        String siteId = (String) params.get("siteId");
        String tool = (String) params.get("tool");
        String context = (String) params.get("context");

        log.debug("handleShowPollNow('{}', '{}', '{}')", siteId, tool, context);

        if (StringUtils.isEmpty(siteId) || StringUtils.isEmpty(tool) || StringUtils.isEmpty(context)) {
            throw new EntityException("Bad request", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        // We only have an algorithm for the lessons tool, so do nothing anywhere else
        if (!tool.equals(LESSONS)) {
            log.debug("tool is '{}', not allowed", tool);
            return FALSE;
        }

        Site site = null;
        try {
            site = SiteService.getSite(siteId);
        } catch (IdUnusedException ex) {
            log.error("Unused siteId passed to handleShowPollNow: '{}'", siteId);
        }
        if (site==null) {
            throw new EntityException("Bad request", "", HttpServletResponse.SC_BAD_REQUEST);
        }

        String sessionsName = site.getProperties().getProperty(SESSIONS_NAME_PROP);
        log.debug("sessionsName is '{}'", sessionsName);
        if(StringUtils.isBlank(sessionsName)) {
            log.debug("no sessionsName, so no poll");
            return(FALSE);
        }

        if (!canTakePolls(site, userId)) {
            log.debug("user can't take polls");
            return FALSE;
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
                log.debug("This poll already shown, or other shown within {} hours", throttleHours);
                return FALSE;
            }
        } else {
            log.error("counts.size should be 1, was {}, something is wrong.", counts.size());
            return FALSE;
        }

        // make sure that the page we're on is one of the sub-pages of "Sessions" lesson_builder_page
        // Ignore any Sessions that are exams
        // String sessionsPageTitle = serverConfigurationService.getString(
        //         SESSIONS_PAGE_TITLE_PROP, SESSIONS_PAGE_TITLE_DEFAULT);
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
                  "AND p1.title = ? " +
                  "AND p2.title NOT LIKE ? " +
                  "AND p2.title NOT LIKE ? " +
                  "AND p2.title NOT LIKE ? " +
                  "AND p2.title NOT LIKE ? " +
                  "ORDER BY i1.sequence",
                new Object[] {siteId, sessionsName,
                  examPageTitle, "% "+examPageTitle, examPageTitle+" %", "% "+examPageTitle+" %"},
                null);

        int position = -1;
        int i = 0;
        for (String spId : sessionPageIds) {
            log.debug("checking: '{}' ?== '{}'", spId, context);
            if (spId.equals(context)) {
                position = i;
                break;
            }
            i++;
        }
        log.debug("current page was found in list of lesson pages at position {}", position);
        if (position < 0) {
            return FALSE;
        }

        // Make a hash of the userId, and siteID, and then 
        int modShowsInCourse = serverConfigurationService.getInt(MOD_OF_SHOWS_IN_COURSE_PROP, 3);
        // Do an extra mod on the hashCode to avoid an int overflow
        int showMod = (((userId+siteId).hashCode()%modShowsInCourse)+position)%modShowsInCourse;
        
        log.debug("showMod is {}", showMod);
        if (showMod == 0) {
            return TRUE;
        } else {
            return FALSE;
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
                log.debug("Sending support email ...");

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
                    log.debug("email message is {}", message);
                    emailService.send(from, supportEmailAddress, subject, message, null, user.getEmail(), null);
                } catch (UserNotDefinedException unde) {
                    log.error("Failed sending support email from snap poll. No user for user id '" + userId + "'.");
                }
            }
        }
    }

    @EntityCustomAction(action = "report", viewKey = EntityView.VIEW_LIST)
    public ActionReturn handleReport(EntityView view, Map<String, Object> params) {

        String reportUserId = serverConfigurationService.getString(REPORT_USER_ID_PROP, null);

        if (reportUserId == null) {
            log.warn(REPORT_USER_ID_PROP + " is not defined. Aborting the report ...");
            throw new EntityException("Report user not defined in properties.", "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        String userId = getCheckedUserId();

        String userEid = null;
        try {
            userEid = userDirectoryService.getUser(userId).getEid();
        } catch (UserNotDefinedException unde) { /* Impossible? Should be. We've just checked the current user.*/ }

        if (!userEid.equals(reportUserId)) {
            log.warn("Current user is not the report user. Aborting the report ...");
            throw new EntityException("Not logged in as report user", "", HttpServletResponse.SC_UNAUTHORIZED);
        }

        String siteIdsString = (String) params.get("siteIds");

        boolean bySite = !StringUtils.isEmpty(siteIdsString);
        boolean includeIgnored = TRUE.equals(((String) params.get("includeIgnored")));

        List<SnapPollSubmission> submissions = new ArrayList();

        if (bySite) {
            String siteIds
                = Stream.of(siteIdsString.split(",")).map(id -> "'" + id + "'").collect(Collectors.joining(","));

            StringBuilder sb = new StringBuilder("SELECT * FROM SNAP_POLL_SUBMISSION WHERE ");

            if (!includeIgnored) {
                sb.append("IGNORED IS NULL AND ");
            }

            sb.append("SITE_ID IN (").append(siteIds).append(")");
            submissions = sqlService.dbRead(
                    sb.toString()
                    , new Object[] {}
                    , new SqlReader<SnapPollSubmission>() {

                        public SnapPollSubmission readSqlResultRecord(ResultSet rs) {
                            try {
                                return new SnapPollSubmission(rs);
                            } catch (SQLException sqle) {
                                log.error("Error reading SnapPollSubmission:", sqle);
                                return null;
                            }
                        }
                    });
        }

        for (SnapPollSubmission submission : submissions) {
            if (submission.tool.equals(LESSONS)) {
                submission.title = getPageName(submission.tool, submission.context);
            }
        }

        return new ActionReturn(submissions);
    }

    private String getCheckedUserId() throws EntityException {

        String userId = developerHelperService.getCurrentUserId();

        if (userId == null) {
            throw new EntityException("Not logged in", "", HttpServletResponse.SC_UNAUTHORIZED);
        }

        return userId;
    }

    // Figure out if the user is allowed to take polls
    private boolean canTakePolls(Site site, String userId) {
        // The site must be a course and the user must be a student
        String siteType = site.getType();
        log.debug("siteType is {}", siteType);
        if (!siteType.equals("course")) {
            return false;
        }
        boolean isStudent = site.isAllowed(userId, "section.role.student");
        log.debug("isStudent is {}", isStudent);
        return (isStudent);
    }
    
    // Get the name of a site by siteId
    // Cloned from samigo/samigo-services/src/java/org/sakaiproject/tool/assessment/integration/helper/integrated/AgentHelperImpl.java
    private String getSiteName(String siteId) {
        String siteName="";
        try {
            siteName = SiteService.getSite(siteId).getTitle();
        }
        catch (Exception ex) {
            log.warn("getSiteName : " + ex.getMessage());
            log.warn("SiteService not available.  " +
                  "This needs to be fixed if you are not running a unit test.");
        }
        return siteName;
    }

    // TODO: This can be done using the API, which will permit caching
    private String getPageName(String tool, String context) {
        if (!tool.equals(LESSONS)) {
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

    public class SnapPollSubmission {

        public String id = "";
        public String userId = "";
        public String siteId = "";
        public String response = "";
        public String reason = "";
        public String tool = "";
        public String context = "";
        public String ignored = "";
        public long pollDate = 0L;
        public String title = "";

        public SnapPollSubmission(ResultSet rs) throws SQLException {

            id = rs.getString("ID");
            userId = rs.getString("USER_ID");
            siteId = rs.getString("SITE_ID");
            reason = rs.getString("REASON");
            response = rs.getString("RESPONSE");
            tool = rs.getString("TOOL");
            context = rs.getString("CONTEXT");
            ignored = rs.getString("IGNORED");
            pollDate = rs.getLong("SUBMITTED_TIME");
        }
    }


}
