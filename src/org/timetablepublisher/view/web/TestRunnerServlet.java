/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.web;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Template;
import freemarker.template.TemplateModel;

import org.timetablepublisher.utils.FreemarkerDate;
import org.timetablepublisher.utils.KeyValue;
import org.timetablepublisher.utils.Params;
import org.timetablepublisher.view.FreemarkerProcessor;

/**
 * The purpose of TestRunnerServlet is to build and manage test suites out of individual tests (which are
 * generated on an ongoing basis by the Print servelet -- eg: the idea being that printing means that a 
 * timetable is in a ready state, so the test captured at that point represents correctness).
 * 
 * Tests require Firefox and Selenium IDE to be installed.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Dec 6, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 * 
 * @see     org.timetablepublisher.view.web.PrintTimeTables
 * @see     http://www.openqa.org/selenium-ide/
 */
/**
 * The purpose of TestRunnerServlet is to TODO
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Dec 8, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class TestRunnerServlet extends FreemarkerBaseServlet
{
    private static final Logger LOGGER = Logger.getLogger(TestRunnerServlet.class.getCanonicalName());    
    private static final long serialVersionUID = -5575316288522971953L;

    File       m_testsDir     = null;
    String     m_testsDirPath = null;
    String     m_testsDirURL  = null;

    
    protected void initializeServletContext(HttpServletRequest req, HttpServletResponse resp)            
    {
        super.initializeServletContext(req, resp);
        
        try
        {
            m_testsDirPath = m_fsPath + TESTS_FOLDER;
            m_testsDir = new File(m_fsPath + TESTS_FOLDER);
            m_testsDir.mkdir();
            
            StringBuffer url = req.getRequestURL();
            if(url != null)
            {
                String tmp = url.substring(0, url.lastIndexOf("/") + 1);
                m_testsDirURL = tmp + TESTS_FOLDER;
            }
        }
        catch (RuntimeException e)
        {
            LOGGER.log(DEBUG, "couldn't create a ZIPS directory...probably not a big deal...might be a WEB APP SERVER security config thing", e);
        }
    }
    
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
        delete(req);
        
        parseApp(req);
        parseWeb(req);
        
        req.setAttribute(UTILS, new FreemarkerDate(req, params.getDate()));
        req.setAttribute(TEST_DIR,        m_testsDir);
        req.setAttribute(TEST_DIR_URL,    m_testsDirURL);
        req.setAttribute(WEB_TESTS,       m_testsDir.listFiles(new WebFilter()));
        req.setAttribute(APP_TESTS,       m_testsDir.listFiles(new AppFilter()));
        req.setAttribute(TEST_SUITE_LIST, m_testsDir.listFiles(new SuiteFilter()));
        
        return true;
    }    


    public void parseApp(HttpServletRequest req)
    {
        parse(req, APP_SUITE_NAME, "AppTest", APP_TESTS);
    }
    
    public void parseWeb(HttpServletRequest req)
    {
        parse(req, WEB_SUITE_NAME, "WebTest", WEB_TESTS);        
    }
    
    
    
    /**
     * Makes TestSuite out of list of files...
     * 
     * @param req
     * @param suiteName
     * @param type
     * @param tests
     */
    public void parse(HttpServletRequest req, String suiteName, String type, String tests)
    {
        String sname = req.getParameter(suiteName);
        String name  = "suite" + type;
        if(sname != null)
        {
            name = name + sname;
        }
        name += ".html";
        
        String selected[] = req.getParameterValues(tests);
        if(selected != null && selected.length > 0)
        {
            FreemarkerProcessor fp = new FreemarkerProcessor(m_testsDirPath + "/" + name, m_templatesDir, TEST_SUITE_FTL);            
            fp.addParams(TEST_SUITE, KeyValue.toList(selected));
            fp.process();
        }
    }
    
    
    /**
     * Simple Utility Method to delete files off the server, based on HTTP params specified by user
     * 
     * @param req
     */
    public void delete(HttpServletRequest req)
    {
        for(Object o : req.getParameterMap().keySet())
        {
            String param = (String)o;
                            
            if(param != null && param.startsWith(DELETE))
            {
                // delete old files
                String fileName = param.substring(DELETE.length()).trim();
                if(fileName != null)
                {                        
                    for(File f : m_testsDir.listFiles())                            
                    {
                        if(f == null || f.getName() == null) continue;
                        if(fileName.equals(f.getName()))
                        {
                            try
                            {
                                f.delete();
                            }
                            catch (RuntimeException e)
                            {
                                LOGGER.log(DEBUG, e.toString());                
                            }
                        }
                    }
                            
                    req.setAttribute(ZIP_LIST, m_testsDir.listFiles());
                }
            }                
        }        
    }

    
    
    /**
     * Simple Utility Method to delete files off the server, based on HTTP params specified by user
     * 
     * @param req
     */
    public void delete(HttpServletRequest req, FilenameFilter ff)
    {
        for(Object o : req.getParameterMap().keySet())
        {
            String param = (String)o;
                            
            if(param != null && param.startsWith(DELETE))
            {
                // delete old files
                String fileName = param.substring(DELETE.length()).trim();
                if(fileName != null)
                {                        
                    for(File f : m_testsDir.listFiles(ff))                            
                    {
                        if(f == null || f.getName() == null) continue;
                        if(fileName.equals(f.getName()))
                        {
                            try
                            {
                                f.delete();
                            }
                            catch (RuntimeException e)
                            {
                                LOGGER.log(DEBUG, e.toString());                
                            }
                        }
                    }
                            
                    req.setAttribute(ZIP_LIST, m_testsDir.listFiles());
                }
            }                
        }        
    }
    
    class SuiteFilter implements FilenameFilter 
    {
        public boolean accept(File dir, String name) 
        {
            return (name.startsWith(SUITE_PREFIX));
        }
    }
    class AppFilter implements FilenameFilter 
    {
        public boolean accept(File dir, String name) 
        {
            return (name.startsWith(APP_TEST_PREFIX));
        }
    }
    class WebFilter implements FilenameFilter 
    {
        public boolean accept(File dir, String name) 
        {
            return (name.startsWith(WEB_TEST_PREFIX));
        }
    }    
}
