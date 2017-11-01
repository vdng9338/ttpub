/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs;

import java.math.BigDecimal;
import java.util.List;

import org.timetablepublisher.schedule.gtfs.loader.GTFSColumn;
import org.timetablepublisher.schedule.gtfs.loader.GTFSDataLoader;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.StopImpl;

/**
 * The purpose of Stops is to represent the Stops data object.  This class, and the member variables within
 * map directly to the stops.txt CSV file specified in the Google Feed Spec. 
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 8, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class Stops extends GTFSData
{
 
    @GTFSColumn(name="stop_id",  description="")
    public String m_id; 
    
    @GTFSColumn(name="stop_name",  description="")
    public String m_name;
    
    @GTFSColumn(name="stop_desc",  description="")
    public String m_description;
    
    @GTFSColumn(name="stop_lon",  description="")
    public String m_longitude;
    
    @GTFSColumn(name="stop_lat",  description="")
    public String m_latitude;
    
    @GTFSColumn(name="zone_id",  description="")
    public String m_fareZoneID;
    
    @GTFSColumn(name="stop_url",  description="")
    public String m_url;
    
    private BigDecimal m_lat = null;
    private BigDecimal m_lon = null;
    
    public static String getFileName()
    {
        return "stops.txt";
    }
    
    public String getUrl()
    {
        return m_url;
    }

    public String getDescription()
    {
        if(m_description == null)
            m_description = m_name;
        
        return m_description;
    }

    public String getId()
    {
        return m_id;
    }

    public String getLatitude()
    {
        return m_latitude;
    }

    public String getLongitude()
    {
        return m_longitude;
    }

    public BigDecimal getLon()
    {
        if(m_lon == null)
        {
            try
            {
                m_lon = new BigDecimal(m_longitude);    
            }
            catch(Exception e)
            {
                m_lon = new BigDecimal(-122.0);
            }            
        }
        
        return m_lon;
    }

    public BigDecimal getLat()
    {
        if(m_lat == null)
        {
            try
            {
                m_lat = new BigDecimal(m_latitude);    
            }
            catch(Exception e)
            {
                m_lat = new BigDecimal(46.0);
            }            
        }
        
        return m_lat;
    }

    public String getName()
    {
        return m_name;
    }

    public String getFareZoneID()
    {
        return m_fareZoneID;
    }

    
    public static Stops getStop(Agency a, String id, GTFSDataLoader loader)
    {
        if(loader == null || id == null) return null;
        
        Stops retVal = null;
        List sIn = loader.getData(a, Stops.class);
        if(sIn != null)
        {
            for(Stops st : (List<Stops>) sIn)
            {
                if(id.equals(st.getId()))
                {
                    retVal = st;
                    break;
                }
            }
        }
        
        return retVal;
    }
    
    public static Stops getStop(String agencyName, String id, GTFSDataLoader loader)
    {
        if(loader == null || id == null) return null;
        Agency agency = loader.getAgency(agencyName);
        return getStop(agency, id, loader);
    }

    public static Stop makeStop(Stops s, Integer sequence)
    {
        if(s == null) return null;
        
        StopImpl retVal = new StopImpl(s.getId(), s.getId(), sequence, s.getName(), s.getLat(), s.getLon());
        if(retVal != null)
        {
            retVal.setUrl(s.getUrl());
        }
        
        return retVal;
    }
    
    public static Stop makeColumn(GTFSDataLoader loader, Agency a, String stopID, Integer sequence)
    {
        int seq = 111;
        if(sequence != null) 
            seq = sequence; 

        Stop retVal = makeStop(getStop(a, stopID, loader), seq);
        if(retVal == null)
        {
            retVal = new StopImpl(stopID, stopID, seq, stopID);
        }
                
        return retVal;
    }
}
