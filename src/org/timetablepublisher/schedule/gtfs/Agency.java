/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs;

import org.timetablepublisher.schedule.gtfs.loader.GTFSColumn;

/**
 * The purpose of Agency is to represent the Agency data object in the GData format.  This class, and the member
 * variables within, map directly to the Agency.txt CSV file specified in the Google Feed Spec. 
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 8, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class Agency extends GTFSData
{
    @GTFSColumn(name="agency_id", description="")
    public String m_id;
    
    @GTFSColumn(name="agency_name", description="")
    public String m_name;
    
    @GTFSColumn(name="agency_url", description="")
    public String m_url;
    
    @GTFSColumn(name="agency_timezone", description="")
    public String m_timezone;
    
    @GTFSColumn(name="agency_lang", description="")
    public String m_lang;
    
    public static String getFileName()
    {
        return "agency.txt";
    }
    
    public String getId()
    {
        return m_id;
    }

    public String getLang()
    {
        return m_lang;
    }

    public String getName()
    {
        return m_name;
    }

    public String getTimezone()
    {
        return m_timezone;
    }

    public String getUrl()
    {
        return m_url;
    }
}
