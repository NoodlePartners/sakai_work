/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.pages;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.basic.Label;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistration;

public class MyNotifications extends BasePage implements IHeaderContributor {

	private static final Logger log = LoggerFactory.getLogger(MyNotifications.class);

	public MyNotifications() {
		disableLink(notificationsLink);
	}

    public void renderHead(final IHeaderResponse response) {

        final List<Site> sites = sakaiProxy.getUserSites();

        final Map<String, String> siteIdToTitle = new HashMap();
        sites.stream().forEach(s -> siteIdToTitle.put(s.getId(), s.getTitle()));

        StringBuilder sb = new StringBuilder();
        sb.append("var profile = profile || {};\n\n");
        sb.append("profile.registrations = [");
        for (UserNotificationPreferencesRegistration registration : sakaiProxy.getRegisteredNotificationItems()) {
            sb.append("{title: '");
            sb.append(StringEscapeUtils.escapeEcmaScript(registration.getSectionTitle()));
            sb.append("', canOverrideBySite: ");
            sb.append(registration.isOverrideBySite());
            sb.append(", description: '");
            sb.append(StringEscapeUtils.escapeEcmaScript(registration.getSectionDescription()));
            sb.append("', tool: '");
            sb.append(registration.getType());
            sb.append("', options: [");
            Map<String, String> options = registration.getOptions();
            for (String key : options.keySet()) {
                sb.append("{value: '");
                sb.append(StringEscapeUtils.escapeEcmaScript(key));
                sb.append("', display: '");
                sb.append(StringEscapeUtils.escapeEcmaScript(options.get(key)));
                sb.append("'},");
            }
            sb.append("]"); //options

            if (registration.isOverrideBySite()) {
                // Add sites that can be overridden
                final List<Site> toolSites
                    = sites.stream()
                        .filter(s -> s.getToolForCommonId(registration.getToolId()) != null).collect(Collectors.toList());
                if (toolSites.size() > 0) {
                    sb.append(", overridableBy: [");
                    for (Site site : toolSites) {
                        sb.append("{id: '");
                        sb.append(StringEscapeUtils.escapeEcmaScript(site.getId()));
                        sb.append("', title: '");
                        sb.append(StringEscapeUtils.escapeEcmaScript(site.getTitle()));
                        sb.append("'},");
                    }
                    sb.append("]");
                }
            }

            sb.append("},");
        }

        sb.append("];\n\n");

        // Render current preferences
        Preferences preferences = sakaiProxy.getPreferences();
        final Collection<String> keys
            = ((Collection<String>) preferences.getKeys()).stream()
                .filter(k -> k.startsWith(NotificationService.PREFS_TYPE)).collect(Collectors.toList());

        sb.append("profile.currentPreferences = {");

        for (String key : keys) {
            String tool = key.substring(NotificationService.PREFS_TYPE.length());
            if (tool.endsWith(NotificationService.NOTI_OVERRIDE_EXTENSION)) {
                ResourceProperties props = preferences.getProperties(key);
                sb.append("'");
                sb.append(tool);
                sb.append("': [");
                Iterator<String> i = props.getPropertyNames();
                while (i.hasNext()) {
                    String siteId = i.next();
                    sb.append("{siteId: '");
                    sb.append(siteId);
                    sb.append("', siteTitle: '");
                    sb.append(siteIdToTitle.get(siteId));
                    sb.append("', setting: '");
                    sb.append(props.getProperty(siteId));
                    sb.append("'}");
                    if (i.hasNext()) {
                        sb.append(",");
                    }
                }
                sb.append("],");
            } else {
                ResourceProperties props = preferences.getProperties(key);
                sb.append("'");
                sb.append(tool);
                sb.append("': '");
                sb.append(props.getProperty("2"));
                sb.append("', ");
            }
        }
        sb.append("};");

        response.render(JavaScriptHeaderItem.forScript(sb.toString(), null));
    }
}
