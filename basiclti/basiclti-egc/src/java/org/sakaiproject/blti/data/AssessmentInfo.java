package org.sakaiproject.blti.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.blti.json.JSONObject;
import org.sakaiproject.blti.json.JSONTest;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;

/**
 * Created by Stanislav_Aytuganov on 06.10.2015.
 */
public class AssessmentInfo extends JSONTest {
    private static Log log = LogFactory.getLog(JSONTest.class);
    public String id;

    public void populate(PublishedAssessmentFacade assessment) {
        name = assessment.getTitle();
        id = assessment.getAssessmentId().toString();
    }

    public String toJSONString () {
        log.info("Entering toJSONString() method");
        JSONObject json = new JSONObject();
        try {
            json.start();
            json.addItem("name", name);
            json.addItem("id", id);
            json.end();
            return json.toString();
        }catch (Exception ex) {
            return json.toString() + "\nError: " + ex.getMessage();
        }
    }
}
