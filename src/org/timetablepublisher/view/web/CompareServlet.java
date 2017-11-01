/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Template;
import freemarker.template.TemplateModel;

import org.timetablepublisher.table.TimeTableFactory;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.TimeTableCompare;
import org.timetablepublisher.utils.Params;
import org.timetablepublisher.view.pdf.PdfCompareDesignImpl;
import org.timetablepublisher.view.pdf.PdfCompareTableDoc;

public class CompareServlet extends FreemarkerBaseServlet
{    
    private static final long serialVersionUID = -4579998522971953L;
    private static final Logger LOGGER = Logger.getLogger(CompareServlet.class.getCanonicalName());
    
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
        TimesTable tt = null;
        TimesTable A  = null;
        TimesTable B  = null;
        
        String selected[] = req.getParameterValues(RT_SELECT_FORM);
        if(selected != null && selected.length >= 1
           &&  params.getDate() != null && params.getDiffDate() != null 
           && !params.getDate().equals(params.getDiffDate()))
        {
            List<TimeTableCompare> diffList = new ArrayList<TimeTableCompare>();
            String buffer = req.getParameter(TIME_BUFFER);
            for(String rt : selected)
            {
                if(rt == null || rt.length() < 1) continue;
                params.setRouteID(rt);
                params.seperateAgencyRoute();
                
                // compare inbound & outbound, for each of the three service days
                KeyType[] keys = {KeyType.Weekday, KeyType.Saturday, KeyType.Sunday};
                for(KeyType k : keys)
                {
                    params.setKey(k);
                    
                    // do inbound compare
                    params.setDir(DirType.Inbound);                    
                    A = TimeTableFactory.create(params, params.getDate());
                    B = TimeTableFactory.create(params, params.getDiffDate());
                    diffList.add(new TimeTableCompare(A, B, buffer));

                    // do outbound compare                    
                    params.setDir(DirType.Outbound);
                    A = TimeTableFactory.create(params, params.getDate());
                    B = TimeTableFactory.create(params, params.getDiffDate());
                    diffList.add(new TimeTableCompare(A, B, buffer));
                    
                    tt = A;
                }
            }
            
            if(doPdf(req)) 
            {
                PdfCompareTableDoc pdf = null;
                try
                {
                    // do the processing to create the PDF
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();            
                    pdf = new PdfCompareTableDoc(diffList, new PdfCompareDesignImpl(A, B), baos, params.getDate(), false); 
                    outputPdf(baos, resp);
                }
                catch (Exception e)
                {
                    LOGGER.log(SEVERE, "something bad happened generating the zip file\n" + e.toString());
                }
                finally
                {
                    if(pdf != null)
                    {
                        pdf.close();
                        return false;  // pdf output -- no further processing needed beyond this point
                    }
                }
            }

            req.setAttribute(TT_DIFF_LIST, diffList);
        }

        if(tt == null) {
            tt = TimeTableFactory.create(params);
        }        
        setParams(req, tt);
        
        return true;
    }
    
    public boolean doPdf(HttpServletRequest req)
    {
        String sub = req.getParameter(SUBMIT);
        if(sub != null && (sub.equals(PDF)))
            return true;
        
        return false;
    }    
}
