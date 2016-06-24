package org.sakaiproject.blti.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.blti.util.RedirectUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Stanislav_Aytuganov on 18.05.2016.
 */
public class LtiRedirectServlet extends HttpServlet {
    private static Log M_log = LogFactory.getLog(LtiRedirectServlet.class);
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        M_log.info("Entering LtiRedirectServlet.doGet");
        M_log.info("Request:");
        M_log.info(req);
        M_log.info("Respond:");
        M_log.info(resp);
        RedirectUtils.redirectToLti(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        M_log.info("Entering LtiRedirectServlet.doPost");
        doGet(req, resp);
    }

}
