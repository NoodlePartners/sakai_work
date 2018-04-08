package org.sakaiproject.hedex.impl;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HedexEventDigester implements Observer {

    private List<String> handledEvents;
    private int batchSize;
    private List<Event> batchedEvents = new ArrayList<>();
    private final String[] defaultEvents = new String[] {"user.login", "user.logout"};

    @Setter
    private EventSender eventSender;

    @Setter
    private EventTrackingService eventTrackingService;

    @Setter
    private ServerConfigurationService serverConfigurationService;

    @Setter
    private SessionFactory sessionFactory;

    public void init() {

        log.debug("HedexEventDigester.init()");

        String[] events = serverConfigurationService.getStrings("hedex.events");
        handledEvents = Arrays.asList(events == null ? defaultEvents : events);
        log.debug("Handled events: {}", String.join(",", handledEvents));
        batchSize = serverConfigurationService.getInt("hedex.batchSize", 10);
        log.debug("batchSize: {}", batchSize);
        eventTrackingService.addObserver(this);
    }

    public void update(Observable o, final Object arg) {

        if (arg instanceof Event) {
            Event event = (Event) arg;
            String eventName = event.getEvent();
            if (handledEvents.contains(eventName)) {

                log.debug("Handling event '{}' ...", eventName);

                synchronized(batchedEvents) {
                    log.debug("Added '{}' to batched events ...", eventName);
                    batchedEvents.add(event);
                    if (batchedEvents.size() == batchSize) {
                        log.debug("Sending batch of events ...");
                        List<Event> copy = new ArrayList<>(batchedEvents);
                        new Thread(() -> {
                            eventSender.sendEvents(copy);
                        }).start();
                        batchedEvents.clear();
                    }
                }
            }
        }
    }
}
