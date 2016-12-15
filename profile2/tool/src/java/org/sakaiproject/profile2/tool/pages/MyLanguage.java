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

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.util.ResourceLoader;

public class MyLanguage extends BasePage implements IHeaderContributor {

	private static final Logger log = LoggerFactory.getLogger(MyLanguage.class);

	public MyLanguage() {
		disableLink(languageLink);
	}

    public void renderHead(final IHeaderResponse response) {

        Preferences preferences = sakaiProxy.getPreferences();

        ResourceProperties localeProps = preferences.getProperties(ResourceLoader.APPLICATION_ID);
        String currentLanguage = localeProps.getProperty(ResourceLoader.LOCALE_KEY);
        Locale currentLocale = sakaiProxy.getLocaleFromString(currentLanguage);

        String currentLocaleDisplay = currentLocale.getDisplayName();

        ResourceLoader rl = new ResourceLoader();

        String languageTabUrl = "/portal/site/" + sakaiProxy.getCurrentSiteId() + "/tool/" + sakaiProxy.getCurrentToolId() + "/language";

        StringBuilder sb = new StringBuilder();
        sb.append("var profile = profile || {};\n\n");
        sb.append("profile.languageTabUrl = '");
        sb.append(languageTabUrl);
        sb.append("';\n\nprofile.currentLanguage = '");
        sb.append(currentLanguage); // Used to select an item in the box
        sb.append("';\n\nprofile.currentLocaleDisplay = '");
        sb.append(currentLocaleDisplay); // Used to show the current language in the info box
        sb.append("';\n\nprofile.locales = [");
        for (Locale locale : sakaiProxy.getSakaiLocales()) {
            sb.append("{locale: '");
            sb.append(locale.toString());
            sb.append("', display: '");
            sb.append(rl.getLocaleDisplayName(locale));
            sb.append("'},");
        }
        sb.append("];\n\n");
        response.render(JavaScriptHeaderItem.forScript(sb.toString(), null));
    }
}
