/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Template;
import freemarker.template.TemplateModel;

import org.timetablepublisher.table.TimeTableFactory;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.Params;

public class ViewServlet extends FreemarkerBaseServlet
{    
    private static final long serialVersionUID = -4575316288522971953L;

    
    protected boolean preTemplateProcess(
                          HttpServletRequest  req, 
                          HttpServletResponse resp,
                          Template            template,
                          TemplateModel       data,
                          Params              params,
                          Params              oldParams                          
        )
        throws ServletException, IOException     
    {   
        TimesTable tt = TimeTableFactory.create(params);
        if(req.getParameter("ALL") != null)
        {
            tt.setTimepoints(tt.getRouteStops());
            tt.process();
        }
        
        setParams(req, tt);
              
        return true;
    }
}
