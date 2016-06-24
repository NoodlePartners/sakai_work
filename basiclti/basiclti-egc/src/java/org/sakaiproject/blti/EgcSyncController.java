package org.sakaiproject.blti;


import net.fortuna.ical4j.model.Dur;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sakaiproject.assignment.impl.BaseAssignmentService;
import org.sakaiproject.assignment.impl.DbAssignmentService;
import org.sakaiproject.blti.data.AssessmentInfo;
import org.sakaiproject.calendar.api.*;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService;
import org.sakaiproject.calendar.cover.*;
import org.sakaiproject.calendaring.api.ExternalCalendaringService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.spring.IntegrationContext;
import org.sakaiproject.tool.assessment.integration.helper.integrated.CalendarServiceHelperImpl;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.foorm.SakaiFoorm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

/**
 * Created by Stanislav_Aytuganov on 11.10.2015.
 */
public class EgcSyncController {

    private static Log M_log = LogFactory.getLog(EgcSyncDataServlet.class);
    @Autowired
    private static ExternalCalendaringService externalCalendaringService;
    public static final String AC_CALENDAR_EVENT = "Adobe Connect Meeting";

    public static String getSites() {
        List<Site> sites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ANY, null, null, null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);
        String retval = "";
        M_log.info(sites);
        for (Site site : sites) {
            retval += "Title: " + site.getTitle();
            retval += " | ID: " + site.getId();
            retval += " | Description: " + site.getDescription();
            retval += "\n";
        }
        M_log.info("retval:");
        M_log.info(retval);
        return retval;
    }

    public static String getAssessments(String siteId) {
        M_log.info("siteId:");
        M_log.info(siteId);

        Site site = null;

        if (null != siteId) {
            try {
                site = SiteService.getSite(siteId);
            } catch (IdUnusedException e) {
                e.printStackTrace();
            }
        }

        /*
            PublishedAssessmentService service = new PublishedAssessmentService();
            ArrayList<PublishedAssessmentFacade> assessments = service.getAllActiveAssessments("title");
        */

        PublishedAssessmentService service = new PublishedAssessmentService();
        ArrayList<PublishedAssessmentFacade> assessments = service.getAllPublishedAssessments("title", PublishedAssessmentFacade.ACTIVE_STATUS);


        M_log.info("assessments:");
        M_log.info(assessments);

        M_log.info("site:");
        M_log.info(site);

        String output = "";

        ArrayList<String> resultArray = new ArrayList<String>();
        for (PublishedAssessmentFacade ass : assessments) {
            AssessmentAccessControlIfc accessControl = ass.getAssessmentAccessControl();
            M_log.info("accessControl.getReleaseTo():");
            M_log.info(accessControl.getReleaseTo());
            M_log.info("site.getTitle():");
            M_log.info(site.getTitle());

            if (accessControl.getReleaseTo().equals(site.getTitle())) {

                AssessmentInfo assessmentInfo = new AssessmentInfo();
                assessmentInfo.populate(ass);
                resultArray.add(assessmentInfo.toJSONString());
            }
        }

        return resultArray.toString();
    }

    public static String submitResults(String siteId, String assessmentId, String userID, String attemptID, String[] answersList, boolean isSurvey) {
        String retval = "";
        String eventRef = "siteId=" + siteId + ", publishedAssessmentId=" + assessmentId + ", agentId=" + userID;
        M_log.info("eventRef: " + eventRef);

        EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.take", eventRef, siteId, true, NotificationService.NOTI_REQUIRED));

/*
        sam.assessment.take
        sam.submit.from_last_page
        sam.assessment.submit.click_sub
        sam.assessment.submit.checked
        sam.assessment.submit
*/
       /* Iterator<Assignment> iterator =  new DbAssignmentService().getAssignmentsForContext(siteId);
        ArrayList<Assignment> assignments = (iterator != null && iterator.hasNext()) ? (ArrayList<Assignment>)iterator : null;
        M_log.info(assignments);
*/

        GradingService service = new GradingService();

        AssessmentGradingData assessmentGradingData = new AssessmentGradingData();
        assessmentGradingData.setPublishedAssessmentId(Long.valueOf(assessmentId));
//        assessmentGradingData.setAgentId(AgentFacade.getAgentString());
        assessmentGradingData.setAgentId(userID);
        assessmentGradingData.setForGrade(Boolean.TRUE);
        assessmentGradingData.setAttemptDate(new Date());
        assessmentGradingData.setSubmittedDate(new Date());
        assessmentGradingData.setIsLate(Boolean.FALSE);
        assessmentGradingData.setStatus(Integer.valueOf(1));
        assessmentGradingData.setTimeElapsed(Integer.valueOf("0"));

        M_log.info("before save assessment");
        service.saveOrUpdateAssessmentGradingOnly(assessmentGradingData);

        M_log.info("assessmentGradingId after saving");
        M_log.info(assessmentGradingData.getAssessmentGradingId());

        // HashMap itemHash = getPublishedItemHash(assessmentId);
        List<PublishedItemData> publishedItems = preparePublishedItemList(assessmentId);
        HashMap answerHash = getPublishedAnswerItemHash(assessmentId);
        hmToString(answerHash);

        Double totalScore = 0d;

        Set<ItemGradingData> itemGradingSet = new HashSet<ItemGradingData>();
        ItemGradingData itemGradingData;
        int index = 0;
        for (String answer : answersList) {
            itemGradingData = new ItemGradingData();
            //itemGradingData.setAnswerText(answer);
            itemGradingData.setAgentId(userID);
            itemGradingData.setSubmittedDate(new Date());
            itemGradingData.setAssessmentGradingId(assessmentGradingData.getAssessmentGradingId());
            PublishedItemData item = publishedItems.get(index);

            Iterator iter = answerHash.entrySet().iterator();
            PublishedAnswer publishedAnswer = null;
            while (iter.hasNext() && publishedAnswer == null) {
                Map.Entry pairs = (Map.Entry) iter.next();
                PublishedAnswer tmp = (PublishedAnswer) pairs.getValue();

                if (tmp.getItem().getItemId().equals(item.getItemId()) && answer.equals(tmp.getText()))
                    publishedAnswer = tmp;
            }

            PublishedItemText itemText = (PublishedItemText) item.getItemTextArraySorted().get(0);


            if (publishedAnswer != null) {
                M_log.info("PUBLISHED ANSWER FOUND!!!! id: " + publishedAnswer.getId());
                M_log.info("publishedAnswer.getText()" + publishedAnswer.getText());
                M_log.info("publishedAnswer.getGrade()" + publishedAnswer.getGrade());
                M_log.info("publishedAnswer.getScore()" + publishedAnswer.getScore());
                if (publishedAnswer.getIsCorrect()) {
                    totalScore += publishedAnswer.getScore();
                    itemGradingData.setAutoScore(publishedAnswer.getScore());
                } else
                    itemGradingData.setAutoScore(0d);

                //itemGradingData.setPublishedAnswer(publishedAnswer);
                itemGradingData.setPublishedAnswerId(publishedAnswer.getId());

            } else
                itemGradingData.setAutoScore(0d);

            itemGradingData.setOverrideScore(0d);
            itemGradingData.setPublishedItemId(item.getItemId());
            itemGradingData.setPublishedItemTextId(itemText.getId());
            itemGradingData.setSubmittedDate(new Date());
            itemGradingSet.add(itemGradingData);
            index++;

            service.saveItemGrading(itemGradingData);

        }

        //assessmentGradingData.setItemGradingSet(itemGradingSet);
        //service.completeItemGradingData(assessmentGradingData);

        assessmentGradingData.setTotalAutoScore(totalScore);
        assessmentGradingData.setFinalScore(totalScore);
        assessmentGradingData.setTotalOverrideScore(0d);
        service.saveOrUpdateAssessmentGradingOnly(assessmentGradingData);
        EventTrackingService.post(EventTrackingService.newEvent("sam.assessment.submit", "siteId=" + siteId + ", submissionId=" + assessmentGradingData.getAssessmentGradingId(), siteId, true, NotificationService.NOTI_REQUIRED));
        return retval;
    }

    private static List<PublishedItemData> preparePublishedItemList(String assessmentId) {
        PublishedAssessmentService pubService = new PublishedAssessmentService();
        PublishedAssessmentFacade assessmentFacade = pubService.getPublishedAssessment(assessmentId);
        List<PublishedItemData> retval = new ArrayList<PublishedItemData>();
        ArrayList sectionArray = assessmentFacade.getSectionArray();
        for (int i = 0; i < sectionArray.size(); i++) {
            SectionDataIfc section = (SectionDataIfc) sectionArray.get(i);
            ArrayList itemArray = section.getItemArray();
            retval.addAll(itemArray);
        }
        return retval;
    }

    private static HashMap getPublishedItemHash(String assessmentId) {
        PublishedAssessmentService pubService = new PublishedAssessmentService();
        PublishedAssessmentFacade assessmentFacade = pubService.getPublishedAssessment(assessmentId);

        return pubService.preparePublishedItemHash(assessmentFacade);
    }

    private static HashMap getPublishedAnswerItemHash(String assessmentId) {
        PublishedAssessmentService pubService = new PublishedAssessmentService();
        PublishedAssessmentFacade assessmentFacade = pubService.getPublishedAssessment(assessmentId);

        return pubService.preparePublishedAnswerHash(assessmentFacade);
    }

    private static List<PublishedItemData> preparePublishedItemList(PublishedAssessmentIfc publishedAssessment) {
        List<PublishedItemData> retval = new ArrayList<PublishedItemData>();
        ArrayList sectionArray = publishedAssessment.getSectionArray();
        for (int i = 0; i < sectionArray.size(); i++) {
            SectionDataIfc section = (SectionDataIfc) sectionArray.get(i);
            ArrayList itemArray = section.getItemArray();
            retval.addAll(itemArray);
        }
        return retval;
    }

    private static void hmToString(HashMap map) {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
/*
            M_log.info("hashmap item");
            M_log.info(pairs.getKey() + " = " + pairs.getValue());
            PublishedItemData item = (PublishedItemData)pairs.getValue();


            M_log.info("item.getItemId()");
            M_log.info(item.getItemId());
            M_log.info("item.getAnswerKey()"); //question right answer
            M_log.info(item.getAnswerKey()); //question right answer
            M_log.info("item.getText()");//question text
            M_log.info(item.getText());//question text

            M_log.info("item.getItemTextArraySorted()");
            M_log.info(item.getItemTextArraySorted());
            PublishedItemText itemText = (PublishedItemText) item.getItemTextArraySorted().get(0);
            M_log.info("itemText.getId()");
            M_log.info(itemText.getId());
            M_log.info("itemText.getText()");
            M_log.info(itemText.getText());*/
        }


   /* private static void hmToString(HashMap map) {
        Iterator it = map.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());

            M_log.info("hashmap item");
            M_log.info(pairs.getKey() + " = " + pairs.getValue());
            PublishedItemData item = (PublishedItemData)pairs.getValue();


            M_log.info("item.getItemId()");
            M_log.info(item.getItemId());
            M_log.info("item.getAnswerKey()"); //question right answer
            M_log.info(item.getAnswerKey()); //question right answer
            M_log.info("item.getText()");//question text
            M_log.info(item.getText());//question text

            M_log.info("item.getItemTextArraySorted()");
            M_log.info(item.getItemTextArraySorted());
            PublishedItemText itemText = (PublishedItemText) item.getItemTextArraySorted().get(0);
            M_log.info("itemText.getId()");
            M_log.info(itemText.getId());
            M_log.info("itemText.getText()");
            M_log.info(itemText.getText());
        }*/
    }

    public static JSONArray submitCalendarEvents(JSONArray calendars) {
        Session currentSession = SessionManager.getCurrentSession();
        M_log.info("Current Session: " + currentSession);
        currentSession.setUserId("admin");
        currentSession.setUserEid("admin");

        org.sakaiproject.calendar.api.ExternalCalendarSubscriptionService service = org.sakaiproject.calendar.cover.ExternalCalendarSubscriptionService.getInstance();

        M_log.info("loop through calendars");

        JSONArray result = new JSONArray();
        for (Object object : calendars) {
            JSONObject calendar = (JSONObject) object;
            M_log.info(calendar);
            String calendarReference = calendar.get("calendarReference").toString();
            String siteId = calendar.get("siteId").toString();

           /* org.sakaiproject.calendar.api.Calendar bbCalendar =
                    service.getCalendarSubscription(calendarReference);*/

            // this.setCalendarEditable(calendarReference, true);

            // if (bbCalendar == null) {
            M_log.info("Calendar :" + calendarReference + " is not found and will be created");

            if (externalCalendaringService == null) {
                externalCalendaringService = (org.sakaiproject.calendaring.api.ExternalCalendaringService) ComponentManager.get(org.sakaiproject.calendaring.api.ExternalCalendaringService.class);
            }

            JSONArray events = (JSONArray) calendar.get("events");

            for (Object obj : events) {
                JSONObject event = (JSONObject) obj;
                String eventName = event.get("name").toString();


                String startParam = event.get("startDate").toString();
                String endParam = (String) event.get("endDate").toString();
                Date startDateParam = null;
                Date endDateParam = null;

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");

                try {
                    startDateParam = df.parse(startParam);
                    if (endParam != null)
                        endDateParam = df.parse(endParam);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }

                String durationParam = null;

                if (endParam == null)
                    durationParam = event.get("duration").toString();

                M_log.info("startParam: " + startParam);
                M_log.info("endParam: " + endParam);
                M_log.info("durationParam: " + durationParam);

                Calendar c = Calendar.getInstance();
                if (null == endDateParam && null != durationParam) {
                    M_log.info("endDateParam 1 " + endParam);
                    M_log.info("durationParam 1 " + durationParam);
                    c.setTime(startDateParam);

                    Dur dur = new Dur(durationParam);
                    if (dur.getWeeks() > 0) {
                        c.add(Calendar.DATE, dur.getWeeks() * 7);
                    }
                    if (dur.getDays() > 0) {
                        c.add(Calendar.DATE, dur.getDays());
                    }
                    if (dur.getHours() > 0) {
                        c.add(Calendar.HOUR, dur.getHours());
                    }
                    if (dur.getMinutes() > 0) {
                        c.add(Calendar.MINUTE, dur.getMinutes());
                    }
                    endDateParam = c.getTime();
                }

                //if endDate and duration are not specified
                //or if for some reason startDate equals endDate
                //By default event duration is set to 1hour
                if (null == endDateParam || endDateParam.getTime() == startDateParam.getTime()) {

                    c.setTime(startDateParam);
                    c.add(Calendar.HOUR, 1);
                    endDateParam = c.getTime();
                }

                    /*Dur dur = null;

                    net.fortuna.ical4j.model.Date start = new net.fortuna.ical4j.model.Date(startDateParam);
                    net.fortuna.ical4j.model.Date end = null;

                    if (endDateParam != null)
                        end = new net.fortuna.ical4j.model.Date(endDateParam);
                    else if (null != durationParam) {
                        if (durationParam == "day")
                            durationParam = "24H";
                        dur = new Dur(durationParam);
                    } else {
                        //if endDate and duration are not specified
                        //By default event duration is set to 1hour
                        dur = new Dur("1H");
                    }

                    VEvent vEvent = null;
                    if (null != start && null != end) {
                        vEvent = new VEvent(start, end, eventName);

                    } else if (null != start && dur != null) {
                        vEvent = new VEvent(start, dur, eventName);
                    }
                    String uuid = (String) event.get("id");

                    if (null == uuid)
                        uuid = UUID.randomUUID().toString();

                    M_log.info("Event's uuid: " + uuid);
                    vEvent.getProperties().add(new Uid(uuid));
                    vEvents.add(vEvent);*/
                //M_log.info(vEvent);
                addEventToCalendar(event, siteId, calendarReference, calendar, startDateParam, endDateParam);
                result.add(event);
            }

            //net.fortuna.ical4j.model.Calendar resultCalendar = externalCalendaringService.createCalendar(vEvents);
            // M_log.info("Calendar is created:  " + resultCalendar);
            // result += "Calendar is created:  " + resultCalendar + "\n";
            // }

        }
        return result;

    }

    public static List<String> deleteCalendarEvents(JSONArray calendars) {
        List<String> result = new ArrayList<>();
        org.sakaiproject.calendar.api.Calendar calendarObj = null;
        Session currentSession = SessionManager.getCurrentSession();
        M_log.info("Current Session: " + currentSession);
        currentSession.setUserId("admin");
        currentSession.setUserEid("admin");

        for (Object object : calendars) {
            JSONObject calendar = (JSONObject) object;
            M_log.info("deleteCalendarEvents for calendar: " + calendar);
            String calendarReference = calendar.get("calendarReference").toString();
            String siteId = calendar.get("siteId").toString();
            calendarReference = "/calendar/calendar/" + siteId + "/" + calendarReference;
            try {
                calendarObj = getHelper().getCalendar(calendarReference);
            } catch (IdUnusedException e) {
                e.printStackTrace();
            } catch (PermissionException e) {
                e.printStackTrace();
            }
            JSONArray events = (JSONArray) calendar.get("events");

            for (Object obj : events) {
                JSONObject event = (JSONObject) obj;
                String sakaiId = event.get("sakaiId").toString();
                try {
                    CalendarEventEdit edit = calendarObj.getEditEvent(sakaiId, org.sakaiproject.calendar.api.CalendarService.EVENT_MODIFY_CALENDAR);
                    calendarObj.removeEvent(edit, CalendarService.MOD_NA);
                    result.add(sakaiId);
                } catch (IdUnusedException err) {
                    // if this event doesn't exist, let user stay in activity view
                    // set the state recorded ID as null
                    // show the alert message
                    // reset the menu button display, no revise/delete
                    M_log.debug(".IdUnusedException " + err);
                } catch (PermissionException err) {
                    M_log.debug(".PermissionException " + err);
                } catch (InUseException err) {
                    M_log.debug(".InUseException " + err);
                }

            }

        }
        return result;
    }

    private static String getParamerizedEventDescription(JSONObject calendar, String sakaiId) {
        M_log.info("Entering getParamerizedEventDescription()");
        M_log.info("calendar: " + calendar);
        M_log.info("sakaiId: " + sakaiId);

        String result = null;

        String secret = calendar.get("secret").toString();
        String meetingId = calendar.get("meetingId").toString();
        String siteId = calendar.get("siteId").toString();


        String buttonSource = calendar.get("buttonSource") == null ? null : calendar.get("buttonSource").toString();

        String ltiUrl = getLtiUrl(secret, siteId);
        String template = "<p><a href='##LTI_URL##/?ltiId=##LTI_ID##%26ltiAction=##LTI_ACTION##%26sakaiId=##SAKAI_ID##' target='_blank'><input type='button' value='Join'></a></p>";

        template += "<span id='id_token_secret' style='display: none;'>##SECRET##</span>";
        template += "<span id='id_token_meetingId' style='display: none;'>##MEETING_ID##</span>";

        result = StringUtils.replace(template, "##LTI_URL##", ltiUrl);
        result = StringUtils.replace(result, "##BUTTON_SRC##", buttonSource);
        result = StringUtils.replace(result, "##LTI_ID##", meetingId);
        result = StringUtils.replace(result, "##MEETING_ID##", meetingId);
        result = StringUtils.replace(result, "##SAKAI_ID##", sakaiId);
        result = StringUtils.replace(result, "##SECRET##", secret);
        result = StringUtils.replace(result, "##LTI_ACTION##", "join");

        M_log.info("Exiting getParamerizedEventDescription() " + result);
        return result;
    }

    protected  static org.sakaiproject.site.api.SiteService m_siteService =
            (org.sakaiproject.site.api.SiteService) ComponentManager.get(org.sakaiproject.site.api.SiteService.class);

    private static String getLtiUrl(String securityKey, String siteId) {
        final String getServerUrl = ServerConfigurationService.getServerUrl();
        M_log.info("sakaiHomePath: " + getServerUrl);


        String url = null;
        try {
            M_log.info("m_siteService: " + m_siteService);

            Site site = (Site) m_siteService.getSite(siteId);

            M_log.info("site: " + site );

            Collection<ToolConfiguration> tools = site.getTools("sakai.basiclti");
            String toolId  = null;
            M_log.info("tools: " + tools);

            if(tools.size() > 0) {
                for (ToolConfiguration tool : tools) {
                    Properties config = tool.getPlacementConfig();

                    if (config.getProperty("imsti.key").equals(securityKey)) {
                        toolId  = tool.getId();
                        url = getServerUrl + "/access/basiclti/site/" + siteId + "/" + toolId;
                        break;
                    }
                }
            }
            if(toolId == null) {
                tools = site.getTools("sakai.web.168");
                Map<String,Object> toolMap = null;
                Long contentKey = null;
                LTIService ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");

                M_log.info("ltiService: " + ltiService);

                Map<String,Object> content = null;
                SakaiFoorm foorm = new SakaiFoorm();

                for (ToolConfiguration tool : tools) {
                    Properties config = tool.getPlacementConfig();

                    if (config.getProperty("source") != null) {
                        M_log.info("config.getProperty(\"source\"): " + config.getProperty("source"));

                        String[] split = config.getProperty("source").split(":");

                        M_log.info("split: " + split);

                        if(split.length == 2)
                            contentKey =  foorm.getLongKey(split[1]);


                        if ( contentKey >= 0 ) {
                            content = ltiService.getContentDao(contentKey, siteId);
                            if (content != null) {
                                String contentSiteId = (String) content.get(LTIService.LTI_SITE_ID);
                                if (contentSiteId == null || !siteId.equals(contentSiteId)) {
                                    content = null;
                                }
                            }
                            if (content != null) {
                                Long toolKey = foorm.getLongKey(content.get(LTIService.LTI_TOOL_ID));
                                if (toolKey >= 0) toolMap = ltiService.getToolDao(toolKey, siteId);
                            }
                        }


                        M_log.info("toolMap: " + toolMap);

                        Object consumerKey = toolMap.get("consumerkey");

                        M_log.info("consumerKey: " + consumerKey);

                        if(consumerKey != null && consumerKey.equals(securityKey))
                        {
                            url = getServerUrl + config.getProperty("source");
                            break;
                        }
                    }
                }
            }
        } catch (IdUnusedException e) {
            M_log.error(e);
        }

        M_log.info("getLtiUrl return value: " + getServerUrl + "/egcint/redirect.jsf?url=" + url);
        return getServerUrl + "/egcint/redirect.jsf?url=" + url;
    }

    private static CalendarServiceHelperImpl getHelper() {
        return (CalendarServiceHelperImpl) IntegrationContext.getInstance().getCalendarServiceHelper();
    }

    private static void addEventToCalendar(JSONObject event, String siteId, String calendarRef, JSONObject jsonCalendar, Date startDate, Date endDate) {
        M_log.info("Entering addEventToCalendar: " );
        M_log.info("event: " + event);
        org.sakaiproject.calendar.api.Calendar calendar = null;

        try {
            //M_log.info("helper.getCalendarService()");
            //M_log.info(helper.getCalendarService());
            //calendarRef =  helper.calendarReference(siteId, SiteService.MAIN_CONTAINER);
            calendarRef = "/calendar/calendar/" + siteId + "/" + calendarRef;
            calendar = getHelper().getCalendar(calendarRef);

        } catch (IdUnusedException e) {
            e.printStackTrace();
        } catch (PermissionException e) {
            e.printStackTrace();
        }
        if (calendar.allowAddCalendarEvent()) {
            try {
                M_log.info("Before calendar.addEvent");
//                helper.getCalendarService()
                M_log.info("StartDate: " + startDate);
                M_log.info("EndDate: " + endDate);
                TimeRange timeRange = org.sakaiproject.time.cover.TimeService.newTimeRange(startDate.getTime(), endDate.getTime() - startDate.getTime());
                M_log.info("TimeRange: " + timeRange);

                String sakaiId = event.get("sakaiId") != null ? event.get("sakaiId").toString() : null;
                String eventName = event.get("name") != null ? event.get("name").toString() : "";

                if (sakaiId == null || sakaiId.isEmpty()) {
                    CalendarEvent newEvent = calendar.addEvent(timeRange, "", "", AC_CALENDAR_EVENT, null, null, null, null);
                    M_log.info("newEvent: " + newEvent);
                    sakaiId = newEvent.getId();
                }
                CalendarEventEdit edit = calendar.getEditEvent(sakaiId, org.sakaiproject.calendar.api.CalendarService.EVENT_MODIFY_CALENDAR);
                M_log.info("edit event: " + edit);
                String eventDescription = getParamerizedEventDescription(jsonCalendar, sakaiId);
                edit.setDescriptionFormatted(eventDescription);
                edit.setDisplayName(eventName);
                edit.setRange(timeRange);
                calendar.commitEvent(edit);

                event.put("sakaiId", sakaiId);

            } catch (Exception e) {
                M_log.error("ERROR!!! : " + e);
                M_log.info("INFO!!! : " + e);
            }
        }
    }


}
