/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs;

import java.util.Date;

import org.timetablepublisher.schedule.gtfs.loader.GTFSColumn;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.IntUtils;

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
public class Calendar extends GTFSData
{
    enum DayType {M, T, W, TH, F, S, SU};
    
    @GTFSColumn(name="service_id", description="")
    public String  m_rawKey;
    
    @GTFSColumn(name="start_date", description="")    
    public Date    m_startDate;
    
    @GTFSColumn(name="end_date",  description="")
    public Date    m_endDate;
    
    @GTFSColumn(name="monday",    description="", useSetter=true)
    public Boolean  m_monday;
    @GTFSColumn(name="tuesday",   description="", useSetter=true)
    public Boolean  m_tuesday;
    @GTFSColumn(name="wednesday", description="", useSetter=true)
    public Boolean  m_wednesday;
    @GTFSColumn(name="thursday",  description="", useSetter=true)
    public Boolean  m_thursday;
    @GTFSColumn(name="friday",    description="", useSetter=true)
    public Boolean  m_friday;
    @GTFSColumn(name="saturday",  description="", useSetter=true)
    public Boolean  m_saturday;
    @GTFSColumn(name="sunday",    description="", useSetter=true)
    public Boolean  m_sunday;
    
    boolean[] m_days = new boolean[DayType.SU.ordinal() + 1];
    private KeyType m_key = null;
    
    public static String getFileName()
    {
        return "calendar.txt";
    }

    // SETTERS    
    public void setStartDate(String startDate)
    {
        m_startDate = IntUtils.toDate(startDate);
    }
    public void setEndDate(String endDate)
    {
        m_endDate = IntUtils.toDate(endDate);
    }

    public void setMonday(String monday)
    {
        m_monday = IntUtils.toBoolean(monday);
        m_days[DayType.M.ordinal()] = m_monday;
    }
    public void setTuesday(String tuesday)
    {
        m_tuesday = IntUtils.toBoolean(tuesday);
        m_days[DayType.T.ordinal()] = m_tuesday;
    }
    public void setWednesday(String wednesday)
    {
        m_wednesday = IntUtils.toBoolean(wednesday);
        m_days[DayType.W.ordinal()] = m_wednesday;
    }
    public void setThursday(String thursday)
    {
        m_thursday = IntUtils.toBoolean(thursday);
        m_days[DayType.TH.ordinal()] = m_thursday;
    }
    public void setFriday(String friday)
    {
        m_friday = IntUtils.toBoolean(friday);
        m_days[DayType.F.ordinal()] = m_friday;
    }
    public void setSaturday(String saturday)
    {
        m_saturday = IntUtils.toBoolean(saturday);
        m_days[DayType.S.ordinal()] = m_saturday;
    }
    public void setSunday(String sunday)
    {
        m_sunday = IntUtils.toBoolean(sunday);
        m_days[DayType.SU.ordinal()] = m_sunday;
    }
    
    public String getRawKey()
    {
        return m_rawKey;
    }
    
    public KeyType getServiceKey()
    {
        if(m_key == null)
            process();
                
        return m_key;
    }
    
    public boolean[] getDays()
    {
        return m_days;
    }

    public Date getEndDate()
    {
        return m_endDate;
    }

    public Date getStartDate()
    {
        return m_startDate;
    }

    /////// SERVICE KEY PROCESSING BELOW.... /////
    public void process()
    {
        if(isEveryDay())
        {
            m_key = KeyType.AllDays;
        }
        else if(isWeekday())
        {
            m_key = KeyType.Weekday;
        }
        else if(isWeekend())
        {
            m_key = KeyType.Weekend;
        }
        else if(isMonThur())
        {
            m_key = KeyType.Monday_Thursday;
        }
        else if(isSingleDay())
        {
            m_key = toKey();
        }
        else
        {
            m_key = KeyType.Weekday;
        }
    }
    
    public boolean activeDaysInSeq(DayType start, DayType end, boolean target)
    {
        if(start == null || end == null || start == end) return true;
        if(start.ordinal() > end.ordinal())
        {
            DayType tmp = start;
            start = end;
            end   = tmp; 
        }

        boolean retVal = true;
        for(int i = start.ordinal(); i <= end.ordinal(); i++)
        {
            if(m_days[i] != target)
            {
                retVal = false;
                break;
            }
        }       
        
        return retVal;
    }

    
    public boolean isEveryDay()
    {
        return activeDaysInSeq(DayType.M, DayType.SU, true);
    }
    
    public boolean isWeekday()
    {
        boolean retVal = activeDaysInSeq(DayType.M, DayType.F, true);
        if(retVal)
        {
            retVal = activeDaysInSeq(DayType.S, DayType.SU, false);
        }
        return retVal;
    }

    public boolean isWeekend()
    {
        boolean retVal = activeDaysInSeq(DayType.S, DayType.SU, true);
        if(retVal)
        {
            retVal = activeDaysInSeq(DayType.M, DayType.F, false);
        }
        return retVal;
    }

    public boolean isMonThur()
    {
        boolean retVal = activeDaysInSeq(DayType.M, DayType.TH, true);
        if(retVal)
        {            
            retVal = activeDaysInSeq(DayType.F, DayType.SU, false);
        }
        return retVal;
    }

    public boolean isSingleDay()
    {
        int cnt = 0;
        for(DayType d : DayType.values())
        {
            if(m_days[d.ordinal()])
            {
                cnt++;
            }
        }       
        
        return cnt == 1;
    }

    
    public DayType getDay()
    {
        DayType retVal = null;
        for(DayType d : DayType.values())
        {
            if(m_days[d.ordinal()])
            {
                retVal = d;
                break;
            }
        }       
        
        return retVal;
    }
    
    public static KeyType toKey(DayType d)
    {
        switch(d)
        {
        case M:  return KeyType.Monday;    
        case T:  return KeyType.Tuesday;   
        case W:  return KeyType.Wednesday; 
        case TH: return KeyType.Thursday;  
        case F:  return KeyType.Friday;    
        case S:  return KeyType.Saturday;  
        case SU: return KeyType.Sunday;    
        default: return KeyType.Weekday;   
        }
    }
    public KeyType toKey()
    {
        return toKey(getDay());
    }
}
