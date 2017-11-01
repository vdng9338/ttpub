/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateModel;

import org.timetablepublisher.configure.loader.ConfigurationCacheSingleton;
import org.timetablepublisher.schedule.gtfs.loader.GTFSDataLoader;
import org.timetablepublisher.table.TimeTableFactory;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.LogUtils;
import org.timetablepublisher.utils.Params;
import org.timetablepublisher.utils.TimeTableProperties;


/**
 * The purpose of FreemarkerBaseServlet is to proivde a base level of functionality and Freemarker configuration to 
 * child servlets.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 19, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
abstract public class FreemarkerBaseServlet extends FreemarkerServlet implements Constants
{    
    private static final Logger LOGGER = Logger.getLogger(FreemarkerBaseServlet.class.getCanonicalName());
    
    private static final long serialVersionUID = 8317394560922063209L;
    private boolean     m_initialized        = false;
    protected String    m_fsPath             = null;
    protected String    m_templatesDir       = null;
    protected String    m_scheduleDataDir    = null;
    protected int       m_guid               = -111;
    
    
    /**
    * HOOK: eg, init() in HttpServlet parlance
    *
    * called when servlet detects in a request processing that global attribs are not yet set
    */
    protected void initializeServletContext(HttpServletRequest req, HttpServletResponse resp)
    {
        m_initialized = true;

        // get OS specific path to templates directory in web container
        ServletContext context = req.getSession().getServletContext();
        m_fsPath = context.getRealPath("/");
        if(!m_fsPath.endsWith("/") && !m_fsPath.endsWith("\\")) {
            m_fsPath += "/";
        }            
        m_templatesDir    = m_fsPath + TimeTableProperties.TEMPLATES_DIR.get("templates");
        m_scheduleDataDir = m_fsPath + TimeTableProperties.SCHEDULE_DIRECTORY.get("WEB-INF");

        // set the base dir of Schedule Data & Config Loaders
        ConfigurationCacheSingleton.setBaseDir(m_fsPath);
        GTFSDataLoader.setBaseDir(m_fsPath);

        // use [# ] syntax (or the old psudo-xml)
        Configuration cfg = getConfiguration();                      
        cfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);

        // allows freemarker to call any java bean methods
        BeansWrapper wrapper = new BeansWrapper();
        wrapper.setExposureLevel(BeansWrapper.EXPOSE_ALL);
        // TODO: might want to use EXPOSE_PROPERTIES_ONLY 
        cfg.setObjectWrapper(wrapper);
    }

    // HOOK: eg, session()
    //  Called when servlet detects session processing
    protected void initializeSession(HttpServletRequest req, HttpServletResponse resp)
    {
    }

    protected void postTemplateProcess(
                          HttpServletRequest  req, 
                          HttpServletResponse resp,
                          Template            template,
                          TemplateModel       data
        )
    {
        TimeTableFactory.cleanup();
    }
   

    /**
     * All Servlets that base themselves on this Abstract Freemarker helper class need to implement
     * this
     * 
     * @param req
     * @param resp
     * @param template
     * @param data
     * @param params
     * @param oldParams
     * @return
     * @throws ServletException
     * @throws IOException
     */
    abstract protected boolean preTemplateProcess(
            HttpServletRequest  req, 
            HttpServletResponse resp,
            Template            template,
            TemplateModel       data,
            Params              params,
            Params              oldParams            
    ) throws ServletException, IOException;

    protected boolean preTemplateProcess(
                          HttpServletRequest  req, 
                          HttpServletResponse resp,
                          Template            template,
                          TemplateModel       data
        )
        throws ServletException, IOException 
    {
        if(!m_initialized) initializeServletContext(req, resp);

        // get last page's parameters (for detecting whether there's been a change)
        String oldRouteStr = req.getParameter(OLD_ROUTE);
        String oldDirStr   = req.getParameter(OLD_DIR);
        String oldKeyStr   = req.getParameter(OLD_KEY);
        String oldDateStr  = req.getParameter(OLD_DATE);
        
        Params params    = new Params(req, m_scheduleDataDir);
        Params oldParams = new Params(oldRouteStr, oldDirStr, oldKeyStr, oldDateStr);
        params.equal(oldParams);
        
        // put these params back on the stack...so that they're uniformily available
        req.setAttribute(PARAMETERS,   new ParameterMap().getMap());
        req.setAttribute(AGENCY,       params.getAgency());
        req.setAttribute(ROUTE,        params.getRouteID());
        req.setAttribute(DIR,          params.getDir());
        req.setAttribute(KEY,          params.getKey());
        req.setAttribute(DATE,         params.getDate());
        req.setAttribute(DIFF_DATE,    params.getDiffDate());
        req.setAttribute(QUERY_STRING, req.getQueryString());
        req.setAttribute(THIS_URL,     req.getRequestURL());
        
        LogUtils.setLevel(req);
        
        // do the default param processing above, then hand control over to servlet to the real work 
        boolean retVal = preTemplateProcess(req, resp, template, data, params, oldParams);
        setDates(req,  params.getDate());
        
        return retVal;
    }
    
    static public void setParams(HttpServletRequest req, TimesTable tt)
    {
        if(tt == null || req == null) return;
        
        req.setAttribute(TIMES_TABLE, tt);
        if(tt.getRouteNames()     != null) req.setAttribute(ROUTE_LIST,     tt.getRouteNames());   
        if(tt.getAgencyName()     != null) req.setAttribute(AGENCY,         tt.getAgencyName());
        if(tt.getRouteID()        != null) req.setAttribute(ROUTE,          tt.getRouteID());
        if(tt.getDir().toString() != null) req.setAttribute(DIR,            tt.getDir().toString());
        if(tt.getKey().toString() != null) req.setAttribute(KEY,            tt.getKey().toString());
        if(tt.getDate()           != null) req.setAttribute(EFFECTIVE_DATE, tt.getDate());
    }

    
    synchronized static public void setDates(HttpServletRequest req, String date)
    {
        // set today's date
        req.setAttribute(TODAY, new Date());
        
        // set the schedule effective date
        try
        {
            req.setAttribute(EFFECTIVE_DATE, dateSDF.parse(date));
        }
        catch(Exception e) 
        {
            // TODO: log date parse error 
            req.setAttribute(EFFECTIVE_DATE, new Date());
        }        
    }
    
    // TODO: is there a dead-lock problem here?  should BAOS be in a separate thread from resp?
    public void outputPdf(ByteArrayOutputStream os, HttpServletResponse resp) throws IOException 
    {
        //setting some response headers
        resp.setHeader("Expires", "0");
        resp.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        resp.setHeader("Pragma", "public");
        
        //setting the content type
        resp.setContentType("application/pdf");
        
        // the contentlength is needed for MSIE!!!        
        resp.setContentLength(os.size());
        
        // write ByteArrayOutputStream to the ServletOutputStream
        ServletOutputStream out = resp.getOutputStream();
        os.writeTo(out);
        out.flush();
    }
}
