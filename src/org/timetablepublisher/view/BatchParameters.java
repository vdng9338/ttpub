/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view;

import java.util.List;
import java.util.Locale;

import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.TimeTableFactory;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimeTableFactory.TableType;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.Params;
import org.timetablepublisher.utils.SelectedTableParams;
import org.timetablepublisher.utils.TimeTableProperties;
import org.timetablepublisher.view.pdf.PdfDesign;
import org.timetablepublisher.view.pdf.TrimetPdfDesignImpl;

public class BatchParameters implements Constants
{
    // set up some defaults (can be over written via the cmd line)
    private TableType m_tableType          = TableType.getDefault();
    private String    m_templatesDir       = TimeTableProperties.TEMPLATES_DIR.get("templates");        
    private String    m_defAgency          = TimeTableProperties.DEFAULT_AGENCY.get(TRIMET);
    private String    m_defRoute           = TimeTableProperties.DEFAULT_ROUTE.get("4");
    private String    m_htmlTemplatesDir   = TimeTableProperties.HTML_TEMPLATES_DIR.get("trimet");        
    private String    m_fmBasicFile        = TimeTableProperties.HTML_BASICTABLE.get("basic.web");
    private String    m_fmAllStopFile      = null;
    private String    m_fmVerticalFile     = null;
    private String    m_fmMapFile          = null;
    private PdfDesign m_pdfTemplate        = null;
    private String    m_fmRedirectPath     = null;
    private Boolean   m_preview            = false;
    private Boolean   m_once               = false;
    
    private Boolean   m_generateReport     = true;
    private Boolean   m_genTests           = false;    
    private Boolean   m_genAllStops        = null;
    private Boolean   m_genVertical        = null;
    private Boolean   m_genMap             = null;
    private Boolean   m_genRedirects       = null;

    private Locale    m_locale;
    private String    m_language           = "en";
    private String    m_configDataDir      = TimeTableProperties.CONFIGURE_DIRECTORY.get(".");
    private String    m_schedDataDir       = TimeTableProperties.SCHEDULE_DIRECTORY.get(".");
    private boolean   m_bypassConfig       = false;
    
    private List<SelectedTableParams> m_selectionList = null;

    public BatchParameters()
    {
    }

    public BatchParameters(Params params)
    {
        if(params != null)
        {
            m_tableType    = (TableType)get(params.getTableType(), m_tableType);
            m_bypassConfig = (Boolean)get(params.isBypassConfig(), m_bypassConfig);
            m_language     = (String)get(params.getLanguage(),     m_language);
            m_locale       = (Locale)get(params.getLocale(),       m_locale);
        }
    }
    
    private Object get(Object value, Object defVal)
    {
        if(value != null)
            return value;
        
        return defVal;
    }

    public TimesTable makeTable(List<Stop> allStops, String agency, String route, DirType dir, KeyType key, String date)
    {
        return TimeTableFactory.create(getTableType(), allStops, agency, route, dir, key, date, getLocale(), getConfigDataDir(), isBypassConfig(), getSchedDataDir());    
    }
    public TimesTable makeTable(String agency, String route, DirType dir, KeyType key, String date)
    {
        return makeTable((List<Stop>)null, agency, route, dir, key, date);    
    }
    public TimesTable makeTable()
    {
        return makeTable((List<Stop>)null, m_defAgency, m_defRoute, DirType.Outbound, KeyType.Weekday, "7-1-2007");    
    }
    
    public void init()
    {
        if(m_locale == null)
            m_locale = new Locale(m_language);
        
        if(m_genAllStops == null || m_fmAllStopFile == null)
            m_genAllStops = m_fmAllStopFile != null;
        
        if(m_genVertical == null || m_fmVerticalFile == null)
            m_genVertical = m_fmVerticalFile != null;
        
        if(m_genRedirects == null || m_fmRedirectPath == null)
            m_genRedirects = m_fmRedirectPath != null;
        
        if(m_pdfTemplate == null) 
        {
            m_pdfTemplate = new TrimetPdfDesignImpl();
        }
    }

    public void initFreeMarker()
    {
        m_fmBasicFile        = TimeTableProperties.HTML_BASICTABLE.get("basic.web");
        m_pdfTemplate        = PdfDesign.PdfDesignTypes.makeDesign("");

        if(m_genRedirects) m_fmRedirectPath = TimeTableProperties.HTML_REDIRECT.get("include/redirect.ftl");
        if(m_genVertical)  m_fmVerticalFile = TimeTableProperties.HTML_VERTTABLE.get("vert.web");        
        if(m_genMap)       m_fmMapFile      = TimeTableProperties.HTML_MAPTABLE.get("map.web");
        if(m_genAllStops)  m_fmAllStopFile  = TimeTableProperties.HTML_ALLSTOPS.get("allstops.web");
    }

    public void initAllButAllStops()
    {
        m_generateReport     = true;
        m_genAllStops        = false;
        m_genVertical        = true;
        m_genMap             = true;
        m_genRedirects       = true;
        initFreeMarker();
    }
    
    public String getDefaultAgency()
    {
        return m_defAgency;
    }
    
    public String getFmAllStopFile()
    {
        return m_fmAllStopFile;
    }

    public String getFmBasicFile()
    {
        return m_fmBasicFile;
    }

    public String getFmMapFile()
    {
        return m_fmMapFile;
    }

    public String getFmVerticalFile()
    {
        return m_fmVerticalFile;
    }

    public String getTemplatesDir()
    {
        return m_templatesDir;
    }

    public String getFmAllStopPath()
    {
        return m_htmlTemplatesDir + "/" + m_fmAllStopFile;
    }

    public String getFmBasicPath()
    {
        return m_htmlTemplatesDir + "/" + m_fmBasicFile;
    }

    public String getFmMapPath()
    {
        return m_htmlTemplatesDir + "/" + m_fmMapFile;
    }

    public String getFmVerticalPath()
    {
        return m_htmlTemplatesDir + "/" +  m_fmVerticalFile;
    }


    public String getFmRedirectPath()
    {
        return m_fmRedirectPath;
    }

    public Boolean getGenerateReport()
    {
        return m_generateReport;
    }

    public Boolean getPreview()
    {
        return m_preview;
    }

    public PdfDesign getPdfTemplate()
    {
        return m_pdfTemplate;
    }

    public Boolean getGenAllStops()
    {
        return m_genAllStops;
    }

    public Boolean getGenVertical()
    {
        return m_genVertical;
    }

    public TableType getTableType()
    {
        return m_tableType;
    }

    public String getHtmlTemplatesDir()
    {
        return m_htmlTemplatesDir;
    }

    public boolean isBypassConfig()
    {
        return m_bypassConfig;
    }

    public String getConfigDataDir()
    {
        return m_configDataDir;
    }

    public String getLanguage()
    {
        return m_language;
    }

    public Locale getLocale()
    {
        return m_locale;
    }

    public String getSchedDataDir()
    {
        return m_schedDataDir;
    }

    public Boolean getGenTests()
    {
        return m_genTests;
    }

    public Boolean getOnce()
    {
        return m_once;
    }

    public Boolean getGenRedirects()
    {
        return m_genRedirects;
    }

    public List<SelectedTableParams> getSelectionList()
    {
        if(m_selectionList == null || m_selectionList.size() < 1)
            setSelectionList(makeTable());
        
        return m_selectionList;
    }

    public void setSelectionList(List<SelectedTableParams> selectionList)
    {
        m_selectionList = selectionList;
    }

    public void setSelectionList(TimesTable tt)
    {
        m_selectionList = SelectedTableParams.getCombos(tt.getRouteNames());
    }

    public void setOnce(boolean b)
    {
        m_once = b;
    }

    public void setTemplatesDir(String templatesDir)
    {
        m_templatesDir = templatesDir;
    }

    public void setPreview(Boolean isPreview)
    {
        m_preview = isPreview;
    }
}
