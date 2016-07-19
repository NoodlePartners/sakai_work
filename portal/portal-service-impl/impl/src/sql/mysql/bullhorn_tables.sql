CREATE TABLE BULLHORN_ALERTS
(
    ID bigint NOT NULL AUTO_INCREMENT,
    ALERT_TYPE char(8) NOT NULL,
    FROM_USER varchar(99) NOT NULL,
    TO_USER varchar(99) NOT NULL,
    EVENT varchar(32) NOT NULL,
    REF varchar(255) NOT NULL,
    TITLE varchar(255),
    SITE_ID varchar(99),
    URL TEXT NOT NULL,
    EVENT_DATE datetime NOT NULL,
    PRIMARY KEY(ID),
    FOREIGN KEY(FROM_USER) REFERENCES SAKAI_USER(USER_ID),
    FOREIGN KEY(TO_USER) REFERENCES SAKAI_USER(USER_ID),
    FOREIGN KEY(SITE_ID) REFERENCES SAKAI_SITE(SITE_ID)
);
