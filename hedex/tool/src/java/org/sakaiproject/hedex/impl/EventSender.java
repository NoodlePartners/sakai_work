package org.sakaiproject.hedex.impl;

import java.util.List;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.Event;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventSender {

    private ObjectMapper objectMapper = new ObjectMapper();
    private String tenantId;

    @Setter
    private ServerConfigurationService serverConfigurationService;

    public void init() {
        tenantId = serverConfigurationService.getString("hedex.tenantId", "unknown");
    }

    public void sendEvents(List<Event> events) {

        log.debug("Sending {} events ...", events.size());
        try {
            String eventsJson = objectMapper.writeValueAsString(new EventsPackage(tenantId, events));
            log.debug("EventsPackage json: {}", eventsJson);
        } catch (Exception e) {
            log.error("Failed to serialise events to JSON", e);
        }
    }

    @Getter
    public class EventsPackage {

        private List<Event> events;
        private String tenantId;

        public EventsPackage(String tenantId, List<Event> events) {

            this.tenantId = tenantId;
            this.events = events;
        }
    }
}
