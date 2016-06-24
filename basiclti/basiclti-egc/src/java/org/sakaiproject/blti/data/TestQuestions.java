package org.sakaiproject.blti.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.blti.json.AdvancedJSONObject;
import org.sakaiproject.blti.json.ItemType;
import org.sakaiproject.blti.json.JSONArray;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.TypeFacade;

import java.io.IOException;
import java.util.*;

public class TestQuestions {

    private static Log log = LogFactory.getLog(TestQuestions.class);

    public static Map<String, String> addQuestion(ItemData itemFacade) {
        log.info("Entering addQuestion method");

        Map<String, String> questionMap = new HashMap<String, String>();
        questionMap.put("type", ItemType.getTypeAsString(itemFacade.getTypeId()));
        questionMap.put("title", itemFacade.getDescription());
        questionMap.put("text", itemFacade.getText());

///////////////////////////////////////////
     /*   if (AsiMetadata.BbmdQuestionType.FILL_IN_THE_BLANK.equals(questionWrapper.getQuestionType())) {
            FillInTheBlankQuestionWrapper fillInTheBlankQuestionWrapper = (FillInTheBlankQuestionWrapper) questionWrapper;
            JSONArray answersJSON = new JSONArray();
            answersJSON.start();
            for (FillInTheBlankQuestionWrapper.FillInTheBlankAnswer fillInTheBlankAnswer : fillInTheBlankQuestionWrapper.getAnswers()) {
                List<Map<String,String>> list = new ArrayList<Map<String, String>>();

                Map<String,String> map = new HashMap<String, String>();
                map.put("text", fillInTheBlankAnswer.getText());
                list.add(map);

                map = new HashMap<String, String>();
                map.put("subType", fillInTheBlankAnswer.getEvaluationType().name());
                list.add(map);

                map = new HashMap<String, String>();
                map.put("caseSensitive", fillInTheBlankAnswer.isCaseSensitive()? "true" : "false");
                list.add(map);

                answersJSON.addJSONObject(new AdvancedJSONObject(list));
            }
            answersJSON.end();
            questionMap.put("answersList", answersJSON.toString());
        }

*/
        if (itemFacade.getTypeId().equals(TypeFacade.MATCHING)) {
            JSONArray answers = new JSONArray();
            answers.start();
            List<ItemTextIfc> itemTexts = itemFacade.getItemTextArray();

            for (ItemTextIfc item : itemTexts) {
                List<AnswerIfc> itemAnswers = item.getAnswerArray();
                List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                Map<String, String> map;
                int index = 0;
                for (AnswerIfc answer : itemAnswers) {

                    if(answer.getIsCorrect()) {
                        map = new HashMap<String, String>();
                        map.put(item.getText(), answer.getText());
                        map.put("index", Integer.valueOf(index).toString());
                        list.add(map);	
                        AdvancedJSONObject object = new AdvancedJSONObject(list);
                        answers.addJSONObject(object);
                    }
                    index++;
                }
            }

            answers.end();
            questionMap.put("answersList", answers.toString());
        } else if (itemFacade.getTypeId().equals(TypeFacade.FILL_IN_BLANK) ||
                itemFacade.getTypeId().equals(TypeFacade.FILL_IN_NUMERIC)) {
            ArrayList<String> answersList = new ArrayList<String>();
            List<ItemTextIfc> itemTexts = itemFacade.getItemTextArray();

            if (itemTexts != null && itemTexts.size() > 0) {
                List<AnswerIfc> itemAnswers = itemTexts.get(0).getAnswerArray();

                for (AnswerIfc answer : itemAnswers) {
                    answersList.add(answer.getText());
                }
            }
            questionMap.put("answersList", answersList.toString());
        }
//////////////////////////////////////////
        //Essay and Short Answer
        else if (itemFacade.getTypeId().equals(TypeFacade.ESSAY_QUESTION)) {
            ArrayList<String> answersList = new ArrayList<String>();
            List<ItemTextIfc> itemTexts = itemFacade.getItemTextArray();
            if (itemTexts != null && itemTexts.size() > 0) {
                List<AnswerIfc> itemAnswers = itemTexts.get(0).getAnswerArray();

                for (AnswerIfc answer : itemAnswers) {
                    answersList.add(answer.getText());
                }
            }
            questionMap.put("answersList", answersList.toString());
        } else if (itemFacade.getTypeId().equals(TypeFacade.TRUE_FALSE)) {
            Set answerSet = null;
            Set set = itemFacade.getItemTextSet();
            Iterator iter = set.iterator();
            if (iter.hasNext()) {
                answerSet = ((ItemTextIfc) iter.next()).getAnswerSet();
            }

            if (answerSet != null) {
                Iterator aiter = answerSet.iterator();
                while (aiter.hasNext()) {
                    AnswerIfc answer = (AnswerIfc) aiter.next();
                    if (answer.getIsCorrect())
                        questionMap.put("answer", answer.getText());
                }
            }
        } else if (itemFacade.getTypeId().equals(TypeFacade.MULTIPLE_CHOICE) ||
                itemFacade.getTypeId().equals(TypeFacade.MULTIPLE_CORRECT) ||
                itemFacade.getTypeId().equals(TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION)) {
           /*
            List<String> answersImageLinks = new ArrayList<String>();
            List<String> answersImageBinary = new ArrayList<String>();

            for (MultipleChoiceQuestionWrapper.MultipleChoiceAnswer multipleChoiceAnswer : multipleChoiceQuestionWrapper.getAnswers()) {
                answers.add(multipleChoiceAnswer.getFormattedText().getFormattedText());
                String serverPath = "https://blackboard.esynctraining.com";

            }*/

            ArrayList<String> answersList = new ArrayList<String>();
            List<ItemTextIfc> itemTexts = itemFacade.getItemTextArray();
            if (itemTexts != null && itemTexts.size() > 0) {
                List<AnswerIfc> itemAnswers = itemTexts.get(0).getAnswerArray();

                for (AnswerIfc answer : itemAnswers) {
                    answersList.add(answer.getLabel() + ":" + answer.getText());
                }
            }
            questionMap.put("answersList", answersList.toString());
            questionMap.put("answer", itemFacade.getAnswerKey());
//            questionMap.put("answersImageLinks", new JSONArray(answersImageLinks).toString());
//            questionMap.put("answersImageBinary", new JSONArray(answersImageBinary).toString());

        }

        return questionMap;
    }

    /*
    public static Map<String, String> addQuestion(QuestionWrapper questionWrapper)
            throws PersistenceException, InvalidVariableException, DuplicateVariableException {
        Map<String, String> questionMap = new HashMap<String, String>();
        questionMap.put("type", questionWrapper.getQuestionTypeAsString());
        questionMap.put("title", questionWrapper.getQuestionTitle());
        //questionMap.put("text", questionWrapper.getMediumDescription());
//        questionMap.put("text", questionWrapper.getFormattedText().getFormattedText());
        questionMap.put("text", questionWrapper.getShortDescription(Integer.MAX_VALUE));
        questionMap.put("partialCredit", questionWrapper.getPartialCredit() ? "true" : "false");
        questionMap.put("pointsValue", questionWrapper.getPointValueAsString());
        questionMap.put("correctFeedback", questionWrapper.getCorrectFeedback().getFormattedText().getText());
        questionMap.put("incorrectFeedback", questionWrapper.getIncorrectFeedback().getFormattedText().getText());
        questionMap.put("isDisabled", questionWrapper.getIsDisabled() ? "true" : "false");
        questionMap.put("isExtraCredit", questionWrapper.getIsExtraCredit() ? "true" : "false");
        questionMap.put("id", questionWrapper.getIdAsString());
        if (questionWrapper.getCategories() != null && questionWrapper.getCategories().size() > 0) {
            questionMap.put("categories", new JSONCategoryArray(questionWrapper.getCategories()).toString());
        }
        if (questionWrapper.getLearningObjectives() != null && questionWrapper.getLearningObjectives().size() > 0) {
            questionMap.put("learningObjectives", new JSONCategoryArray(questionWrapper.getLearningObjectives()).toString());
        }
        if (questionWrapper.getLevelsOfDifficulty() != null && questionWrapper.getLevelsOfDifficulty().size() > 0) {
            questionMap.put("levelsOfDifficulty", new JSONCategoryArray(questionWrapper.getLevelsOfDifficulty()).toString());
        }
        if (questionWrapper.getTagKeywords() != null && questionWrapper.getTagKeywords().size() > 0) {
            questionMap.put("tagKeywords", new JSONCategoryArray(questionWrapper.getTagKeywords()).toString());
        }
        if (questionWrapper.getNumberLabels() != null && questionWrapper.getNumberLabels().length > 0) {
            questionMap.put("numberLabels", new JSONArray(questionWrapper.getNumberLabels()).toString());
        }
        try {
            if (questionWrapper.getResponseNames() != null && questionWrapper.getResponseNames().size() > 0) {
                questionMap.put("responseNames", new JSONArray(questionWrapper.getResponseNames()).toString());
            }
        } catch (Exception ignored) {
        }
        if (questionWrapper.getVariables() != null && questionWrapper.getVariables().size() > 0) {
            questionMap.put("variables", new JSONArray(questionWrapper.getVariables()).toString());
        }

        if (AsiMetadata.BbmdQuestionType.JUMBLED_SENTENCE.equals(questionWrapper.getQuestionType())) {
            JumbledSentenceQuestionWrapper jumbledSentenceQuestionWrapper = (JumbledSentenceQuestionWrapper) questionWrapper;

            // Add question answer(s)
            JSONObject answersJSON = new JSONObject();
            answersJSON.start();
            JumbledSentenceQuestionWrapper.Answers answers = jumbledSentenceQuestionWrapper.getAnswers();
            Set<String> answersIdents = answers.getIdents();
            for (String ident : answersIdents) {
                String answer = answers.getAnswer(ident);
                answersJSON.addItem(ident, answer);
            }
            answersJSON.end();
            questionMap.put("answers", answersJSON.toString());


            // Add Jumbled sentence choices
            JumbledSentenceQuestionWrapper.ChoicesMap choicesMap = jumbledSentenceQuestionWrapper.getChoicesMap();
            JSONObject choicesString = new JSONObject();
            choicesString.start();
            Set<String> choicesIdents = choicesMap.getIdents();
            for (String ident : choicesIdents) {
                JumbledSentenceQuestionWrapper.Choices choices = choicesMap.getChoices(ident);
                JSONObject choiceIdent = new JSONObject();
                choiceIdent.start();
                for (int i = 0; i < choices.getSize(); i++) {
                    JumbledSentenceQuestionWrapper.Choice choice = choices.getChoice(i);
                    choiceIdent.addItem(choice.getIdent(), choice.getValue());
                }
                choiceIdent.end();
                choicesString.addItem(ident, choiceIdent);
            }
            choicesString.end();
            questionMap.put("choices", choicesString.toString());
        } else if (AsiMetadata.BbmdQuestionType.CALCULATED.equals(questionWrapper.getQuestionType())) {
            CalculatedQuestionWrapper calculatedQuestionWrapper = (CalculatedQuestionWrapper) questionWrapper;

            questionMap.put("answerTolerance", calculatedQuestionWrapper.getAnswerToleranceTypeAsString());
            questionMap.put("formula", calculatedQuestionWrapper.getFormula());
            questionMap.put("answerFormat", calculatedQuestionWrapper.getCorrectAnswerFormat().toString());
            questionMap.put("tolerance", calculatedQuestionWrapper.getAnswerTolerance().getValueAsString());

            // Calculated question variable sets
            VariableSetMap variableSetMap = calculatedQuestionWrapper.getVariableSetMap();
            VariableSet[] variableSets = variableSetMap.getVariableSets();
            JSONObject variables = new JSONObject();
            variables.start();
            for (VariableSet variableSet : variableSets) {
                JSONObject variableJSON = new JSONObject();
                variableJSON.start();
                for (String variable : calculatedQuestionWrapper.getVariables()) {
                    VariableValue variableValue = variableSet.getVariable(variable);
                    variableJSON.addItem(variableValue.getName(), String.valueOf(variableValue.getValue()));
                }
                variableJSON.addItem("answer", String.valueOf(variableSet.getAnswer().getValue()));
                variableJSON.end();
                variables.addItem(variableSet.getIdent(), variableJSON);
            }
            variables.end();
            questionMap.put("variableSets", variables.toString());
        } else if (AsiMetadata.BbmdQuestionType.NUMERIC.equals(questionWrapper.getQuestionType())) {
            NumericQuestionWrapper numericQuestionWrapper = (NumericQuestionWrapper) questionWrapper;
            questionMap.put("answer", numericQuestionWrapper.getNumericResponseAnswer().getValueAsString());
            questionMap.put("answerRange", numericQuestionWrapper.getNumericResponseAnswer().getToleranceAsString());
        } else if (AsiMetadata.BbmdQuestionType.BINARY_CHOICE.equals(questionWrapper.getQuestionType())) {
            EitherOrQuestionWrapper eitherOrQuestionWrapper = (EitherOrQuestionWrapper) questionWrapper;
            questionMap.put("answersChoices", eitherOrQuestionWrapper.getStringAnswer());
            if (eitherOrQuestionWrapper.getBooleanAnswer() != null) {
                questionMap.put("answer", eitherOrQuestionWrapper.getBooleanAnswer().toString());
            }
        } else if (AsiMetadata.BbmdQuestionType.TRUE_FALSE.equals(questionWrapper.getQuestionType())) {
            TrueFalseQuestionWrapper trueFalseQuestionWrapper = (TrueFalseQuestionWrapper) questionWrapper;
            if (trueFalseQuestionWrapper.getAnswer() != null) {
                questionMap.put("answer", trueFalseQuestionWrapper.getAnswer().toString());
            }
        }
        else if (AsiMetadata.BbmdQuestionType.FILL_IN_THE_BLANK.equals(questionWrapper.getQuestionType())) {
            FillInTheBlankQuestionWrapper fillInTheBlankQuestionWrapper = (FillInTheBlankQuestionWrapper) questionWrapper;
            JSONArray answersJSON = new JSONArray();
            answersJSON.start();
            for (FillInTheBlankQuestionWrapper.FillInTheBlankAnswer fillInTheBlankAnswer : fillInTheBlankQuestionWrapper.getAnswers()) {
                List<Map<String,String>> list = new ArrayList<Map<String, String>>();

                Map<String,String> map = new HashMap<String, String>();
                map.put("text", fillInTheBlankAnswer.getText());
                list.add(map);

                map = new HashMap<String, String>();
                map.put("subType", fillInTheBlankAnswer.getEvaluationType().name());
                list.add(map);

                map = new HashMap<String, String>();
                map.put("caseSensitive", fillInTheBlankAnswer.isCaseSensitive()? "true" : "false");
                list.add(map);

                answersJSON.addJSONObject(new AdvancedJSONObject(list));
            }
            answersJSON.end();
            questionMap.put("answersList", answersJSON.toString());
        } else if (AsiMetadata.BbmdQuestionType.FIB_PLUS.equals(questionWrapper.getQuestionType())) {
            FillInTheBlankPlusQuestionWrapper fillInTheBlankPlusQuestionWrapper = (FillInTheBlankPlusQuestionWrapper) questionWrapper;
            FillInTheBlankPlusQuestionWrapper.Answers answers = fillInTheBlankPlusQuestionWrapper.getAnswers();
            JSONObject answersJSON = new JSONObject();
            answersJSON.start();

            for (int i = 0; i < answers.getSize(); i++) {
                FillInTheBlankPlusAnswer answer = answers.getAnswer(i);
                List<String> answerList = new ArrayList<String>();

                JSONArray answersArray = new JSONArray();
                answersArray.start();
                for (int j = 0; j < answer.getSize(); j++) {
                    FillInTheBlankPlusAnswer.AnswerValue variableValue = answer.getValue(j);

                    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                    Map<String, String> map = new HashMap<String, String>();

                    list.add(map);
                    map = new HashMap<String, String>();
                    map.put("subType", answer.getValue(i).getEvaluationType().name());
                    list.add(map);

                    map = new HashMap<String, String>();
                    map.put("text", variableValue.getAnswerValue());
                    list.add(map);

                    map = new HashMap<String, String>();
                    map.put("caseSensitive", answer.getValue(i).isCaseSensitive()? "true" : "false");
                    list.add(map);

                    AdvancedJSONObject object = new AdvancedJSONObject(list);

                    answersArray.addJSONObject(object);

                }
                answersArray.end();
                answersJSON.addArray(answer.getIdent(), answersArray);
            }
            answersJSON.end();
            questionMap.put("answersList", answersJSON.toString());
        } else if (AsiMetadata.BbmdQuestionType.MULTIPLE_ANSWER.equals(questionWrapper.getQuestionType())) {
            MultipleAnswerQuestionWrapper multipleAnswerQuestionWrapper = (MultipleAnswerQuestionWrapper) questionWrapper;
            JSONObject answers = new JSONObject();
            answers.start();

            for (MultipleAnswerQuestionWrapper.MultipleAnswerAnswer multipleAnswerAnswer : multipleAnswerQuestionWrapper.getAnswers()) {
                answers.addItem(multipleAnswerAnswer.getFormattedText().getFormattedText(), multipleAnswerAnswer.isCorrect());
            }
            answers.end();

            questionMap.put("answersList", answers.toString());
        } else if (AsiMetadata.BbmdQuestionType.MULTIPLE_CHOICE.equals(questionWrapper.getQuestionType()) ||
                AsiMetadata.BbmdQuestionType.LIKERT_SCALE.equals(questionWrapper.getQuestionType())) {
            MultipleChoiceQuestionWrapper multipleChoiceQuestionWrapper = (MultipleChoiceQuestionWrapper) questionWrapper;
            List<String> answers = new ArrayList<String>();
            List<String> answersImageLinks = new ArrayList<String>();
            List<String> answersImageBinary = new ArrayList<String>();

            for (MultipleChoiceQuestionWrapper.MultipleChoiceAnswer multipleChoiceAnswer : multipleChoiceQuestionWrapper.getAnswers()) {
                answers.add(multipleChoiceAnswer.getFormattedText().getFormattedText());
                String serverPath = "https://blackboard.esynctraining.com";

//                Course course = CourseManagerFactory.getInstance().getCourse(questionWrapper.getCourseId());
//                String homeDirectory = ContentSystemServiceExFactory.getInstance().getDocumentManagerEx().getHomeDirectory(course);

                try {
                    if (null != multipleChoiceAnswer.getFile()) {
                        answersImageLinks.add(multipleChoiceAnswer.getFile().getUrl());
                        answersImageBinary.add(getEncodedImageData(multipleChoiceAnswer.getFile().getUrl(), serverPath));
                    }
                } catch (FileSystemException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            questionMap.put("answersList", new JSONArray(answers).toString());
            questionMap.put("answersImageLinks", new JSONArray(answersImageLinks).toString());
            questionMap.put("answersImageBinary", new JSONArray(answersImageBinary).toString());
            questionMap.put("answer", String.valueOf(multipleChoiceQuestionWrapper.getCorrectAnswerIndex()));

        } else if (AsiMetadata.BbmdQuestionType.ORDERING.equals(questionWrapper.getQuestionType())) {
            OrderingQuestionWrapper orderingQuestionWrapper = (OrderingQuestionWrapper) questionWrapper;
            List<String> answersIndex = new ArrayList<String>();
            List<String> answers = new ArrayList<String>();
            for (OrderingQuestionWrapper.OrderingAnswer orderingAnswer : orderingQuestionWrapper.getAnswers()) {
                answersIndex.add(Integer.toString(orderingAnswer.getDisplayOrder()));
                answers.add(orderingQuestionWrapper.getAnswer(orderingAnswer.getDisplayOrder()).getFormattedText().getText());
            }
            questionMap.put("answersList", new JSONArray(answersIndex).toString());
            questionMap.put("answers", new JSONArray(answers).toString());
        } else if (AsiMetadata.BbmdQuestionType.QUIZ_BOWL.equals(questionWrapper.getQuestionType())) {
            QuizBowlQuestionWrapper quizBowlQuestionWrapper = (QuizBowlQuestionWrapper) questionWrapper;
            List<String> answerPhrases = new ArrayList<String>();
            List<String> questionWords = new ArrayList<String>();
            for (String answerPhrase : quizBowlQuestionWrapper.getAnswerPhrases()) {
                answerPhrases.add(answerPhrase);
            }
            for (String questionWord : quizBowlQuestionWrapper.getQuestionWords()) {
                questionWords.add(questionWord);
            }
            questionMap.put("answerPhrasesList", new JSONArray(answerPhrases).toString());
            questionMap.put("questionWordsList", new JSONArray(questionWords).toString());
        } else if (AsiMetadata.BbmdQuestionType.ESSAY.equals(questionWrapper.getQuestionType()) ||
                AsiMetadata.BbmdQuestionType.SHORT_ANSWER.equals(questionWrapper.getQuestionType())) {
            EssayQuestionWrapper essayQuestionWrapper = (EssayQuestionWrapper) questionWrapper;
            questionMap.put("answer", essayQuestionWrapper.getAnswerText().getFormattedText());
        } else if (AsiMetadata.BbmdQuestionType.MATCHING.equals(questionWrapper.getQuestionType())) {
            MatchingQuestionWrapper matchingQuestionWrapper = (MatchingQuestionWrapper) questionWrapper;
            JSONArray answers = new JSONArray();
            answers.start();
            Integer leftMatchCount = matchingQuestionWrapper.getLeftMatchCount();
            Integer rightMatchCount = matchingQuestionWrapper.getRightMatchCount();
            Boolean simpleCase = leftMatchCount >= rightMatchCount;
            for (int i = 0; i < (simpleCase ? leftMatchCount : rightMatchCount); i++) {
                MatchingQuestionWrapper.LeftMatch leftMatch = null;
                if (simpleCase || leftMatchCount > i)
                    leftMatch = matchingQuestionWrapper.getLeftMatch(i);

                List<Map<String, String>> list = new ArrayList<Map<String, String>>();
                Map<String, String> map = new HashMap<String, String>();
                String leftText = null != leftMatch ? leftMatch.getFormattedText().getText() : "#EXTRAVARIANT#";
                Integer rightMatchIndex = null != leftMatch ? leftMatch.getRightMatchIndex() : i;
                String rightText = matchingQuestionWrapper.getRightMatch(rightMatchIndex).getFormattedText().getText();
                map.put(leftText, rightText);

                list.add(map);
                map = new HashMap<String, String>();
                map.put("index", rightMatchIndex.toString());
                list.add(map);

                AdvancedJSONObject object = new AdvancedJSONObject(list);

                answers.addJSONObject(object);
            }
            answers.end();
            questionMap.put("answersList", answers.toString());
        } else if (AsiMetadata.BbmdQuestionType.HOTSPOT.equals(questionWrapper.getQuestionType())) {
            HotspotQuestionWrapper hotspotQuestionWrapper = (HotspotQuestionWrapper) questionWrapper;
            JSONObject answer = new JSONObject();
            answer.start();
            answer.addItem("lowerLeft", hotspotQuestionWrapper.getHotspotAnswer().getLowerLeftAsString());
            answer.addItem("lowerRight", hotspotQuestionWrapper.getHotspotAnswer().getLowerRightAsString());
            answer.addItem("upperLeft", hotspotQuestionWrapper.getHotspotAnswer().getUpperLeftAsString());
            answer.addItem("upperRight", hotspotQuestionWrapper.getHotspotAnswer().getUpperRightAsString());
            answer.addItem("coord", hotspotQuestionWrapper.getHotspotAnswer().getCoords());
            try {
                AssessmentFile file = hotspotQuestionWrapper.getImageFile();
                answer.addItem("image", file.getUrl());
            } catch (Exception ignored) {
            }

            answer.end();

            questionMap.put("answersList", answer.toString());
        }

        return questionMap;
    }

    private static String getEncodedImageData(String link, String serverPath) throws IOException {

        if (link == null || link.isEmpty())
            return "";

        // read "any" type of image (in this case a png file)

        BufferedImage image = ImageIO.read(new URL(serverPath + "/" + link));
        String encoded = "";
        // write it to byte array in-memory (jpg format)
        if (null != image) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();

            ImageIO.write(image, "jpg", b);

            // do whatever with the array...
            byte[] jpgByteArray = b.toByteArray();

            encoded = DatatypeConverter.printBase64Binary(jpgByteArray);
        }

        return encoded;
    }
    */


}

