package org.sakaiproject.blti.data;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: mihai.popescu
 * Date: 07.01.2015
 * Time: 11:37
 */
public class TestInfo {

    protected String id;
    protected String name;
    protected String description;
    protected String instructions;
    protected String titleColor;
    protected String linkRef;
    protected boolean isAvailable;
    protected boolean hasMultipleAttempts;
    protected boolean isUnlimitedAttempts;
    protected java.lang.Integer attemptCount;
    protected String aggregationModel;
    protected boolean forcedCompletionIndicator;
    protected java.lang.Integer timeLimit;
    protected String timerCompletion;
    protected Calendar startDate;
    protected Calendar endDate;
    protected boolean passwordIndicator;
    protected String password;
    protected boolean doNotAllowLateSubmissionIndicator;
    protected String deliveryType;
    protected boolean isBacktrackProhibited;
    protected boolean randomizeQuestionsIndicator;
    protected boolean showScoreIndicator;
    protected boolean showUserAnsIndicator;
    protected boolean showCorrAnsIndicator;
    protected boolean showFeedbackIndicator;
    protected Calendar announcementTime;
    protected boolean showInstInstructionsIndicator;
    protected boolean showInstDescriptionIndicator;
    protected String feedbackSettings;
    protected List<Map<String, String>> questions;

    protected boolean hasCourseAssessmentInfo = false;

    public TestInfo() {

    }
}
