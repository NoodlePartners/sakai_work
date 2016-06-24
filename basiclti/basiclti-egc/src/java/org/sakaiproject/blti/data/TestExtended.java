package org.sakaiproject.blti.data;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.blti.json.JSONTest;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.SectionService;
import org.sakaiproject.tool.assessment.shared.impl.assessment.SectionServiceImpl;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: mihai.popescu
 * Date: 20.01.2015
 * Time: 14:54
 */
public class TestExtended extends JSONTest {
    private static Log log = LogFactory.getLog(TestExtended.class);

    public void populate(AssessmentFacade assessment) {
        for (SectionFacade section : (List<SectionFacade>) assessment.getSectionArray()) {
            log.info("Entering populate() metod");
            QuestionPoolService qpService = new QuestionPoolService();

            SectionDataIfc sf = new SectionServiceImpl().getSection(section.getSectionId(), null);

            /*
            log.info("section pool name");
            log.info(section
                    .getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW ));

            ArrayList itemlist = qpService.getAllItems(Long.valueOf(section
                    .getSectionMetaDataByLabel(SectionDataIfc.POOLNAME_FOR_RANDOM_DRAW )));
                    */
            name = assessment.getTitle();
            description = assessment.getDescription();
            instructions = assessment.getComments();
            titleColor = "NO_SUCH_PROPERTY";
            isAvailable = assessment.getStatus() == 0 ? false : true;

            ArrayList itemlist = section.getItemArray();
            questions = new ArrayList<Map<String, String>>();
            log.info("itemList size" + itemlist.size());

            Iterator iter = itemlist.iterator();
            while (iter.hasNext()) {
                ItemData item = (ItemData) iter.next();
                Map<String, String> questionMap = TestQuestions.addQuestion(item);
                questions.add(questionMap);
            }
        }


/*
        Id assessmentID = content.getGradebookItem().getAssessmentId();

        AssessmentWrapper assessmentWrapper = AssessmentWrapper.loadById(assessmentID);

        name = assessmentWrapper.getName();
        description = assessmentWrapper.getDescription().getText();
        instructions = assessmentWrapper.getInstructions().getText();
        titleColor = content.getTitleColor();
        linkRef = content.getLinkRef();
        isAvailable = content.getIsAvailable();
        if (content.getGradebookItem().getAggregationModel().equals(GradableItem.AttemptAggregationModel.AVERAGE)) {
            aggregationModel = "average";
        } else if (content.getGradebookItem().getAggregationModel().equals(GradableItem.AttemptAggregationModel.FIRST)) {
            aggregationModel = "first";
        } else if (content.getGradebookItem().getAggregationModel().equals(GradableItem.AttemptAggregationModel.HIGHEST)) {
            aggregationModel = "highest";
        } else if (content.getGradebookItem().getAggregationModel().equals(GradableItem.AttemptAggregationModel.LAST)) {
            aggregationModel = "last";
        } else if (content.getGradebookItem().getAggregationModel().equals(GradableItem.AttemptAggregationModel.LOWEST)) {
            aggregationModel = "lowest";
        }
        startDate = content.getStartDate();
        endDate = content.getEndDate();

        // For some content we don't find the course assessment

        try {
            CourseAssessment courseAssessment = CourseAssessmentDbLoader.Default.getInstance().loadByQtiAssessmentId(assessmentWrapper.getId()).get(0);
            hasMultipleAttempts = courseAssessment.getHasMultipleAttempts();
            isUnlimitedAttempts = courseAssessment.getIsUnlimitedAttempts();
            attemptCount = courseAssessment.getAttemptCount();
            forcedCompletionIndicator = courseAssessment.getForcedCompletionIndicator();
            timeLimit = courseAssessment.getTimeLimit();
            timerCompletion = courseAssessment.getTimerCompletion().equals(CourseAssessment.TimerCompletion.CONTINUAL) ? "continual" : "hardstop";
            passwordIndicator = courseAssessment.getPasswordIndicator();
            password = courseAssessment.getPassword();
            doNotAllowLateSubmissionIndicator = courseAssessment.getDoNotAllowLateSubmissionIndicator();
            if (courseAssessment.getDeliveryType().equals(CourseAssessment.DeliveryType.ALL_AT_ONCE)) {
                deliveryType = "allAtOnce";
            } else if (courseAssessment.getDeliveryType().equals(CourseAssessment.DeliveryType.SECTION_BY_SECTION)) {
                deliveryType = "sectionBySection";
            } else if (courseAssessment.getDeliveryType().equals(CourseAssessment.DeliveryType.QUESTION_BY_QUESTION)) {
                deliveryType = "questionByQuestion";
            } else {
                deliveryType = "default";
            }
            isBacktrackProhibited = courseAssessment.getIsBacktrackProhibited();
            randomizeQuestionsIndicator = courseAssessment.getRandomizeQuestionsIndicator();
            showScoreIndicator = courseAssessment.getShowScoreIndicator();
            showUserAnsIndicator = courseAssessment.getShowUserAnsIndicator();
            showCorrAnsIndicator = courseAssessment.getShowCorrAnsIndicator();
            showFeedbackIndicator = courseAssessment.getShowFeedbackIndicator();
            announcementTime = courseAssessment.getAnnouncementTime();
            showInstInstructionsIndicator = courseAssessment.getShowInstInstructionsIndicator();
            showInstDescriptionIndicator = courseAssessment.getShowInstDescriptionIndicator();
            feedbackSettings = courseAssessment.getFeedbackSettings();

            // We mark the flag to parse the course assessment info
            hasCourseAssessmentInfo = true;

        } catch (Exception ignored) {
        }

        questions = new ArrayList<Map<String, String>>();

        List<AsiObjectWrapper> assessmentContent = assessmentWrapper.getContents();
        for (AsiObjectWrapper objectWrapper : assessmentContent) {

            QuestionWrapper questionWrapper = (QuestionWrapper) objectWrapper;

            Map<String, String> questionMap = TestQuestions.addQuestion(questionWrapper);

            questions.add(questionMap);
        }
        */
    }

}
