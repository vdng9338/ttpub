/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * The purpose of Configuration is to read a properties file, and expose the content theirin via the named enumerated params 
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Apr 18, 2007
 * @project ttpub
 * @version Revision: 1.0
 * @since   1.0
 */
public enum TimeTableProperties 
{
    DEFAULT_AGENCY, 
    DEFAULT_ROUTE, 
    DEFAULT_TABLE_TYPE,
    SCHEDULE_DIRECTORY,
    CONFIGURE_DIRECTORY, 
    MAX_TIMEPOINTS, 
    STOP_ID_STRING_NAME, 
    
    // Freemarker Templates: control the web page / print output from 'PRINT' app
    TEMPLATES_DIR,           // base freemarker template dir (eg: templates)
    HTML_TEMPLATES_DIR,      // dir where HTML templates are stored (eg: templates/trimet)
    HTML_BASICTABLE,         // template file name -- basic.web 
    HTML_ALLSTOPS, 
    HTML_MAPTABLE, 
    HTML_VERTTABLE, 
    HTML_REDIRECT,

    PRINT_TEMPLATE,
    PDF_TEMPLATE, 
    TEST_APP_TEMPLATE, 
    TEST_WEB_TEMPLATE
    ;

    private static ResourceBundle rb = ResourceBundle.getBundle(TimeTableProperties.class.getSimpleName());

    public String get()
    {
        synchronized(TimeTableProperties.class) 
        {
            return rb.getString(name());
        }
    }

    public String format(Object ... args) 
    {
        synchronized(TimeTableProperties.class) 
        {
            return MessageFormat.format(rb.getString(name()), args);
        }
    }

    public String get(String def)
    {
        String retVal = null;
        try
        {
            retVal = get();
        }
        catch(Exception _)
        {            
        }
        
        if(retVal == null || retVal.length() < 1)
            retVal = def;

        return retVal;
    }

    public int get(Integer def)
    {
        String tmp = get(def != null ? def.toString() : null);
        return IntUtils.getIntFromString(tmp);
    }
}
