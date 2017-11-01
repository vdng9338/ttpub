 /**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimeTableFactory.TableType;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.FileUtils;
import org.timetablepublisher.utils.KeyValue;
import org.timetablepublisher.utils.SelectedTableParams;
import org.timetablepublisher.utils.TimeTableProperties;
import org.timetablepublisher.utils.TimeTableReport;
import org.timetablepublisher.view.ZipTimeTables;

import freemarker.template.Template;


/**
 * The purpose of BatchGenerator is to batch process a schedule, producing all valid time tables,
 * and returning a ZIP file full of html & pdf time tables.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 26, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class BatchGenerator implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(BatchGenerator.class.getCanonicalName());

    public    BatchParameters       m_params;
    protected TimeTableReport       m_report = null;
    private   ZipTimeTables         m_zip;    
    private   Map<String, Template> m_fmTemplates = new Hashtable<String, Template>();  
    
    private   int             m_estimatedNumOfFiles;
    private   int             m_actualNumOfHtmlFiles;
    private   int             m_actualNumOfPdfFiles;
    
    private   String          m_agency;
    private   String          m_route;
    private   DirType         m_dir;
    private   KeyType         m_key;    
    private   String          m_date;

    
    public BatchGenerator(ZipTimeTables z, BatchParameters params)
    {
        m_zip = z;
        m_params = params;
    }
    public BatchGenerator(OutputStream os, BatchParameters params)
    {
        initZip(os);
        m_params = params;
    }
    public BatchGenerator()
    {
        initZip(new PrintStream(System.out));
        m_params = new BatchParameters();
        m_params.initFreeMarker();        
    }
    
    public void initFreemarkerParams()
    {
        m_params.init();
        
        m_zip.addParams(SHOW_VERT,  m_params.getGenVertical());
        m_zip.addParams(ALL_STOPS,  m_params.getGenAllStops());
        m_zip.addParams(PREVIEW,    m_params.getPreview());
    }

    public boolean makeHtml(String fileName, Template template)
    {
        boolean isOK = true;
        try
        {
            LOGGER.log(DEBUG, "create: " + fileName);           
            m_zip.addFileToZip(fileName);
            m_zip.templateProcess(template);
            m_actualNumOfHtmlFiles++;
        } 
        catch(Exception e)
        {
            isOK = false;
            LOGGER.log(SEVERE, "Generating HTML document " + fileName + " ended pre-maturely", e);
        }
        finally
        {
            m_zip.closeEntry();
        }
        
        return isOK;        
    }
    
    public boolean makeHtml(TimesTable tt, String fileName, Template template)
    {
        if(tt == null || template == null) 
        {
            LOGGER.log(DEBUG, "Template / TimeTable for file " + fileName + " is null.");
            return false;
        }
        m_zip.addParams(TIMES_TABLE, tt);
        boolean retVal = makeHtml(fileName, template);
        if(!retVal)
            logReport(tt, "Generating HTML document " + fileName + " ended pre-maturely", fileName);
        
        return retVal;
    }
    
    public boolean makePdf(TimesTable tt, String fileName)
    {
        boolean isOK = true;
        try
        {
            // PDF generation
            // step 1: add the PDF version to the zip file
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            m_params.getPdfTemplate().makeDocument(tt, b);
            
            // step 2: add the PDF to the zip
            LOGGER.log(DEBUG, "create: " + fileName);
            m_zip.addFileToZip(fileName);
            b.writeTo(m_zip.getZip());                    

            // step 3: tell ZIP to close this PDF file
            m_zip.closeEntry();
            m_actualNumOfPdfFiles++;  
        }
        catch(Exception e)
        {
            isOK = false;
            logReport(tt, "Generating PDF document " + fileName + " ended pre-maturely", fileName);
            LOGGER.log(SEVERE, "Generating PDF document " + fileName + " ended pre-maturely", e);
        }
        finally
        {
            m_zip.closeEntry();
        }
        
        return isOK;
    }
    
    public boolean makeHtml(TimesTable tt, String fileName, TimeTableProperties prop)
    {
        String tempName = null;
        switch(prop)
        {
            case TEST_APP_TEMPLATE:
            case TEST_WEB_TEMPLATE:
                return makeHtml(fileName, getTemplate(m_params.getFmRedirectPath()));
            case HTML_REDIRECT:
                return makeHtml(fileName, getTemplate(m_params.getFmRedirectPath()));                
            case HTML_VERTTABLE:
                tempName = m_params.getFmVerticalPath();
                break;
            case HTML_ALLSTOPS:
                tempName = m_params.getFmAllStopPath();
                break;
            case HTML_MAPTABLE:
                tempName = m_params.getFmMapPath();
                break;
            case HTML_BASICTABLE:
            default:
                tempName = m_params.getFmBasicPath();
        }
            
        return makeHtml(tt, fileName, getTemplate(tempName));
    }

    public void end()
    {
        if(m_params.getGenerateReport() && m_report != null)
        {
            m_report.note("File Report:", "Numbers");
            m_report.note("Estimated number of Files:", m_estimatedNumOfFiles  + "");
            m_report.note("HMTLs Generated:",           m_actualNumOfHtmlFiles + "");
            m_report.note("PDFs Generated:",            m_actualNumOfPdfFiles  + "");
            m_report.close();
            try
            {
                m_zip.addFileToZip(REPORT_NAME, m_report.getReport());
            }
            catch (Exception e)
            {
                LOGGER.log(DEBUG, "TODO: ", e);
            }
        }
        
        LOGGER.info("\n\n\n>>>>>>> File Report:\n\tEstimated = " + m_estimatedNumOfFiles  + 
                    " HTML and PDFs.\n\tActual number of HTML: " + m_actualNumOfHtmlFiles +
                    "\n\tActual number of PDFs: "                + m_actualNumOfPdfFiles);          
    }
        
    synchronized public Template getTemplate(String templateName)
    {
        Template retVal = null;
        try
        {
            retVal = m_fmTemplates.get(templateName);
            if(retVal == null)
            {
                Template t = m_zip.getTemplate(templateName);
                if(t != null)
                {
                    m_fmTemplates.put(templateName, t);
                    retVal = t;
                }
            }
        }
        catch(Exception _)
        {                
        }
        
        return retVal;
    }

    
    public void addFreemarkerParam(String name, Object val)
    {
        m_zip.addParams(name, val);
    }
    
    public ZipTimeTables getZip()
    {
        return m_zip;
    }

    public void setZip(ZipTimeTables zip)
    {
        m_zip = zip;
    }
    
    public boolean doReport()
    {
        boolean retVal = false;
        if(m_params.getGenerateReport())
        {
            retVal = true;
            
            if(m_report == null)
                m_report = new TimeTableReport();
        }
        return retVal;
    }
    public boolean doReport(TimesTable tt)
    {
        boolean retVal = doReport();
        if(tt != null && (tt.getTableType() == TableType.TRANS))
        {
            m_report.setNumericStopIDs(true);
        }
        
        return retVal;
    }
    

    protected void logReport(TimesTable tt, String problem, String url)
    {        
        if(!doReport(tt)) return;

        m_report.warning(tt, url, "file problem", problem);
    }

    protected void logReport(TimesTable tt, String route, DirType dir, KeyType key, String date, String url)
    {
        if(!doReport(tt)) return;
        
        if(tt == null)
        {
            m_report.newEntry(route, route, dir, key, date, url);
            m_report.warning(tt, url, "NULL TT", "couldn't generate a timetable for this route / dir / key / date");
        }
        else
        {            
            m_report.newEntry(tt, url);
            m_report.inspectTable(tt);
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
    public void process()
        throws Exception
    {
        setTemplates(getZip());
        initFreemarkerParams();

        m_estimatedNumOfFiles = m_params.getSelectionList().size();

        List<KeyValue> tests = new ArrayList<KeyValue>();
        
        // step 1: loop through all of the routes in this schedule
        LOGGER.log(DEBUG, "starting to loop through routes");
        for(SelectedTableParams sel : m_params.getSelectionList())
        {   
            System.out.print(".");
            
            m_agency = sel.getAgency();
            m_route  = sel.getRoute();
            m_key    = sel.getKey();
            m_dir    = sel.getDir();
            
            // directories are names one of w/s/h/ (h == holiday and/or sunday)                    
            String  directory = (m_key == KeyType.Sunday) ? "h" : m_key.value().toLowerCase();  

            // step 4: get the time-table from our data store
            TimesTable tt = makeTable();
            
            // step 5: build a file name based on TriMet file / path naming convention, and log the result
            String fileName = getFileName(tt); 
            String pathName = directory + "/" + fileName;
            logReport(tt, m_route, m_dir, m_key, m_date, pathName + ".htm");
            if(tt == null) continue;
            LOGGER.log(DEBUG, tt.getRouteDescription());            
            
            // TODO -- use TimeTable.properties FORMAT to format the file name
            // step 6: produce the HTML, MAP, PDF and (if desired) ALL STOPS  
            makeHtml(tt, pathName + ".htm",    TimeTableProperties.HTML_BASICTABLE);
            makeHtml(tt, pathName + "map.htm", TimeTableProperties.HTML_MAPTABLE);
            makeHtml(tt, pathName + ".html",   TimeTableProperties.HTML_VERTTABLE);
            makePdf(tt,  pathName  + ".pdf");
            
            // step 6a: create the ALL STOPS schedule 
            if(m_params.getGenAllStops())
            {
                LOGGER.log(DEBUG, "ALL STOPS: " + tt.getRouteDescription());            
                tt = makeTable(tt.getRouteStops());
                makeHtml(tt, pathName + "all.htm", TimeTableProperties.HTML_ALLSTOPS);
            }
            
            // step 6b: create tests
            if(m_params.getGenTests()) 
            {
                String appTest = "TEST/app/" + pathName + ".html";
                String webTest = "TEST/web/" + pathName + ".html";
                makeHtml(tt, appTest, TimeTableProperties.TEST_APP_TEMPLATE);
                makeHtml(tt, webTest, TimeTableProperties.TEST_WEB_TEMPLATE);
                try 
                {
                  tests.add(new KeyValue(tt.getRouteDescription(), pathName + ".html"));
                } 
                catch(Exception e) 
                {                            
                }
            }                                        

            if(m_params.getOnce()) break;
        } // routes

 
        // step 7a: create small index.html files which have a redirection back to the route landing page
        if(m_params.getGenRedirects()) 
        {
            // step 7b: loop through all of the keys for each route
            for(KeyType k : KeyType.getWkSaSu())
            {            
                // directories are names one of w/s/h/ (h == holiday and/or sunday)                    
                String  directory = (k == KeyType.Sunday) ? "h" : k.value().toLowerCase();  
                makeHtml(null, directory + "/index.html", TimeTableProperties.HTML_REDIRECT);
            }
        }

        
        // step 8: generate Selenium IDE acceptence tests
        if(m_params.getGenTests())
        {
            String appSuite = "TEST/app/TestSuite.html";
            m_zip.addParams(TEST_TYPE,  "app");
            m_zip.addParams(TEST_SUITE, tests);
            makeHtml(null, appSuite, TimeTableProperties.TEST_APP_TEMPLATE);
            
            String webSuite = "TEST/web/TestSuite.html";
            m_zip.addParams(TEST_TYPE,  "web");        
            m_zip.addParams(TEST_SUITE, tests);
            makeHtml(null, webSuite, TimeTableProperties.TEST_APP_TEMPLATE);
        }
    }
    
    synchronized public void setTemplates(ZipTimeTables po) throws Exception
    {
  
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


    public TimesTable makeTable(List<Stop> allStops)
    {
        TimesTable tt = m_params.makeTable(allStops, m_agency, m_route, m_dir, m_key, m_date); 
        if(tt == null || tt.getTimePoints() == null || tt.getTimePoints().size() <= 1 || tt.getTimeTable()  == null || tt.getTimeTable().size()  <  1)
        {
            LOGGER.log(DEBUG, tt.getRouteDescription() + " didn't generate a schedule (probably not be a big deal: most likely this route doesn't run on this day). Continuing....");
            tt = null;
        }
        return tt;
    }    
    public TimesTable makeTable()
    {
        return makeTable((List<Stop>)null);
    }

    
    public TimesTable makeTable(String defRte)
    {
        TimesTable retVal = makeTable();
        if(retVal == null)
        {
            String tmp = m_route;
            m_route = defRte;
            retVal = makeTable();
            m_route = tmp;            
        }
        
        return retVal;
    }
    
    
    private void initZip(OutputStream out)
    {
        try
        {
            m_zip = new ZipTimeTables(m_params.getTemplatesDir(), m_params.getFmBasicPath(), out);
        }
        catch(Exception _)
        {
        }
    }
    
    synchronized static public ZipTimeTables zip(String dirName, String fileName, String svcDate, BatchParameters params)
    {
        ZipTimeTables retVal = null;
        try 
        {                       
            FileUtils.mkdir(dirName);
            FileUtils.rename(dirName, fileName);

            LOGGER.log(DEBUG, "create zip file: " + dirName + fileName); 
            File f = new File(dirName + fileName);
            retVal = zip(f, svcDate, params); 
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, "TimeTable Batch Zip -- couldn't create file " + e.toString(), e);
        }
        
        return retVal;
    }
    synchronized static public ZipTimeTables zip(File file, String svcDate, BatchParameters params)
    {
        String today = dateSDF.format(new Date());
        ZipTimeTables retVal = null;

        try 
        {                       
            OutputStream fs = new FileOutputStream(file);
            retVal = new ZipTimeTables(params.getTemplatesDir(), params.getFmBasicPath(), fs);

            // zip dir
            LOGGER.log(DEBUG, "set zip comment");
            String comment = "Created on: "        + today + "\n\n";
                   comment += "PARAMETERS: \n";
                   comment += "  - Table Type: "   + params.getTableType() + "\r\n";
                   comment += "  - Service Date: " + svcDate               + "\n";
                   comment += "  - Routes: "       + "all routes"          + "\n";
                   comment += "  - Directions: "   + "Both Inbound / Outbound" + "\n";
                   comment += "  - Service Keys: " + "W, S, U"        + "\n";
            retVal.getZip().setComment(comment);
            
            BatchGenerator bg = new BatchGenerator(retVal, params);
            bg.setDate(svcDate);
            bg.process();
            bg.end();
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, "VERY VERY BAD: TimeTable Batch Process Ended PreMaturely: " + e.toString(), e);
        }
        finally
        {
            // Complete the ZIP file
            LOGGER.log(DEBUG, "closing the zip file");
            if(retVal != null) retVal.close();
        }
        
        return retVal;
    }
    
    public static void main(String[] argv)
    {
        BatchParameters params = new BatchParameters();
        params.initFreeMarker();
        params.setOnce(true);
        zip("./zips/", "test.zip", "7-1-2007", params); 
    }
    public String getDate()
    {
        return m_date;
    }
    public void setDate(String date)
    {
        m_date = date;
    }
}
