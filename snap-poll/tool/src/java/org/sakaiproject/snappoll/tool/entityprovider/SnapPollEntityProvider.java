package org.sakaiproject.snappoll.tool.entityprovider;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ServerConfigurationService;
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
    private final static String MAX_SHOWS_PER_COURSE_PROP = "snappoll.maxShowsPerCourse";

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

        // Count shows for this user and site/course.
        List<String> ids = sqlService.dbRead(
                "SELECT ID FROM SNAP_POLL_SUBMISSION WHERE USER_ID = '" + userId + "' AND SITE_ID = '" + siteId + "'");

        int numberOfShowsForCourse = ids.size();

        if (log.isDebugEnabled()) {
            log.debug("Number shows for this user and site/course: " + numberOfShowsForCourse);
        }

        int maxShowsPerCourse = serverConfigurationService.getInt(MAX_SHOWS_PER_COURSE_PROP, 4);

        if (numberOfShowsForCourse >= maxShowsPerCourse) {
            return "false";
        }

        // Work out whether a poll has been shown to this user in the throttle period
        List<Long> times = sqlService.dbRead(
                "SELECT MAX(SUBMITTED_TIME) FROM SNAP_POLL_SUBMISSION WHERE USER_ID = ?"
                    , new Object[] {userId}
                    , new SqlReader<Long>() {
                        public Long readSqlResultRecord(ResultSet rs){
                            try {
                                return rs.getLong(1);
                            } catch (SQLException sqle) {
                                return 0L;
                            }
                        }
                    });

        if (times.size() == 1) {
            Long lastShow = times.get(0);
            if (lastShow > 0L) {
                int throttleHours = serverConfigurationService.getInt(THROTTLE_HOURS_PROP, 24);
                if (lastShow > (new Date().getTime() - throttleHours*3600000) ) {
                    return "false";
                }
            }
        } else {
            log.warn("Only one max time should be returned. Something is wrong.");
        }

        // Now count shows for this tool and context, too.
        ids = sqlService.dbRead(
                "SELECT ID FROM SNAP_POLL_SUBMISSION WHERE USER_ID = '" + userId
                    + "' AND SITE_ID = '" + siteId
                    + "' AND TOOL = '" + tool
                    + "' AND CONTEXT = '" + context + "'");

        int numberOfShowsForToolContext = ids.size();

        if (log.isDebugEnabled()) {
            log.debug("Number shows for this tool and context: " + numberOfShowsForToolContext);
        }

        if (numberOfShowsForToolContext == 0) {
            // No poll has been shown for this site-tool-context.
            if (Math.random() <= 0.5D) {
                return "true";
            } else {
                return "false";
            }
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
        long epochSeconds = new Date().getTime()/1000;
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
        long epochSeconds = new Date().getTime()/1000;
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
                    String messageTemplate = serverConfigurationService.getString(SUPPORT_EMAIL_MESSAGE_TEMPLATE_PROP
                                                    , "{0} responded to a snap poll with a {1}. Reason given: {2}");
                    String message
                        = messageTemplate.replace("{0}", displayName).replace("{1}", response).replace("{2}", reason);
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
}
