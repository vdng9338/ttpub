/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Template;
import freemarker.template.TemplateModel;

import org.timetablepublisher.table.TimeTableFactory;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.FreemarkerDate;
import org.timetablepublisher.utils.HttpSelectedTableParams;
import org.timetablepublisher.utils.Params;
import org.timetablepublisher.view.BatchGenerator;
import org.timetablepublisher.view.BatchParameters;
import org.timetablepublisher.view.FreemarkerBase;
import org.timetablepublisher.view.ZipTimeTables;
import org.timetablepublisher.view.pdf.PdfMultiTableDoc;
import org.timetablepublisher.view.pdf.TrimetPdfDesignImpl;

import fr.vdng9338.gtfs.ttpubmod.FrenchPdfDesignImpl;

@SuppressWarnings("unchecked")
public class PrintTimeTablesServlet extends FreemarkerBaseServlet
{
    private static final Logger LOGGER = Logger.getLogger(PrintTimeTablesServlet.class.getCanonicalName());    
    private static final long serialVersionUID = -5575316288522971953L;

    File       m_zipDir       = null;
    String     m_zipDirPath   = null;

    File       m_testsDir     = null;
    String     m_testsDirPath = null;
    
    
    protected void initializeServletContext(HttpServletRequest req, HttpServletResponse resp)            
    {
        super.initializeServletContext(req, resp);
        
        try
        {
            m_zipDirPath = m_fsPath + "/zips/";
            m_zipDir = new File(m_zipDirPath);
            m_zipDir.mkdir();

            m_testsDirPath = m_fsPath + TESTS_FOLDER;
            m_testsDir = new File(m_fsPath + TESTS_FOLDER);
            m_testsDir.mkdir();            
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
        // set freemaker params for zip dir & file...also handle any delete zip requests
        req.setAttribute(UTILS, new FreemarkerDate(req, params.getDate()));
        req.setAttribute(ZIP_DIR,  m_zipDir);   
        req.setAttribute(ZIP_LIST, m_zipDir.listFiles());
        delete(req);

        // do the table rendering work
        TimesTable tt = TimeTableFactory.create(params);
        List selected = HttpSelectedTableParams.parse(req);
        if(selected.size() > 0)
        {
            // 
            // Customer selected some number of routes/directions on the webpage
            // so now we either generate a PDF file (for preview), or a ZIP of Indesign XML files
            //
            if(doPdf(req))
            {
                // do the processing to create the PDF
                ByteArrayOutputStream os = multiTablePdf(selected, params);

                // now output that PDF back to the client
                outputPdf(os, resp);
                return false;  // pdf output -- no further processing needed beyond this point
            }
            
            // NOT DOING A PDF, SO DO THE ZIP
            try
            {           
                if(doHtmls(req))
                {
                    BatchParameters batchParams = new BatchParameters(params);
                    batchParams.setSelectionList(selected);
                    batchParams.initAllButAllStops();
                    batchParams.setTemplatesDir(m_templatesDir);
                    batchParams.setPreview(req.getParameter(PREVIEW) != null);
                    File zipFile = File.createTempFile("HTML_" + zipName(selected), ".zip", m_zipDir); 
                    BatchGenerator.zip(zipFile, params.getDate(), batchParams);
                    req.setAttribute("zipFile", zipFile);
                }
                else
                {
                    makeTests(selected, params, "testTemplates/viewTest.web", "testTemplates/trimetOrgTest.web");
                    if(!justTests(req))
                    {
                        File zipFile = File.createTempFile(zipName(selected), ".zip", m_zipDir);               
                        makeIndesignZip(zipFile, routeNames(selected), "indesign.bcid", selected, params); 
                        req.setAttribute("zipFile", zipFile);
                    }
                }
            }
            catch (Exception e)
            {
                LOGGER.log(DEBUG, "making tests/zip failed", e);
            }
        }

        setParams(req, tt);
        
        return true;
    }    

    
    /**
     * Create a PDF file with multiple Times Tables, based on user input
     * 
     * @param selected
     * @param svcDate
     * @return
     */
    public ByteArrayOutputStream multiTablePdf(List<HttpSelectedTableParams> selected, Params params)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();            
        PdfMultiTableDoc pdf = null;
        
        try
        {
            pdf = new PdfMultiTableDoc(new FrenchPdfDesignImpl(), baos, params.getDate());
            
            // create N schedules with 2..N number of time points (good for creating InDesign Templates)
            for(HttpSelectedTableParams sel : selected)
            {
                //
                // PROCESS:
                // 1. Get TimeTable Data
                // 2. Tell the ZIP file about the timetable file your about to add
                // 3. Format the timetable data according to the freemarker template
                // NOTE: we stream the Freemarker template result to the ZIP stream
                // 4. Tell the ZIP file to close this timetable
                //

                // step 1: get the time-table from our data store
                params.setParams(sel);
                TimesTable tt = TimeTableFactory.create(params);

                if(tt == null || tt.getDestination() == null || tt.getDestination().equals("what-happened")
                   || tt.getTimePoints() == null || tt.getTimePoints().size() < 2
                   || tt.getTimeTable()  == null || tt.getTimeTable().size()  < 1)
                {
                    LOGGER.log(DEBUG, "this time table looks wrong, so we're going to skip it");
                    continue;
                }
                
                pdf.addTable(tt);
            }
        }
        catch (Exception e)
        {
            LOGGER.log(SEVERE, "something bad happened generating the zip file\n" + e.toString());
        }
        finally
        {
            if(pdf != null) pdf.close();
        }
        
        return baos;
    }
    
    
    /**
     * Create a ZIP file of multiple Times Tables / InDesign XML files, based on user input
     * 
     * @param zipFile
     * @param lines
     * @param template
     * @param selected
     * @param svcDate
     * @param bypassConfig
     * @throws Exception
     */
    public void makeTests(List<HttpSelectedTableParams> selected, Params params, String appTestTemplate, String webTestTemplate)
       throws Exception            
    {
        try
        {
            // create N schedules with 2..N number of time points (good for creating InDesign Templates)
            for(HttpSelectedTableParams sel : selected)
            {
                if(sel == null || sel.getRoute() == null || sel.getRoute().length() < 1 || sel.getDir() == null || sel.getKey() == null) continue;
                
                //
                // PROCESS:
                // 1. Get TimeTable Data
                // 2. Tell the ZIP file about the timetable file your about to add
                // 3. Format the timetable data according to the freemarker template
                // NOTE: we stream the Freemarker template result to the ZIP stream
                // 4. Tell the ZIP file to close this timetable
                //

                // step 1: get the time-table from our data store
                TimesTable tt = makeTable(params, sel);
                if(tt == null) continue;

                String testFileName = tt.getRouteID()     + "-" +   
                                      tt.getDestination() + "-" + 
                                      tt.getKeyName()     +
                                      ".html";
                testFileName = testFileName.replace('/',  ' ');
                testFileName = testFileName.replace('\\', ' ');
                testFileName = testFileName.replace('&',  ' ');                

                FreemarkerBase.process(tt, m_testsDirPath + "/" + WEB_TEST_PREFIX + testFileName, m_templatesDir, webTestTemplate);
                FreemarkerBase.process(tt, m_testsDirPath + "/" + APP_TEST_PREFIX + testFileName, m_templatesDir, appTestTemplate);
            }
        }
        catch (Exception e)
        {
            LOGGER.log(SEVERE, "something bad happened generating the tests\n" + e.toString());
        }
        finally
        {
        }
    }
    
    
    /**
     * Create a ZIP file of multiple Times Tables / InDesign XML files, based on user input
     * 
     * @param zipFile
     * @param lines
     * @param template
     * @param selected
     * @param svcDate
     * @param bypassConfig
     * @throws Exception
     */
    public void makeIndesignZip(File zipFile, String lines, String template, List<HttpSelectedTableParams> selected, Params params)
       throws Exception            
    {
        ZipTimeTables po = null;
        OutputStream fs = null;

        try
        {
            fs = new FileOutputStream(zipFile);
            po = new ZipTimeTables(m_templatesDir, template, fs);

            String comment = "Created on: " + new Date() + "\r\n\r\n";
            comment += "PARAMETERS: \r\n";
            comment += "  - Table Type: "   + params.getTableType() + "\r\n";
            comment += "  - Service Date: " + params.getDate() + "\r\n";
            comment += "  - Routes:       " + lines.replaceAll("_", ", ") + "\r\n";
            po.getZip().setComment(comment);

            // create N schedules with 2..N number of time points (good for creating InDesign Templates)
            for(HttpSelectedTableParams sel : selected)
            {
                //
                // PROCESS:
                // 1. Get TimeTable Data
                // 2. Tell the ZIP file about the timetable file your about to add
                // 3. Format the timetable data according to the freemarker template
                // NOTE: we stream the Freemarker template result to the ZIP stream
                // 4. Tell the ZIP file to close this timetable
                //

                // step 1: get the time-table from our data store
                TimesTable tt = makeTable(params, sel);
                if(tt == null) continue;
                
                String xmlFileName = "rt" + tt.getRouteID()     + "-"     
                                          + tt.getDestination() + "-" 
                                          + tt.getKeyName()     
                                          + "_cols-" + tt.getTimePoints().size() 
                                          + ".xml";
                xmlFileName = xmlFileName.replace('/',  ' ');
                xmlFileName = xmlFileName.replace('\\', ' ');
                xmlFileName = xmlFileName.replace('&',  ' ');

                po.addParams(TIMES_TABLE, tt);

                // step 2: name the xml file you're about to stream into the ZIP file
                po.addFileToZip(xmlFileName);

                // step 3: have freemarker stream the XML via the template engine
                po.templateProcess();

                // step 4: tell ZIP to close this XML file
                po.getZip().closeEntry();                
            }
        }
        catch (Exception e)
        {
            LOGGER.log(SEVERE, "something bad happened generating the zip file\n", e);
        }
        finally
        {
            // Complete the ZIP file
            po.close();
        }
    }
            
    public TimesTable makeTable(Params params, HttpSelectedTableParams sel)
    {
        TimesTable retVal = null;
        try
        {
            params.setParams(sel);
            TimesTable tt = TimeTableFactory.create(params);
            
            if(tt == null || tt.getDestination() == null 
                          || tt.getRouteID()     == null || tt.getRouteID().length()  < 1
                          || tt.getTimePoints()  == null || tt.getTimePoints().size() < 2
                          || tt.getTimeTable()   == null || tt.getTimeTable().size()  < 1)
            {     
                     LOGGER.log(DEBUG, sel.description() + " timetable looks wrong, so we're going to skip it");
            }
            else
            {
                retVal = tt;
            }
        }
        catch(Exception e)
        {
            LOGGER.log(DEBUG, "something bad happened creating the times table\n", e);
            retVal = null;
        }                        
        
        return retVal;
    }


    /**
     * Simple Utility Method to get the route names out of the Selected List of HTTP table parameters
     * 
     * @param  selected
     * @return String
     */
    public String routeNames(List<HttpSelectedTableParams> selected)
    {
        if(selected == null || selected.size() < 1) return "";
        
        String retVal = "";
        String lastRt = "";
        for(HttpSelectedTableParams sel : selected)
        {
            // only want to add valid strings to the route name -- and only add them once
            if(sel == null || sel.getRoute() == null) continue;
            if(lastRt.equals(sel.getRoute()))         continue;
            
            lastRt = sel.getRoute();
            retVal += lastRt + "_";
        }
        
        // strip off that last '_' character, added above
        if(retVal.length() > 1) {
            retVal = retVal.substring(0, retVal.length() - 1);
        }
        
        return retVal;
    }

    
    /**
     * Simple Utility Method to make a Zip File name based on the Routes being selected
     * 
     * @param  selected
     * @return String
     */
    public String zipName(List<HttpSelectedTableParams> selected)
    {
        if(selected == null || selected.size() < 1) return "";
        
        String retVal = "";
        
        String first = selected.get(0).getRoute();
        String last  = selected.get(selected.size() - 1).getRoute();
        String combo = routeNames(selected);

        int numRoutes = 0;
        if(combo.contains("_"))
        {
            String x[] = combo.split("_");
            if(x != null) {
                numRoutes = x.length;
            }
        }
        
        retVal = numRoutes > 1 ? "routes_" : "route_";
        if(numRoutes > 26) retVal += first + "..." + last + "__";
        else               retVal += combo + "__";
        
        return retVal;
    }


    /**
     * Simple Utility Method to check HTTP params as to whether a PDF should be generated
     * 
     * @param  req
     * @return boolean
     */
    public boolean doPdf(HttpServletRequest req)
    {
        String sub = req.getParameter(SUBMIT);
        if(sub != null && (sub.equals(PDF) || sub.equals(BYPASS))) 
            return true;
        
        return false;
    }

    
    public boolean doHtmls(HttpServletRequest req)
    {
        String sub = req.getParameter(SUBMIT);
        
        if(sub != null && sub.equals(HTMLS_ZIP))
            return true;
                
        return false;        
    }
    
    /**
     * Simple Utility Method to check HTTP params as to whether a PDF should be generated
     * 
     * @param  req
     * @return boolean
     */
    public boolean justTests(HttpServletRequest req)
    {
        String sub   = req.getParameter(SUBMIT);
        
        if(sub != null && sub.equals(JUST_TESTS))
            return true;
                
        return false;
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
                    for(File f : m_zipDir.listFiles())                            
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
                            
                    req.setAttribute(ZIP_LIST, m_zipDir.listFiles());
                }
            }                
        }        
    }    
}
