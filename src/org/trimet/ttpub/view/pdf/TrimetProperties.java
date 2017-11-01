/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.trimet.ttpub.view.pdf;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * The purpose of Configuration is to read a properties file, and expose the content theirin via the named enumerated params 
 * 
 * @author  Frank Purcell (purcellf@trimet.org) -- taken from code by Kohsuke Kawaguchi
 * @date    Apr 18, 2007
 * @project tpws
 * @version Revision: 1.0
 * @since   1.0
 */
public enum TrimetProperties 
{
    COPYRIGHT, LATE_NOTE, PM_TIMES, LOGO_FILE, LOGO_PATH;

    private static ResourceBundle rb = ResourceBundle.getBundle(TrimetProperties.class.getName());

    public String get()
    {
        synchronized(TrimetProperties.class) 
        {
            return rb.getString(name());
        }
    }

    public String format(Object ... args) 
    {
        synchronized(TrimetProperties.class) 
        {
            return MessageFormat.format(rb.getString(name()), args);
        }
    }
}
