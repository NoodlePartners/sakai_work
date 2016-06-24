package org.sakaiproject.blti.json;

import org.sakaiproject.tool.assessment.facade.TypeFacade;

/**
 * Created by Stanislav_Aytuganov on 06.10.2015.
 */
public enum ItemType {
    MULTIPLE_CORRECT (TypeFacade.MULTIPLE_CORRECT) ,
    MULTIPLE_CHOICE (TypeFacade.MULTIPLE_CHOICE),
    MULTIPLE_CORRECT_SINGLE_SELECTION(TypeFacade.MULTIPLE_CORRECT_SINGLE_SELECTION),
    TRUE_FALSE (TypeFacade.TRUE_FALSE),
    ESSAY_QUESTION (TypeFacade.ESSAY_QUESTION),
    FILE_UPLOAD (TypeFacade.FILE_UPLOAD),
    AUDIO_RECORDING (TypeFacade.AUDIO_RECORDING),
    FILL_IN_BLANK (TypeFacade.FILL_IN_BLANK),
    FILL_IN_NUMERIC (TypeFacade.FILL_IN_NUMERIC),
    MATCH(TypeFacade.MATCHING),
    MULTIPLE_CHOICE_SURVEY(TypeFacade.MULTIPLE_CHOICE_SURVEY),
    MATRIX_CHOICES_SURVEY(TypeFacade.MATRIX_CHOICES_SURVEY);

    private ItemType(final Long value) {
        this.value = value;
    }

    private final Long value;

    public static String getTypeAsString(Long type) {
        return ItemType.getByValue(type).name();
    }

    private static ItemType getByValue(Long val) {
        for (ItemType type : ItemType.values()) {
            if (type.value.equals(val)) {
                return type;
            }
        }
        return null;
    }





  /*
    public static final Long TRUE_FALSE = Long.valueOf(4);
    public static final Long ESSAY_QUESTION = Long.valueOf(5);
    public static final Long FILE_UPLOAD = Long.valueOf(6);
    public static final Long AUDIO_RECORDING = Long.valueOf(7);
    public static final Long FILL_IN_BLANK = Long.valueOf(8);
    public static final Long FILL_IN_NUMERIC = Long.valueOf(11);
    public static final Long MATCHING = Long.valueOf(9);
    public static final Long MULTIPLE_CORRECT_SINGLE_SELECTION = Long.valueOf(12);
    public static final Long MATRIX_CHOICES_SURVEY = Long.valueOf(13);
    // these are section type available in this site,
    public static final Long DEFAULT_SECTION = Long.valueOf(21);
    // these are assessment template type available in this site,
    public static final Long TEMPLATE_QUIZ = Long.valueOf(41);
    public static final Long TEMPLATE_HOMEWORK = Long.valueOf(42);
    public static final Long TEMPLATE_MIDTERM = Long.valueOf(43);
    public static final Long TEMPLATE_FINAL = Long.valueOf(44);
    // these are assessment type available in this site,
    public static final Long QUIZ = Long.valueOf(61);
    public static final Long HOMEWORK = Long.valueOf(62);
    public static final Long MIDTERM = Long.valueOf(63);
    public static final Long FINAL = Long.valueOf(64);*/

}
