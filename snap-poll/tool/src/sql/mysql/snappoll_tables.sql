CREATE TABLE SNAP_POLL_SUBMISSION
(
    ID char(36) NOT NULL,
    USER_ID varchar(99) NOT NULL,
    SITE_ID varchar(99) NOT NULL,
    RESPONSE char(1),
    REASON varchar(255),
    TOOL varchar(24) NOT NULL,
    CONTEXT varchar(255) NOT NULL,
    IGNORED char(1),
    SUBMITTED_TIME bigint NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES SAKAI_USER(USER_ID),
    FOREIGN KEY (SITE_ID) REFERENCES SAKAI_SITE(SITE_ID),
    PRIMARY KEY(ID)
);
