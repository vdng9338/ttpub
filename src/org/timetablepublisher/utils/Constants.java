/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;



/**
 * Constants used by the TimeTablePublisher.   
 * 
 * Often, these constants are passed between Java and the Freemarker Template Engine
 * Want to keep parameters in sycn between Java(compiled) and Templates(dynamic).
 * Best way to do that is to expose static compiled parameters from java to
 * to the templates.  That's what this class does. The Parameters are defined
 * in the interface below, and the inner static class puts those params
 * into a java.util.Map for access from the Templates. 
 *            
 * CONSTANTS AND FREEMARKER: These constants are used both in the java code, and also in the Freemarker (FTL) 
 * templates.  Anytime you see the Parameters variable in an FTL template, that's referencing these parameters.  
 * Thus, when you see Parameters.ROUTE inside of a FTL template, you're seeing the string value from below.  
 * The purpose is to keep the templates and java code in sync, in terms of parameter names and HTTP parameters.
 * 
 * NOTE: Because FreeMarker can't read fields of a class directly, we place
 *       the parameters into a Hashtable.  With a bit of reflection magic, 
 *       that hash is easy to populate with the latest parameters from Parameters.java
 *           
 * @author  Frank Purcell
 * @version $Revision: 1.0 $
 * @since 1.0
 */
public interface Constants
{
    public static final Level  DEBUG  = Level.INFO; //Level.CONFIG;
    public static final Level  SEVERE = Level.SEVERE;
    
    // milli second times
    public static final Long   ONE_WEEK_MILLI        = 604800000L;    
    public static final Long   ONE_DAY_MILLI         = 86400000L;
    public static final Long   ONE_HOUR_MILLI        = 3600000L;
    public static final Long   ONE_MINUTE_MILLI      = 60000L;
    public static final Long   ONE_SECOND_MILLI      = 1000L;

    public static final Date   NOW                   = new Date();
    public static final Date   NEXT_WEEK             = new Date(NOW.getTime() + ONE_WEEK_MILLI);
    public static final Date   NEXT_MONTH            = new Date(NOW.getTime() + (ONE_WEEK_MILLI * 4) + (ONE_DAY_MILLI * 2));    
    
    public static final String DATE_FORMAT           = "MM-dd-yyyy";
    public static final String DATE_TIME_FORMAT      = "M.d.yy_k.m";
    public static final String DATE_TIME_FORMAT_NICE = "MM.dd.yyyy 'at' h:mm:a z";
    public static final String PRETTY_DATE_FORMAT    = "MMMM d, yyyy";
    public static final String PRETTY_DT_FORMAT      = PRETTY_DATE_FORMAT + " 'at' h:mm:a z";
    public static final SimpleDateFormat dateSDF     = new SimpleDateFormat(DATE_FORMAT);
    public static final SimpleDateFormat dateTimeSDF = new SimpleDateFormat(DATE_TIME_FORMAT);
    public static final SimpleDateFormat PRETTY_DATE = new SimpleDateFormat(PRETTY_DATE_FORMAT);
    public static final SimpleDateFormat PRETTY_DT   = new SimpleDateFormat(PRETTY_DT_FORMAT);
    public static final SimpleDateFormat YEAR        = new SimpleDateFormat("yyyy");
   
    
    // Objects set by Servlet and Passed to 
    public static final String TIMES_TABLE           = "timesTable";
    public static final String ROUTE_LIST            = "routeList";
    public static final String AMENITY_DESCRIPTIONS  = "amenityDescriptions";
    public static final String TODAY                 = "today";
    public static final String EFFECTIVE_DATE        = "effectiveDate";
    public static final String ERROR_MESSAGE         = "errorMessage";
    public static final String ERROR_MESSAGE_POPUP   = "errorMessagePopup";
    
    // FreemarkerBaseServlet: PARAMETES THAT ARE AVAILABLE TO *ALL* TEMPLATES
    public static final String PARAMETERS   = "Parameters";
    public static final String AGENCY       = "agency";  
    public static final String TRIMET       = "TriMet";
    public static final String ROUTE        = "route";
    public static final String AGENCY_ROUTE_SEP = "---";    
    public static final String DIR          = "dir";    
    public static final String KEY          = "key";        
    public static final String DATE         = "date";
    public static final String DIFF_DATE    = "diffDate";
    public static final String TIME_BUFFER  = "timeBuffer";
    public static final String OLD_ROUTE    = "oldRoute";
    public static final String OLD_DIR      = "oldDir";
    public static final String OLD_KEY      = "oldKey";    
    public static final String OLD_DATE     = "oldDate";    
    public static final String QUERY_STRING = "queryString";
    public static final String THIS_URL     = "thisURL";
    public static final String METHOD       = "method";
    public static final String LANGUAGE     = "ln";
    public static final String PREVIEW      = "preview";
    public static final String REPORT_NAME  = "ttReport.html";   

    // submit button name & values
    public static final String SUBMIT       = "submit";
    public static final String JUST_TESTS   = "just tests";    
    public static final String RELOAD       = "reload configuration";
    public static final String REVERT       = "revert timepoints";
    public static final String UPDATE       = "update page";
    public static final String BYPASS       = "bypass configurations";
    public static final String PDF          = "single pdf (proofing)";
    public static final String ZIP          = "make zip (XML for InDesign)";
    public static final String HTMLS_ZIP    = "html zip (Stand alone Webpages)";
    public static final String PERSIST      = "persist configuration";    
    public static final String USE_ROUTE    = "useRoute";
    public static final String USE_KEY      = "useKey";
    public static final String USE_DIR      = "useDir";

    public static final String CONFIG_DATA_DIR   = "configDataDir";
    public static final String SCHEDULE_DATA_DIR = "schedDataDir";
    
    // STRING (eg: 17-Holgate) constants for HTML GET parameters
    public static final String ROUTE_NAME   = "routeName";
    public static final String DIR_NAME     = "dirName";
    public static final String KEY_NAME     = "keyName";            
    public static final String STOP_NAME_   = "STOP_NAME_";   
    public static final String STOP_SEQ_    = "STOP_SEQ_";
    public static final String FN_SEQ_      = "FN_SEQ_";
    public static final String FN_NOTE_     = "FN_NOTE_";    
    public static final String FN_SYM_      = "FN_SYM_";     
    
    public static final String WEEK         = "week";
    public static final String SAT          = "sat";
    public static final String SUN          = "sun";
    public static final String INBOUND      = "inbound";
    public static final String OUTBOUND     = "outbound";
    public static final String DELETE       = "delete";
    public static final String AM           = "a.m.";
    public static final String PM           = "p.m.";

    // HTML ELEMENTS
    public static final String  SPACE        = "&nbsp;";    
    public static final String  TEXT_BOX     = "text";    
    public static final String  SHOW_DEBUG   = "debug";    
    public static final String  SHOW_ALL     = "showAll";  
    public static final String  ALL_STOPS    = "showAllStops";     
    public static final String  SHOW_VERT    = "showVertical";         
    public static final Integer PHANTOM_SEQ  = -111;
    
    // ConfigureServlet: HTTP GET PARAMETERS 
    public static final String ADD_TP_FORM    = "addTPForm";
    public static final String SESSION_       = "SESSION_";
    public static final String SESS_STOP_SEQ_ = SESSION_ + STOP_SEQ_;
    public static final String SESS_ADD_TP    = SESSION_ + ADD_TP_FORM;

    // Print & Compare Servlets: HTTP GET PARAMETERS
    public static final String RT_SELECT_FORM = "routeSelectForm";    
    public static final String ZIP_DIR        = "zipDir";
    public static final String ZIP_LIST       = "zipList";
    public static final String UTILS          = "utils";
    public static final String TT_DIFF_LIST   = "ttDiffList";

    
    // TestSuiteServlet
    public static final String TESTS_FOLDER    = "tests";
    public static final String SUITE_PREFIX    = "suite";
    public static final String APP_TEST_PREFIX = "appTest_";
    public static final String WEB_TEST_PREFIX = "webTest_";

    // TestSuiteServlet -- testRunner.suite  
    public static final String TEST_DIR        = "testDir";
    public static final String APP_TESTS       = "appTests";
    public static final String WEB_TESTS       = "webTests";
    public static final String APP_SUITE_NAME  = "appSuiteName";
    public static final String WEB_SUITE_NAME  = "webSuiteName";    
    public static final String TEST_SUITE_LIST = "testSuiteList";
    public static final String TEST_DIR_URL    = "testDirURL";
    public static final String TEST_SUITE_FTL  = "testTemplates/testSuite.web";

    public static final String TEST_SUITE      = "testSuite";
    public static final String TEST_TYPE       = "testType";

     
    // MAX ROUTES
    public static final Integer MAX_BLUE   = 100;
    public static final Integer MAX_YELLOW = 190;
    public static final Integer MAX_RED    =  90;
    public static final Integer MAX_GREEN  = 200;
    public static final Integer MAX_ORANGE = 300;
    public static final String  MAX_LIST   = MAX_BLUE + ", " + MAX_YELLOW + ", " + MAX_RED  + ", " + MAX_GREEN   + ", " + MAX_ORANGE ;
    
    // FILE NAMES
    public static final String HTM_FILE   = "htmFile";
    public static final String MAP_FILE   = "mapFile";
    public static final String PDF_FILE   = "pdfFile";
    public static final String TEST_FILE  = "testFile";
    public static final String WEB_TEST_FILE  = "webTestFile";
    
    // CSV EDITOR VARS
    public static final String EDIT_CSV_FILE    = "csvFileName";
    public static final String EDIT_ID          = "editID";
    public static final String CSV_EDIT_ROUTE   = "csvEditRoute";
    public static final String EDITS_MADE       = "csvEditsMade";    
    public static final String CSV_FIELD_VALUES = "csvFieldValues";
    public static final String CSV_DATA         = "csvData";
    public static final String CSV_COL_NAMES    = "csvColNames";
    public static final String CSV_LIST         = "csvList";

    // hibernate database config files
    public static final String TRANS_DATABASE_CFG = "hibernate-TRANS.cfg.xml";
    
    
    static public class ParameterMap implements Constants
    {
        final static private Map<String,Object> m_map = new HashMap<String,Object>();

        public ParameterMap()
        {
            if(m_map.isEmpty())
            {
                // bit of reflection magic to populate the hash
                try
                {
                    Field fields[] = getClass().getFields();
                    for(Field f : fields)
                    {
                        m_map.put(f.getName(), f.get(this));
                    }
                }
                catch(Exception e)
                {
                }            
            }
        }
        
        public Map getMap()
        {
            m_map.put("NOW",        new Date());
            m_map.put("NEXT_WEEK",  new Date(NOW.getTime() + ONE_WEEK_MILLI));
            m_map.put("NEXT_MONTH", new Date(NOW.getTime() + (ONE_WEEK_MILLI * 4) + (ONE_DAY_MILLI * 2)));
            
            return m_map;
        }
    }    
}
