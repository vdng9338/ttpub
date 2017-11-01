/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.cmdline;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.kohsuke.args4j.Option;
import org.timetablepublisher.configure.RenameTimePoint;
import org.timetablepublisher.configure.loader.ConfigurationCacheSingleton;
import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.TimeTableFactory;
import org.timetablepublisher.utils.TimeTableReport;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimeTableFactory.TableType;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.IntUtils;
import org.timetablepublisher.view.ZipTimeTables;
import org.timetablepublisher.view.pdf.PdfDesign;
import org.timetablepublisher.view.pdf.TrimetPdfDesignImpl;

import freemarker.template.Template;


/**
 * The purpose of TTCmdLineBase is to provide a base level of command line options for TT_PUB
 * terminal applications.  It uses args4j and Java 1.5 Annotations quite heavily.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 30, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class TTCmdLineBase implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(TTCmdLineBase.class.getCanonicalName());
    
    protected TimeTableReport m_report      = null;
    protected int m_estimatedNumOfFiles     = 0;
    protected int m_actualNumOfHtmlFiles    = 0;
    protected int m_actualNumOfPdfFiles     = 0;
    
    protected Template  m_allStopTemplate   = null;    
    protected Template  m_htmlTemplate      = null;
    protected Template  m_mapTemplate       = null;
    protected Template  m_verticalTemplate  = null;
    protected Template  m_redirectTemplate  = null;
    protected PdfDesign m_pdfTemplate       = null;
    
    protected Template  m_testSuiteTemplate = null;
    protected Template  m_webTestTemplate   = null;
    protected Template  m_appTestTemplate   = null;
    
    protected String    m_fmTestSuitePath   = "testTemplates/testSuite.web";
    
    // input parameters
    @Option(name="-tdir", usage="Templates Directory")
    public String m_fmTemplateDir  = "templates";

    @Option(name="-sdir", usage="Schedule Data Directory")
    public String m_schedDataDir   = ".";

    @Option(name="-cdir", usage="Configure Data Directory")
    public String m_configDataDir  = ".";

    @Option(name="-zip", usage="Zip Up a Bunch of TTs (if -zip is not used...you'll get one-off files)")
    public String m_zipFile = null;

    @Option(name="-zdir", usage="Zip Directory")    
    public String m_zipDirName = "zips/";

    @Option(name="-report", usage="Generates a detailed report (ttReport.html), looking for erros in the generated tables")    
    public boolean m_generateReport;
    
    // table creation parameters
    @Option(name="-type", usage="Table Type")
    public TableType m_tableType = TableType.TRANS;        

    @Option(name="-agency", usage="Agency (eg: 'Amtrak Cascades')")
    public String  m_agency = "Amtrak Cascades";        
    
    @Option(name="-route", usage="Route ID")
    public String  m_route = "104";
    
    @Option(name="-direction", usage="Route Direction")
    public DirType m_dir = DirType.Inbound;

    @Option(name="-key", usage="Service Key")
    public KeyType m_key = KeyType.Weekday;

    @Option(name="-date", usage="Effective Service Date")    
    public String m_svcDate = dateSDF.format(new Date());

    @Option(name="-nextSunday", usage="Effective Service Date is set to the next Calendar Sunday (may be set to input date, if the input date is already a Sunday)")
    public void nextSunday(String date)
    {
        m_svcDate = IntUtils.thisOrNextSunday(date);
    }
    
    @Option(name="-lang", usage="Language")
    public String m_language = Locale.ENGLISH.getLanguage();
    
    @Option(name="-bypass", usage="Bypass Config")    
    boolean m_bypassConfig = false;

    @Option(name="-noRenameTimepoint", usage="Bypass the Rename Timepoint Config (eg: use default Stop Names)")    
    boolean m_noRenameTimepoint = false;

    // processing params
    @Option(name="-once", usage="ONCE will stop the ZIP on the first route....good for testing")    
    public boolean m_once = false;
    
    @Option(name="-preview", usage="Create 'Preview' TimeTables -- this may influence the template (eg: for trimet.org, we remove some links to non-existent content for mode preview)")    
    boolean m_preview = false;


    // templates
    @Option(name="-htmlTemplate", usage="HTML Template")
    public String m_fmHtmlPath = "webPageTemplates/simpleTable.web";
        
    @Option(name="-allTemplate", usage="All Stops HTML Template")
    public String m_fmAllStopPath  = "webPageTemplates/simpleTable.web";
    
    @Option(name="-mapTemplate", usage="Map Template")
    public String m_fmMapPath  = "webPageTemplates/simpleGMap.web";

    @Option(name="-vertTemplate", usage="HTML Template")
    public String m_fmVerticalPath = "webPageTemplates/simpleTable.web";
    
    @Option(name="-webTestTemplate", usage="Test for the Generated WEB")
    public String m_fmAppTestPath  = "testTemplates/viewTest.web";
        
    @Option(name="-appTestTemplate", usage="Test for the TimeTable App")
    public String m_fmWebTestPath  = "testTemplates/trimetOrgTest.web";

    @Option(name="-redirectTemplate", usage="index.html file placed into each directory, to prevent html clients looking at the directory")
    public String m_fmRedirectPath  = "include/redirect.ftl";
    
    
    // output controls
    @Option(name="-maps", usage="Create a MAP version of the timetable.")    
    boolean m_genMaps = false;

    @Option(name="-vert", usage="Create a VERTICAL version of the timetable.")    
    boolean m_genVert = false;
    
    @Option(name="-tests", usage="Create Selenium Tests.")    
    boolean m_genTests = false;

    @Option(name="-redirects", usage="Add index.html file to each directory - redirect out of a directory to /.")    
    boolean m_genRedirects = false;
    
    @Option(name="-allStops", usage="produce a timetable with ALL STOPS")    
    public boolean m_allStops = false;
    
    @Option(name="-allStopsTableType", usage="Table Type for ALL STOPS")
    public TableType m_allStopsTableType = null;
    
    @Option(name="-allStopsDate", usage="Effective Date for ALL STOPS")
    public String m_allStopsDate = null;
    

    // output files -- non-zip / one off file generation (comes into play when zip file name is null)
    @Option(name="-htmlOut", usage="HTML Output file")
    public String m_outHtml = "table.html";
    
    @Option(name="-mapOut", usage="GMap Output file")
    public String m_outMap = "map.html";
    
    @Option(name="-pdfOut", usage="PDF Output file")
    public String m_outPdf = "table.pdf";

    public boolean makePdf(TimesTable tt, ZipTimeTables po, String fileName, PdfDesign pdfTemplate)
    {
        boolean isOK = true;
        try
        {
            // PDF generation
            // step 1: add the PDF version to the zip file
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            pdfTemplate.makeDocument(tt, b);
            
            // step 2: add the PDF to the zip
            LOGGER.log(DEBUG, "create: " + fileName);
            po.addFileToZip(fileName);
            b.writeTo(po.getZip());                    

            // step 3: tell ZIP to close this PDF file
            po.closeEntry();
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
            po.closeEntry();
        }
        
        return isOK;
    }
    
    public boolean makeHtml(TimesTable tt, ZipTimeTables po, String fileName, Template template)
    {
        boolean isOK = true;
        try
        {
            LOGGER.log(DEBUG, "create: " + fileName);           
            po.addFileToZip(fileName);
            po.templateProcess(template);
            m_actualNumOfHtmlFiles++;
        } 
        catch(Exception e)
        {
            isOK = false;
            logReport(tt, "Generating HTML document " + fileName + " ended pre-maturely", fileName);
            LOGGER.log(SEVERE, "Generating HTML document " + fileName + " ended pre-maturely", e);
        }
        finally
        {
            po.closeEntry();
        }
        
        return isOK;
    }
    
    
    synchronized public void setTemplates(ZipTimeTables po, PdfDesign pdf) throws Exception
    {
        m_htmlTemplate      = po.getTemplate(m_fmHtmlPath);
        m_mapTemplate       = po.getTemplate(m_fmMapPath);
        m_verticalTemplate  = po.getTemplate(m_fmVerticalPath);
        m_allStopTemplate   = po.getTemplate(m_fmAllStopPath);
        m_webTestTemplate   = po.getTemplate(m_fmWebTestPath);
        m_appTestTemplate   = po.getTemplate(m_fmAppTestPath);
        m_testSuiteTemplate = po.getTemplate(m_fmTestSuitePath);
        m_redirectTemplate  = po.getTemplate(m_fmRedirectPath);
        m_pdfTemplate       = pdf;
    }

    synchronized public void setTemplates(ZipTimeTables po) throws Exception
    {
        if(m_pdfTemplate == null) {
            m_pdfTemplate = new TrimetPdfDesignImpl();
        }
        setTemplates(po, m_pdfTemplate);         
    }
    
    public TimesTable makeTable()
    {
        TimesTable tt = TimeTableFactory.create(m_tableType, null, m_agency, m_route, m_dir, m_key, m_svcDate, new Locale(m_language), m_configDataDir, m_bypassConfig, m_schedDataDir);
        if(tt == null || tt.getTimePoints() == null || tt.getTimePoints().size() <= 1 ||
           tt.getTimeTable()  == null || tt.getTimeTable().size()  <  1)
        {
            LOGGER.log(DEBUG, tt.getRouteDescription() + " didn't generate a schedule (probably not be a big deal: most likely this route doesn't run on this day). Continuing....");
            tt = null;
        }
        return tt;
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

    public TimesTable makeAllStopsTable(List<Stop>allStops, String routeID, DirType dir, KeyType key)
    {
        if(m_allStopsTableType == null) m_allStopsTableType = m_tableType;
        if(m_allStopsDate      == null) m_allStopsDate      = m_svcDate;
        
        // TimeTableFactory.create(m_allStopsTableType, m_tt.getRouteStops(), m_agency, rd.getRouteID(), dirs[d], key, date, new Locale(m_language), null, m_bypassConfig, m_dataDirectory);
        //   CONSTRUCTOR:       create(TableType, timePoints, agency, route, DirType, KeyType  date,   Locale lang,           configDir,       bypassConfig,  scheduleDataDir)
        return TimeTableFactory.create(m_allStopsTableType, allStops, m_agency, routeID, dir, key, m_allStopsDate, new Locale(m_language), m_configDataDir, m_bypassConfig, m_schedDataDir);        
    }
    
    protected void init()
    {
        // turn OFF Rename TimePoint
        if(m_noRenameTimepoint)
        {
            ConfigurationLoader l;
            if(m_configDataDir != null)
            {
                l = ConfigurationCacheSingleton.getLoader(m_configDataDir);
                l.setIgnoreConfig(RenameTimePoint.class);
            }
            
            l = ConfigurationCacheSingleton.getDefaultLoader();
            l.setIgnoreConfig(RenameTimePoint.class);
        }
    }
    
    public boolean doReport()
    {
        boolean retVal = false;
        if(m_generateReport)
        {
            retVal = true;
            
            if(m_report == null)
                m_report = new TimeTableReport();
        }
        
        LOGGER.log(DEBUG, "reporting is " + (retVal ? "on" : "OFF"));
        
        return retVal;
    }
    public boolean doReport(TimesTable tt)
    {        
        boolean retVal = doReport();
        if(m_report != null && tt != null && (tt.getTableType() == TableType.TRANS))
        {
            m_report.setNumericStopIDs(true);
        }
        
        return retVal;
    }
    

    protected void logReport(TimesTable tt, String problem, String url)
    {        
        if(!doReport(tt)) 
            return;

        m_report.warning(tt, url, "file problem", problem);
        LOGGER.log(DEBUG, "file problem: " + problem);
    }

    protected void logReport(TimesTable tt, String route, DirType dir, KeyType key, String date, String url)
    {
        if(!doReport(tt)) return;
        
        if(tt == null)
        {
            m_report.newEntry(route, route, dir, key, date, url);
            m_report.warning(tt, url, "NULL TT", "couldn't generate a timetable for this route / dir / key / date");
            LOGGER.log(DEBUG, "couldn't generate a timetable for this route / dir / key / date");
        }
        else
        {   
            m_report.newEntry(tt, url);
            m_report.inspectTable(tt);            
        }
    }
}
