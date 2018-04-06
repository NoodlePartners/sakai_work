package org.sakaiproject.hedex.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import lombok.Setter;

public class HedexEventDigester implements Observer {

    private List<String> handledEvents;

    @Setter
    private ServerConfigurationService serverConfigurationService;

    @Setter
    private SessionFactory sessionFactory;

    public void init() {

        System.out.println("HedexEventDigester.init()");
        handledEvents = Arrays.asList(serverConfigurationService.getStrings("hedex.events"));
    }

    public void update(Observable o, final Object arg) {

        if (arg instanceof Event) {
            Event e = (Event) arg;
            String event = e.getEvent();
            if (handledEvents.contains(event)) {
                // About to start a new thread that expects the changes in this hibernate session
                // to have been persisted, so we flush.
                try {
                    sessionFactory.getCurrentSession().flush();
                } catch (HibernateException he) {
                    // This will be thrown if there is no current Hibernate session. Nothing to do.
                }

                new Thread(() -> {
                }).start();
            }
        }
    }
}
