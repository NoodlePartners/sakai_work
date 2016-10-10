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
    INDEX user_id_site_id (USER_ID,SITE_ID),
    PRIMARY KEY(ID)
);
