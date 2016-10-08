/*
 * #%L
 * OAuth API
 * %%
 * Copyright (C) 2009 - 2013 Sakai Foundation
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.sakaiproject.oauth.filter;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.oauth.domain.Accessor;
import org.sakaiproject.oauth.exception.InvalidAccessorException;
import org.sakaiproject.oauth.service.OAuthHttpService;
import org.sakaiproject.oauth.service.OAuthService;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.sakaiproject.oauth.exception.OAuthException;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.user.api.UserDirectoryService;
import java.util.List;
import java.util.Arrays;
/**
 * First filter to apply with OAuth protocol
 * <p>
 * Checks the validity of the current request and OAuth parameters.
 * Sets a security advisor for the request.
 * </p>
 *
 * @author Colin Hebert
 */
public class OAuthPreFilter implements Filter {
    private String encoding;
    private OAuthHttpService oAuthHttpService;
    private OAuthService oAuthService;
    private SecurityService securityService;
    private UserDirectoryService userDirectoryService;
    private static Boolean isApiUserInit = false;
    private static String apiUserId = null;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.oAuthService = (OAuthService) ComponentManager.getInstance().get(OAuthService.class);
        this.oAuthHttpService = (OAuthHttpService) ComponentManager.getInstance().get(OAuthHttpService.class);
        this.securityService = (SecurityService) ComponentManager.getInstance().get(SecurityService.class);
        String initEncoding = filterConfig.getInitParameter(RequestFilter.CONFIG_CHARACTER_ENCODING);
        this.encoding = initEncoding != null ? initEncoding : "UTF-8";
        this.userDirectoryService = (UserDirectoryService) ComponentManager.getInstance().get(UserDirectoryService.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Force the character encoding for HTTP body content
        setEncoding(request);

        // Only apply filter if there is an OAuth implementation and a valid OAuth request
        if (oAuthHttpService == null || !oAuthHttpService.isEnabled()
                || !oAuthHttpService.isValidOAuthRequest(req, res)) {
            chain.doFilter(req, response);
            return;
        }

        try {
            String oAuthToken = oAuthHttpService.getOAuthAccessToken(req);
            final Accessor accessor = oAuthService.getAccessor(oAuthToken, Accessor.Type.ACCESS);

            // Determine the apiUser if we don't already know
            if (isApiUserInit == false) {
                String apiUserEid = ServerConfigurationService.getString("api.user", null);
                try{
                    if (apiUserEid != null){
                        apiUserId = this.userDirectoryService.getUserByEid(apiUserEid).getId();
                    }
                } catch (Exception e)  {
                    // Didn't find user, ignore the setting
                }
                isApiUserInit = true;
            }

            if (apiUserId != null && accessor.getUserId().equals(apiUserId)) {
                // This is a bit of a hack: determine if the request method and path are allowed to
                // the api user. (Mitch Golden 2016-10-25)
                String reqMethod = req.getMethod();
                String reqPath = req.getRequestURI();
                List allowedURI = Arrays.asList("gradebook", "membership", "snap-poll", "engagement", "user");
                String[] reqPathList = reqPath.split("/");
                boolean isAllowedURI = false;
                if (reqPathList.length >= 3) {
                    isAllowedURI = "direct".equals(reqPathList[1]) && allowedURI.contains(reqPathList[2]);
                }
                if (!"GET".equals(reqMethod) || !isAllowedURI ) {
                    throw new InvalidAccessorException("API user only allowed GET for " + allowedURI.toString());
                }
            }

            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(req) {
                @Override
                public Principal getUserPrincipal() {
                    return new Principal() {
                        public String getName() {
                            return accessor.getUserId();
                        }
                    };
                }
            };

            securityService.pushAdvisor(oAuthService.getSecurityAdvisor(accessor.getToken()));

            chain.doFilter(wrappedRequest, response);
        } catch (InvalidAccessorException e) {
            // TODO: Redirect to an error page
        }
    }

    /**
     * Sets the encoding for parameters submitted through a POST request.
     * <p>
     * If a charset isn't set manually, OAuth will try to decode some of the submitted parameters by the user with
     * ISO-8859-1 (those parameters are supposed to be in ASCII in the first place).<br />
     * Parameters are only decoded once, and {@link org.sakaiproject.util.RequestFilter} usually sets the charset
     * encoding itself, but the pre-filter reads the parameters first, so it needs to be done here.
     * </p>
     * <p>
     * Usually only parameters sent through a multipart/form-data POST request already provide the character encoding
     * used to encode the parameters.<br />
     * Parameters sent through a application/x-www-form-urlencoded POST request are expected to be only
     * ASCII characters.<br />
     * To avoid problems with developers who forget to set their POST forms in multipart/form-data, it will be assumed
     * that every client sends their forms (multipart/form-data and application/x-www-form-urlencoded POST forms)
     * using UTF-8.<br />
     * This might cause some problems if the client doesn't actually use UTF-8 though.
     * </p>
     * <p>
     * It's possible to specify an other encoding than "UTF-8" by setting the charset with in the filter configuration
     * parameter {@link RequestFilter#CONFIG_CHARACTER_ENCODING}
     * </p>
     *
     * @param request request on which the character encoding will be specified.
     * @throws UnsupportedEncodingException
     */
    private void setEncoding(ServletRequest request) throws UnsupportedEncodingException {
        request.setCharacterEncoding(this.encoding);
    }

    @Override
    public void destroy() {
    }
}
