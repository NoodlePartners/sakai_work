package org.sakaiproject.blti.json;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.blti.data.TestExtended;
import org.sakaiproject.blti.data.TestInfo;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: mihai.popescu
 * Date: 20.01.2015
 * Time: 12:36
 */
public class JSONTest extends TestInfo {
    private static Log log = LogFactory.getLog(JSONTest.class);
    public String toJSONString() {
        log.info("Entering toJSONString() method");
        JSONObject json = new JSONObject();
        try {
            json.start();
            json.addItem("name", name);
            json.addItem("description", description);
            json.addItem("instructions", instructions);
            json.addItem("titleColor", titleColor);
            json.addItem("linkRef", linkRef);
            json.addItem("isAvailable", isAvailable);
            json.addItem("aggregationModel", aggregationModel);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy MM DD hh:mm:ss");
            if (startDate != null) {
                json.addItem("startDate", formatter.format(startDate.getTime()));
            }
            if (endDate != null) {
                json.addItem("endDate", formatter.format(endDate.getTime()));
            }
            if (hasCourseAssessmentInfo) {
                json.addItem("hasMultipleAttempts", hasMultipleAttempts ? "true" : "false");
                json.addItem("isUnlimitedAttempts", isUnlimitedAttempts ? "true" : "false");
                json.addItem("attemptCount", String.valueOf(attemptCount));
                json.addItem("forcedCompletionIndicator", forcedCompletionIndicator ? "true" : "false");
                json.addItem("timeLimit", String.valueOf(timeLimit));
                json.addItem("timerCompletion", String.valueOf(timerCompletion));
                json.addItem("passwordIndicator", passwordIndicator ? "true" : "false");
                json.addItem("password", password);
                json.addItem("doNotAllowLateSubmissionIndicator", doNotAllowLateSubmissionIndicator ? "true" : "false");
                json.addItem("deliveryType", deliveryType);
                json.addItem("isBacktrackProhibited", isBacktrackProhibited ? "true" : "false");
                json.addItem("randomizeQuestionsIndicator", randomizeQuestionsIndicator ? "true" : "false");
                json.addItem("showScoreIndicator", showScoreIndicator ? "true" : "false");
                json.addItem("showUserAnsIndicator", showUserAnsIndicator ? "true" : "false");
                json.addItem("showCorrAnsIndicator", showCorrAnsIndicator ? "true" : "false");
                json.addItem("showFeedbackIndicator", showFeedbackIndicator ? "true" : "false");
                if (announcementTime != null) {
                    json.addItem("announcementTime", formatter.format(announcementTime.getTime()));
                }
                json.addItem("showInstInstructionsIndicator", showInstInstructionsIndicator ? "true" : "false");
                json.addItem("showInstDescriptionIndicator", showInstDescriptionIndicator ? "true" : "false");
                json.addItem("feedbackSettings", feedbackSettings);
            }

            if (null != questions && questions.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                jsonArray.start();

                for (Map<String, String> question : questions) {
                    JSONObject jsonQuestion = new JSONObject();
                    jsonQuestion.start();

                    for (Map.Entry<String, String> entry : question.entrySet()) {
                        log.info("entry: " );
                        log.info(entry);
                        if (entry.getKey().equals("numberLabels") ||
                                entry.getKey().equals("responseNames") ||
                                entry.getKey().equals("variables") ||
                                "choices".equals(entry.getKey()) ||
                                "answers".equals(entry.getKey()) ||
                                "variableSets".equals(entry.getKey()) ||
                                "answerPhrasesList".equals(entry.getKey()) ||
                                "questionWordsList".equals(entry.getKey()) ||
                                "categories".equals(entry.getKey()) ||
                                "learningObjectives".equals(entry.getKey()) ||
                                "levelsOfDifficulty".equals(entry.getKey()) ||
                                "tagKeywords".equals(entry.getKey())) {
                            jsonQuestion.addArray(entry.getKey(), entry.getValue());
                        } else if ("answersList".equals(entry.getKey())) {
                            jsonQuestion.addArray(entry.getKey(), entry.getValue(), true);
                        } else {
                            jsonQuestion.addItem(entry.getKey(), entry.getValue());
                        }
                    }

                    jsonQuestion.end();
                    jsonArray.addJSONObject(jsonQuestion);
                    log.info("jsonQuestion: " + jsonQuestion);
                }

                jsonArray.end();

                json.addArray("questions", jsonArray);
            }

            json.end();
            return json.toString();
        } catch (Exception ex) {
            return json.toString() + "\nError: " + ex.getMessage();
        }
    }
}
