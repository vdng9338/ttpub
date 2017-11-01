/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.cmdline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.timetablepublisher.table.RouteDescription;
import org.timetablepublisher.table.TableUtils;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimeTableFactory.TableType;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.FileUtils;
import org.timetablepublisher.utils.KeyValue;
import org.timetablepublisher.view.FreemarkerBase;
import org.timetablepublisher.view.ZipTimeTables;
import org.timetablepublisher.view.pdf.TrimetPdfDesignImpl;
import org.trimet.ttpub.view.pdf.TrimetWebPdfDesignImpl;


/**
 * The purpose of TimesTableBatchGenerator is to batch process a schedule, producing all valid time tables,
 * and returning a ZIP file full of html & pdf time tables.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 26, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class TimesTableBatchGenerator extends TTCmdLineBase implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(TimesTableBatchGenerator.class.getCanonicalName());
    
    public TimesTableBatchGenerator()
    {
        this("trimet");
    }
    public TimesTableBatchGenerator(String agPath)
    {
        // set up some defaults (can be over written via the cmd line)
        m_tableType          = TableType.TRANS;
        m_agency             = TRIMET;
        m_fmRedirectPath     = "include/redirect.ftl";
        m_fmHtmlPath         = agPath + "/basic.web";
        m_fmVerticalPath     = agPath + "/vert.web";             
        m_fmMapPath          = agPath + "/map.web";
        m_fmAllStopPath      = agPath + "/allstops.web";
        m_pdfTemplate        = new TrimetWebPdfDesignImpl();
    }
    
    public void oneOff()
    {        
        TimesTable tt = makeTable();
        TableUtils.print(tt);
        FreemarkerBase.process(tt, m_outHtml, m_fmTemplateDir, m_fmHtmlPath);
        if(m_genVert) {
            FreemarkerBase.process(tt, m_outHtml + "l",  m_fmTemplateDir, m_fmVerticalPath);
        }
        if(m_genMaps) {
            FreemarkerBase.process(tt, m_outMap,  m_fmTemplateDir, m_fmMapPath);
        }
    }

    
    /**
     * this method is the heart of the batch processor:
     *    1. loop through all of the routes in this schedule 
     *    2. loop through all of the keys for each route
     *    3. loop through all of the directions for the route / key 
     *    4. Get TimeTable Data
     *    5. Build a file name based on TriMet file / path naming
     *    6. Format the timetable data according to the freemarker / iText template
     *       NOTE: we stream the template results to the ZIP stream
     * 
     * @param po
     * @param plainTemplate
     * @param mapTemplate
     * @return int
     * @throws Exception
     */
    public void process(ZipTimeTables po)
        throws Exception
    {
        setTemplates(po);
        
        LOGGER.log(DEBUG, "getting the list of routes");
        KeyType keys[] = KeyType.getWkSaSu();
        DirType dirs[] = DirType.getInboundOutbound();
        TimesTable routes = makeTable("4");
        m_estimatedNumOfFiles = routes.getRouteNames().size() * dirs.length * (keys.length - 1) + 19;
        po.addParams(SHOW_VERT,  m_genVert);
        po.addParams(ALL_STOPS,  m_allStops);
        po.addParams(PREVIEW,    m_preview);

        List<KeyValue> tests = new ArrayList<KeyValue>();
        
        // step 1: loop through all of the routes in this schedule
        LOGGER.log(DEBUG, "starting to loop through routes");
        for(RouteDescription rd : routes.getRouteNames())
        {   
            if(!m_once) 
            {
                m_route = rd.getRouteID();    
            }
            
            // step 2: loop through all of the keys for each route
            for(int k = 0; k < keys.length; k++)
            {            
                KeyType key = keys[k];
                m_key = key;
                
                // directories are names one of w/s/h/ (h == holiday and/or sunday)                    
                String  directory = (key == KeyType.Sunday) ? "h" : keys[k].value().toLowerCase();  
                                
                // step 3: loop through all of the directions for the route / key 
                for(int d = 0; d < dirs.length; d++)
                {                    
                    System.out.print(".");
                    m_dir    = dirs[d];

                    // step 4: get the time-table from our data store
                    TimesTable tt = makeTable();
                    
                    // step 5: build a file name based on TriMet file / path naming convention, and log the result
                    String fileName = getFileName(tt); 
                    String pathName = directory + "/" + fileName;
                    logReport(tt, m_route, m_dir, m_key, m_svcDate, pathName + ".htm");
                    if(tt == null) continue;
                    LOGGER.log(DEBUG, tt.getRouteDescription());            
                    po.addParams(TIMES_TABLE, tt);

                    // step 6: produce the HTML, MAP, PDF and (if desired) ALL STOPS  
                    makeHtml(tt, po, pathName     + ".htm",    m_htmlTemplate);
                    if(m_genMaps) 
                    {
                        makeHtml(tt, po, pathName + "map.htm", m_mapTemplate);
                    }
                    if(m_genVert)
                    {
                        makeHtml(tt, po, pathName + ".html",   m_verticalTemplate);
                    }
                    makePdf(tt, po, pathName  + ".pdf",    m_pdfTemplate);
                    

                    // step 6a: create the ALL STOPS schedule 
                    if(m_allStops)
                    {
                        LOGGER.log(DEBUG, "ALL STOPS: " + tt.getRouteDescription());            
                        tt = makeAllStopsTable(tt.getRouteStops(), m_route, dirs[d], key);
                        if(tt != null)
                        {
                            po.addParams(TIMES_TABLE, tt);
                            makeHtml(tt, po, pathName + "all.htm", m_allStopTemplate);
                        }
                    }

                    
                    // step 6b: create tests
                    if(m_genTests) 
                    {
                        String appTest = "TEST/app/" + pathName + ".html";
                        String webTest = "TEST/web/" + pathName + ".html";
                        makeHtml(tt, po, appTest, m_appTestTemplate);                    
                        makeHtml(tt, po, webTest, m_webTestTemplate);
                        try 
                        {
                          tests.add(new KeyValue(tt.getRouteDescription(), pathName + ".html"));
                        } 
                        catch(Exception e) 
                        {                            
                        }
                    }                    
                } // dirs 
            } // keys
            if(m_once) break;
        } // routes

        
        // step 7a: create small index.html files which have a redirection back to the route landing page
        if(m_genRedirects) 
        {
            // step 7b: loop through all of the keys for each route
            for(int k = 0; k < keys.length; k++)
            {            
                KeyType key = keys[k];
                
                // directories are names one of w/s/h/ (h == holiday and/or sunday)                    
                String  directory = (key == KeyType.Sunday) ? "h" : keys[k].value().toLowerCase();  
                makeHtml(null, po, directory + "/index.html", m_redirectTemplate);
            }
        }
        
        // step 8: generate Selenium IDE acceptence tests
        if(m_genTests) 
        {
            String appSuite = "TEST/app/TestSuite.html";
            po.addParams(TEST_TYPE,  "app");
            po.addParams(TEST_SUITE, tests);
            makeHtml(null, po, appSuite, m_testSuiteTemplate); 
            
            String webSuite = "TEST/web/TestSuite.html";
            po.addParams(TEST_TYPE,  "web");        
            po.addParams(TEST_SUITE, tests);
            makeHtml(null, po, webSuite, m_testSuiteTemplate);
        }
    }
    
    
    public String getFileName(TimesTable tt)
    {
        String retVal = "x1";
        
        try
        {
            // this is the TRIMET naming convention for static timetable on the web
            // eg: t1[route id]_[dir of 1 or 0]
            // or: t1004_1.htm and t1004_0.htm (inbound & outbound, respectively)
            String fileName = "t1" + FileUtils.pad(3, "0", tt.getRouteID());
            retVal = fileName;
            
            fileName += "_"+ tt.getDir().value();
            retVal = fileName;
        }
        catch (Exception e)
        {
        }

        return retVal;
    }

    synchronized public void zip()
    {
        String today = dateSDF.format(new Date());
        ZipTimeTables po = null;

        try 
        {                       
            FileUtils.mkdir(m_zipDirName);
            FileUtils.rename(m_zipDirName, m_zipFile);

            LOGGER.log(DEBUG, "create zip file: " + m_zipDirName + m_zipFile); 
            File f  = new File(m_zipDirName + m_zipFile);
            OutputStream fs = new FileOutputStream(f);
            po = new ZipTimeTables(m_fmTemplateDir, m_fmHtmlPath, fs);

            // zip dir
            File zipDir = new File(m_zipDirName);
            zipDir.mkdir();       
            LOGGER.log(DEBUG, "set zip comment");
            String comment = "Created on: "        + today + "\n\n";
                   comment += "PARAMETERS: \n";
                   comment += "  - Service Date: " + m_svcDate        + "\n";
                   comment += "  - Routes: "       + "all routes"     + "\n";
                   comment += "  - Directions: "   + "Both Inbound / Outbound" + "\n";
                   comment += "  - Service Keys: " + "W, S, U"        + "\n";
            po.getZip().setComment(comment);
            
            if(!(m_tableType == TableType.TRANS)) 
            {
                m_pdfTemplate = new TrimetPdfDesignImpl();
            }
            
            process(po);
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, "VERY VERY BAD: TimeTable Batch Process Ended PreMaturely: " + e.toString(), e);
        }
        finally
        {
            // Complete the ZIP file
            LOGGER.log(DEBUG, "closing the zip file");
            
            if(m_generateReport && m_report != null)
            {
                m_report.note("File Report:", "Numbers");
                m_report.note("Estimated number of Files:", m_estimatedNumOfFiles  + "");
                m_report.note("HMTLs Generated:",           m_actualNumOfHtmlFiles + "");
                m_report.note("PDFs Generated:",            m_actualNumOfPdfFiles  + "");
                m_report.close();
                try
                {
                    LOGGER.log(DEBUG, "adding the report " + REPORT_NAME + " to the zip file");
                    po.addFileToZip(REPORT_NAME, m_report.getReport());
                }
                catch(Exception e)
                {
                    LOGGER.log(SEVERE, "couldn't add the report " + REPORT_NAME + " to the zip file", e);
                }
            }
            
            if(po != null) po.close();
        }
        
        LOGGER.info("\n\n\n>>>>>>> File Report:\n\tEstimated = " + m_estimatedNumOfFiles  + 
                " HTML and PDFs.\n\tActual number of HTML: " + m_actualNumOfHtmlFiles +
                "\n\tActual number of PDFs: "                + m_actualNumOfPdfFiles);        
    }
    

    public void run()
    {   
        init();
        if(m_zipFile != null)
        {
             zip();
        }
        else
        {
            oneOff();
        }
    }
    

    public static void main(String[] args) 
    {
        TimesTableBatchGenerator bean = new TimesTableBatchGenerator();
        CmdLineParser parser = new CmdLineParser(bean);
        try 
        {
            parser.parseArgument(args);
            bean.run();
        } 
        catch(CmdLineException e) 
        {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }
    }    
}
