/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs;

import java.util.List;

import org.timetablepublisher.schedule.gtfs.loader.GTFSColumn;
import org.timetablepublisher.utils.IntUtils;

/**
 * The purpose of StopTimes is to represent the StopTimes data object in the GData format.  This class, and the member
 * variables within, map directly to the StopTimes.txt CSV file specified in the Google Feed Spec. 
 * TODO
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 8, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class StopTimes extends GTFSData
{
    @GTFSColumn(name="trip_id",        description="")
    public String  m_trip;
    
    @GTFSColumn(name="arrival_time",   description="")
    public String  m_arrivalTime;
    
    @GTFSColumn(name="departure_time", description="")
    public String  m_departureTime;
    
    @GTFSColumn(name="stop_id",        description="")
    public String  m_stopID;
 
    
    // NOTE: any variable that is not a String MUST have a setter 
    
    @GTFSColumn(name="stop_sequence",  description="")
    public Integer m_stopSequence;

    @GTFSColumn(name="timepoint",  description="")
    public Boolean m_timepoint;
    
    @GTFSColumn(name="stop_headsign",  description="")
    public String  m_stopHeadsign;
        
    @GTFSColumn(name="pickup_type",    description="")
    public String  m_pickupType;
    
    @GTFSColumn(name="drop_off_type",  description="")
    public String  m_dropoffType;
    
    @GTFSColumn(name="shape_dist_traveled",  description="")
    public String  m_shapeDistTravelled;
    
    public static String getFileName()
    {
        return "stop_times.txt";
    }
    
    public void setStopSequence(String seq)
    {
        m_stopSequence = IntUtils.getIntFromString(seq);
    }    

    /** 
     * First check departure time and return that...if that's null, then check arrival time and return that.
     * @return a valid time
     */
    public String getValidTime()
    {
        String retVal = null;
        if(m_departureTime != null && m_departureTime.length() > 0)
        {
            retVal = m_departureTime;
        }
        else if(m_arrivalTime != null && m_arrivalTime.length() > 0) 
        {
            retVal = m_arrivalTime;
        }

        return retVal;
    }
    
    public String getShapeDistTravelled()
    {
        return m_shapeDistTravelled;
    }

    public String getStopHeadsign()
    {
        return m_stopHeadsign;
    }

    public String getArrivalTime()
    {
        if(m_arrivalTime == null) m_arrivalTime = m_departureTime;
        return m_arrivalTime;
    }

    public String getDepartureTime()
    {
        if(m_departureTime == null) m_departureTime = m_arrivalTime;
        return m_departureTime;
    }

    public String getDropoffType()
    {
        return m_dropoffType;
    }

    public String getPickupType()
    {
        return m_pickupType;
    }

    public String getStopID()
    {
        return m_stopID;
    }

    public Integer getStopSequence()
    {
        return m_stopSequence;
    }

    public String getTrip()
    {
        return m_trip;
    }

    public void setTimepoint(String tp)
    {
        m_timepoint = tp != null && (tp.equals("1") || tp.equals("true"));
    }
    public boolean isTimepoint()
    {
        if(m_timepoint == null) 
            return false;
        
        return m_timepoint;
    }    

    public static StopTimes find(List<StopTimes> times, String stopId)
    {
        if(stopId == null) return null;
        
        StopTimes retVal = null;        
        for(StopTimes s : times)
        {
            if(s == null || s.isProcessed()) continue;
            if(s.getValidTime() == null)     continue;
            if(stopId.equals(s.getStopID()))
            {
                s.setProcessed(true);
                retVal = s;
                break;
            }
        }
        
        return retVal;
    }
}
