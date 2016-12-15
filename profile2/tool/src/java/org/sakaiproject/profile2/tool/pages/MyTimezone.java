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

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.Preferences;

public class MyTimezone extends BasePage implements IHeaderContributor {

	private static final Logger log = LoggerFactory.getLogger(MyTimezone.class);

	public MyTimezone() {
		disableLink(timezoneLink);
	}

    public void renderHead(final IHeaderResponse response) {

        Preferences preferences = sakaiProxy.getPreferences();
        ResourceProperties timezoneProps = preferences.getProperties(TimeService.APPLICATION_ID);
        String currentTimezone = timezoneProps.getProperty(TimeService.TIMEZONE_KEY);
        if (currentTimezone == null) {
            currentTimezone = TimeZone.getDefault().getID();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("var profile = profile || {};\n\n");
        sb.append("profile.currentTimezone = '");
        sb.append(currentTimezone);
        sb.append("';\n\nprofile.timezones = [");
        for (String timezone : TimeZone.getAvailableIDs()) {
            sb.append("{timezone: '");
            sb.append(timezone);
            sb.append("'},");
        }
        sb.append("];\n\n");
        response.render(JavaScriptHeaderItem.forScript(sb.toString(), null));
    }
}
