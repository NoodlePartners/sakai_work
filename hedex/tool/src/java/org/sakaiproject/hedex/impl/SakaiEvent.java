package org.sakaiproject.hedex.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "HDX_SAKAI_EVENT")
public class SakaiEvent {

    @Id
    @Column(name = "ID")
    private int id;
}
