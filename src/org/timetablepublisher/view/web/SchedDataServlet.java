/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.web;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.timetablepublisher.schedule.gtfs.loader.GTFSDataLoader;

public class SchedDataServlet extends HttpServlet
{    
    private static final long serialVersionUID = -555556288522971953L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String msg = "";
        if(req.getParameter("RELOAD") != null)
        {
            msg += "<BR/>RELOADED<BR/>";
            GTFSDataLoader.reloader();
        }

        String agency = req.getParameter("DIR_FIX");
        if(agency != null)
        {
            msg += "<BR/>DIR FIXED for Agency " + agency + "<BR/>";
            GTFSDataLoader.fixDirection(agency);
        }
        
        ServletOutputStream out = resp.getOutputStream();
        out.println(msg);
        out.flush();
    }
}