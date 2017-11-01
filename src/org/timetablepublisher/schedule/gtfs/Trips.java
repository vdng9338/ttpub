/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs;

import java.util.Hashtable;
import java.util.List;

import org.timetablepublisher.schedule.gtfs.loader.GTFSColumn;
import org.timetablepublisher.schedule.gtfs.loader.GTFSDataLoader;
import org.timetablepublisher.table.TimesTable.DirType;

/**
 * The purpose of Trips is to represent the Trips data object in the GData format.  This class, and the member
 * variables within, map directly to the Trips.txt CSV file specified in the Google Feed Spec. 
 * TODO
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 8, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class Trips extends GTFSData
{
    @GTFSColumn(name="route_id", description="")
    public String  m_route;
    
    @GTFSColumn(name="service_id", description="")
    public String  m_key;
    
    @GTFSColumn(name="trip_id", description="")
    public String  m_tripID;
    
    @GTFSColumn(name="trip_headsign", description="")
    public String  m_headSign;
    
    @GTFSColumn(name="direction_id", description="")
    public DirType m_dir;
    
    @GTFSColumn(name="block_id", description="")
    public String  m_block;

    @GTFSColumn(name="shape_id", description="")
    public String  m_shape;

    @GTFSColumn(name="trip_type", description="")
    public String  m_tripType;

    private List<StopTimes>  m_stopTimes;

    static public String getFileName()
    {
        return "trips.txt";
    }
    
    public void setDir(String dir)
    {
        m_dir = DirType.construct(dir, DirType.Inbound);
    }

    public String getBlock()
    {
        return m_block;
    }

    public String getHeadSign()
    {
        return m_headSign;
    }

    public String getRoute()
    {
        return m_route;
    }

    public String getKey()
    {
        return m_key;
    }

    public String getTripID()
    {
        return m_tripID;
    }

    public String getTripType()
    {
        return m_tripType;
    }

    public DirType getDir()
    {
        return m_dir;
    }

    public String getShape()
    {
        return m_shape;
    }
    
    public static String getMainShape(List<Trips> tList)
    {       
        String retVal = null;
        
        Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
        String largest = "XXXXXXXXXXYYYYYYYYZZZZZZ";
        int    largestCount = 0;
        if(tList != null)
        for(Trips t : tList)
        {
            String  s = t.getShape();
            if(s == null || s.equals(largest)) continue;
            
            Integer c = ht.get(s);
            if(c == null) c = 1;
            else          c++;
            ht.put(s, c);
            
            if(c > largestCount)
            {
                largest = s;
                retVal = largest;
                largestCount = c;
            }
        }

        return retVal;
    }

    public List<StopTimes> getStopTimes(GTFSDataLoader loader, Agency agency)
    {
        if(m_stopTimes == null || m_stopTimes.isEmpty())
        {
            List sList = loader.getData(agency, StopTimes.class);
            List<StopTimes> stl = GTFSDataUtils.getTripTimes(sList, getTripID());
            setStopTimes(stl);
        }
        
        return m_stopTimes;
    }


    /**
     * getStopTimes -- not guaranteed to return a list...should call the getStopTimes(GDataLoader loader, Agency agency)  
     *
     * @return List<StopTimes>
     * @date May 9, 2007
     */
    public List<StopTimes> getStopTimes()
    {
        return m_stopTimes;
    }
    
    
    public void setStopTimes(List<StopTimes> stl)
    {
        if(stl == null) return;
        if(stl.isEmpty() && m_stopTimes != null) return;

        m_stopTimes = stl;
    }
}
