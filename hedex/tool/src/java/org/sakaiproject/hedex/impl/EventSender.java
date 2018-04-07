package org.sakaiproject.hedex.impl;

import java.util.List;

import org.sakaiproject.event.api.Event;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventSender {

    public void sendEvents(List<Event> events) {

        log.debug("Sending {} events ...", events.size());
    }
}
