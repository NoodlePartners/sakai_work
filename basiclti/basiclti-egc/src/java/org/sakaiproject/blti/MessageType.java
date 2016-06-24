package org.sakaiproject.blti;

/**
 * Created by Stanislav_Aytuganov on 15.04.2016.
 */
public enum MessageType {

    SUBMIT_CALENDARS("egc-submit-calendars"),
    DELETE_CALENDARS("egc-delete-calendars"),
    GET_ASSESSMENTS("egc_get_assessments"),
    GET_ASSESSMENT_DATA("egc_get_assessment_data"),
    GET_SITES("egc_get_sites"),
    SUBMIT_RESULTS("egc_submit_results");

    private String value;
    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }

}
