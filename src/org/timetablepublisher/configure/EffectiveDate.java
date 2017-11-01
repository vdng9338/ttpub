/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.Date;
import java.util.List;

import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.Params;

/**
 * The purpose of EffectiveDate is to allow the configuration of a default date for the 
 * Configuration (and/or specific routes within a schedule).  This basically sets a 
 * default date when querying and marking timetables with the 'Schedule Effective Date'.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class EffectiveDate extends Configure
{
    @CsvColumn(index=6, name="Start",  details="calendar date specifying the first effective date of this schedule (eg: 1-1-2007, in the form of month-day-year)")
    public String m_start;
    
    @CsvColumn(index=7, name="Finish", details="calendar date specifying the last effective date of this schedule (eg: 1-1-2007, in the form of month-day-year)")
    public String m_finish;

    public EffectiveDate()
    {        
    }

    public EffectiveDate(String start, String finish)
    {
        m_start  = start;
        m_finish = finish;
    }
    
    public EffectiveDate(TimesTable tt)
    {
        super(tt);
    }
    
    public String getFinish()
    {
        return m_finish;
    }

    public String getStart()
    {
        return m_start;
    }    
    
    // setters
    public void setFinish(String finish)
    {
        if(finish == null) return;
        m_finish = finish.trim();
    }

    public void setStart(String start)
    {
        if(start == null) return;
        m_start = start.trim();
    }

    /**
     * Will read the configuration, and return a date
     * 
     * @param m_tt
     */
    synchronized public static String getDefDate()
    {
        return Constants.dateSDF.format(new Date(System.currentTimeMillis() + (6L * 30L * 24L * 60L * 60L * 1000L)));  // now + 6 months
    }
    
    public static String getStartDate(TimesTable tt, String def)
    {
        List mnList = tt.getConfiguration().getData(EffectiveDate.class);
        return getStartDate(mnList, def);
    }

    
    public static String getStartDate(List<EffectiveDate> mnList, String def)
    {
        String retVal = def;       
        if(mnList != null) 
        {
            EffectiveDate ed = mnList.get(0);
            if(ed != null)
            {
                retVal = ed.getStart();
            }
        }

        return retVal;
    }
}

