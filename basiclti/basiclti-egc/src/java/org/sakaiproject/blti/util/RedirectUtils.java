package org.sakaiproject.blti.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Stanislav_Aytuganov on 18.05.2016.
 */
public class RedirectUtils {
    public static void redirectToLti(HttpServletRequest req, HttpServletResponse res) {
        String siteId = req.getParameter("siteId");
        String externalToolId = req.getParameter("externalToolId");
        try {
//            res.sendRedirect(res.encodeRedirectURL(siteId + "/" + externalToolId));
            res.sendRedirect("http://sakai10.esynctraining.com:8080/portal/site/" + siteId + "/page/" + externalToolId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
