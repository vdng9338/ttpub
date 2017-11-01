/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs;

import java.util.List;

import org.timetablepublisher.schedule.gtfs.loader.GTFSColumnImpl;


/**
 * The purpose of GoogleFeedBase is to act as a base class from which any data object in the Google Feed
 * format can inherit from.   In being the base class  
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 8, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 * @see     http://code.google.com/transit/spec/transit_feed_specification.htm
 */
@SuppressWarnings("unchecked")
public class GTFSData extends GTFSColumnImpl
{
    protected boolean m_processed = false;

    public GTFSData()
    {
    }
    
    public boolean isProcessed()
    {
        return m_processed;
    }
    public void setProcessed(boolean processed)
    {
        m_processed = processed;
    }
    public static void setProcessed(List<GTFSData> iList, boolean value)
    {
        for(GTFSData i : iList)
        {
            i.setProcessed(value);
        }       
    }
    public static void clearProcessed(List iList)
    {
        setProcessed(iList, false);
    }
}
