/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.timetablepublisher.configure.TimePoints;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.utils.IntUtils;
import org.timetablepublisher.utils.TimeTableProperties;

/**
 * Column is 
 * 
 * NOTE: placeID and stopID will be identical, unless a constructor that accepts a placeID is used.  Place ID
 *       is more an internal ID for a stop, whereas stopID would be the public ID (in instances where there are
 *       multiple ID sets -- eg: at TriMet, our scheduling software has a different ID than our public stop IDs,
 *       so the placeID is used for when we tie TTPUB up to our raw scheduling data). 
 *        
 * 
 * @author  Frank Purcell
 * @version 1.0 
 */
@SuppressWarnings("unchecked")
public class StopImpl implements Stop
{
    private static final Logger LOGGER = Logger.getLogger(StopImpl.class.getCanonicalName());
    
    protected String   m_url;
    protected String   m_stopId;
    protected String   m_placeId;
    protected Integer  m_sequence;
    protected String   m_description;
    protected boolean  m_repeated   = false;
    protected boolean  m_processed  = false;
    protected boolean  m_public     = true;
    protected boolean  m_hideStopID = false;
    protected Double   m_latitude;
    protected Double   m_longitude;
    
    // Hibernate: (hpl.id.locationId, hpl.id.placeId, rs.id.colSeq, ep.placeDescription, rs.location.latitude, rs.location.longitude 
    public StopImpl(Integer stop, String place, Integer sequence, String description, char publicFlag, BigDecimal lat, BigDecimal lng)
    {
        this(stop.toString(), place, sequence, description, lat, lng);
        setPublic(publicFlag);
    }
    public StopImpl(String stop, String place, Integer sequence, String description, BigDecimal lat, BigDecimal lng)
    {
        this(stop, place, sequence, description);
        m_latitude  = lat.doubleValue();
        m_longitude = lng.doubleValue();
    }
    public StopImpl(String stop, String place, Integer sequence, String description)
    {
        this(stop, sequence, description);
        if(place != null) {
            m_placeId = place.trim();
        }
    }
    public StopImpl(String stop, Integer sequence, String description)
    {
        m_description = description;
        m_stopId      = stop;
        m_placeId     = stop;
        m_sequence    = sequence;
    }
    
    public boolean isPublic()
    {
        return m_public;
    }
    public void setPublic(boolean pub)
    {
        m_public = pub;
    }    
    public void setPublic(Character pub)
    {
        if(pub == null) pub = 'Y';
        m_public = (pub != 'N');
    }
    
    public String getDescription(List<Stop> cList, String stopID)
    {
        return getDescriptionStatic(cList, stopID);
    }
    public static String getDescriptionStatic(List<Stop> cList, String id)
    {
        String retVal = null;
        
        Stop c = findTimePointStatic(cList, id, 0);
        if(c != null)
            retVal = c.getDescription();
        
        return retVal;
    }
    
    public static void setProcessed(List<Stop> tpList, boolean value)
    {
        for(Stop tp : tpList)
        {
            tp.setProcessed(value);
        }       
    }
    
    public boolean isProcessed()
    {
        return m_processed;
    }

    public void setProcessed(boolean processed)
    {
        m_processed = processed;
    }

    public boolean hideStopId()
    {
        return m_hideStopID;
    }
    public void setHideStopId(boolean hideStopID)
    {
        m_hideStopID = hideStopID;
    }    
    
    public String getDescription()
    {
        return m_description;
    }
    public void setDescription(String description)
    {
        m_description = description;
    }
    public Integer getSequence()
    {
        return m_sequence;
    }
    public void setSequence(Integer sequence)
    {
        m_sequence = sequence;
    }
    public String getStopId()
    {
        return m_stopId;
    }
    public void setStopId(String stop)
    {
        m_stopId = stop;
    }
    

    public Double getLatitude()
    {
        return m_latitude;
    }

    public Double getLongitude()
    {
        return m_longitude;
    }

    public void setLatitude(Double latitude)
    {
        m_latitude = latitude;
    }

    public void setLongitude(Double longitude)
    {
        m_longitude = longitude;
    }
    
    public String getPlaceId()
    {
        return m_placeId;
    }
    public void setPlaceId(String placeId)
    {
        m_placeId = placeId;
    }
    
    /*******************************
    
    STATIC COLUMN FINDER ROUTINES
    
    TODO: could be cleaned up a bit (and some routines deleted)        
    ******************************/
   
    
    public static void processStopRepeats(List<Stop> stopList)
    {
        if(stopList == null) return;
        for(int i = 0; i < stopList.size(); i++)
        {
            Stop si = stopList.get(i);
            if(si == null || si.getStopId() == null) continue;
            
            for(int j = i+1; j < stopList.size(); j++)
            {
                Stop sj = stopList.get(j);
                if(sj == null || sj.getStopId() == null) continue;
                
                
                if(si.getStopId().compareToIgnoreCase(sj.getStopId()) == 0)
                {
                    si.setRepeated(true);
                    sj.setRepeated(true);
                }
            }
        }
    }    
    
    /**
     * findStopsWithPlaceIDs will find stops that have PlaceIDs that are different from the 
     * compainion StopID.  This happens in the ES_ data, where the StopID is mapped to a 
     * scheduling given 'place' (eg: not necessarily a stop -- think intersection or transit center)
     *
     * @param stopList
     * @return List<Stop>
     * @date May 1, 2007
     */
    public static List<Stop> findStopsWithPlaceIDs(List<Stop> stopList)
    {
        return findStopsBasedOnIDEquality(stopList, false);
    }
    public static List<Stop> findStopsWithEqualIDs(List<Stop> stopList)
    {
        return findStopsBasedOnIDEquality(stopList, true);
    }
    public static List<Stop> findStopsBasedOnIDEquality(List<Stop> stopList, boolean areIDsEqual)
    {
        if(stopList == null) return null;
        
        List<Stop> retVal = new ArrayList<Stop>();
        for(Stop s : stopList)
        {
            if(s == null || s.getStopId() == null || s.getPlaceId() == null) continue;
            
            if(areIDsEqual == (s.getPlaceId().compareToIgnoreCase(s.getStopId()) == 0))
            {
                retVal.add(s);
            }
        }
        
        return retVal;
    }

    /**
     * does this list of stops have 1 or more PlaceID (that differs from the coorespondign StopID)
     */
    public static boolean hasPlaceIDs(List<Stop> stopList)
    {
        List<Stop> s = findStopsWithPlaceIDs(stopList);
        return (s != null && s.size() > 0);
    }

    /**
     * does this list of stops have stops that have PlaceIDs/StopsID and StopID/StopIDs 
     * this tells you that maybe the mapping between PlaceID and StopID worked in some intances, but not in others
     * (eg: when the route / dir don't contain these stops) 
     */
    public static boolean hasMixedPlaceIDs(List<Stop> stopList)
    {
        List<Stop> s1 = findStopsWithPlaceIDs(stopList);
        return s1 != null && s1.size() != stopList.size();
    }

    /**
     * This will return a sub-set of columns (of the input) cols.  The dividing point will be the stop id. 
     * 
     * @param cols
     * @param stopId
     * @param fromRight
     * @return
     */
    public static List<Stop> findColumns(List<Stop> cols, String stopId, boolean fromRight, boolean includeBoundryStop)
    {
        List<Stop> retVal;
        if(fromRight) {
            retVal = StopImpl.findColumnsToRight(cols, stopId, includeBoundryStop);
        }
        else {
            retVal = StopImpl.findColumnsToLeft(cols, stopId, includeBoundryStop);
        }

        return retVal;
    }
        
    public static List<Stop> findColumnsToLeft(List<Stop> cols, String stopId, boolean includeBoundryStop)
    {   
        List<Stop> retVal = new ArrayList<Stop>();
        if(cols == null || stopId == null) return retVal;

        // find the column with this stopId
        Integer j = null;
        for(int i = 0; i < cols.size(); i++)
        {
            Stop c = cols.get(i);
            if(c == null) continue; 
            
            if((c.getStopId() != null && c.getStopId().compareToIgnoreCase(stopId)  == 0) 
            || (c.getPlaceId()!= null && c.getPlaceId().compareToIgnoreCase(stopId) == 0))
            {
                j = i;
                break;
            }
        }
        
        // now, add all stops from zero to stopId
        if(j != null)
        {                        
            for(int i = 0 ; i < j; i++)
            {
                retVal.add(cols.get(i));
            }
            
            if(includeBoundryStop)
            {
                retVal.add(cols.get(j));
            }
        }
        return retVal;
    }   
    public static List<Stop> findColumnsToRight(List<Stop> cols, String stopId, boolean includeBoundryStop)
    {   
        List<Stop> retVal = new ArrayList<Stop>();
        if(cols == null || stopId == null) return retVal;

        // find the column with this stopId
        Integer j = null;
        for(int i = cols.size() - 1; i >= 0 ; i--)
        {
            Stop c = cols.get(i); 
            if(c == null) continue;  
            
            if((c.getStopId() != null && c.getStopId().compareToIgnoreCase(stopId)  == 0) 
            || (c.getPlaceId()!= null && c.getPlaceId().compareToIgnoreCase(stopId) == 0))
            {
                j = i;
                break;
            }
        }
        
        // now, add all stops from j to end
        if(j != null)
        {    
            if(includeBoundryStop)
            {
                retVal.add(cols.get(j));
            }
            
            for(int i = j+1 ; i < cols.size(); i++)
            {
                retVal.add(cols.get(i));
            }            
        }
        return retVal;
    }
    
    public static List<String> toPlaceIDs(List<Stop> cols)
    {
        return toPlaceIDs(cols, false); 
    }
    public static List<String> toPlaceIDs(List<Stop> cols, boolean toUpper)
    {
        List<String> retVal = new ArrayList<String>();
        for(Stop c : cols)
        {            
            retVal.add(toUpper ? c.getPlaceId().toUpperCase() : c.getPlaceId());
        }
        
        return retVal;
    }
    public static List<String> columnsToStopIDs(List<Stop> cols)
    {
        List<String> retVal = new ArrayList<String>();
        for(Stop c : cols)
        {
            retVal.add(c.getStopId());
        }
        
        return retVal;
    }
    
    /**
     * this method is needed for TRANS -- stopID's are int in that schema.  If you use Strings in the query,
     * you'll get a ORA-01008: not all variables bound error
     *  
     * @param cols
     * @return List<Integer>
     */
    public static List<Integer> toStopIDsAsIntegers(List<Stop> cols)
    {
        List<Integer> retVal = new ArrayList<Integer>();
        for(Stop c : cols)
        {
            retVal.add(IntUtils.getIntegerFromString(c.getStopId()));
        }
        
        return retVal;
    }
    

    public static Stop findTimePointStatic(List<Stop> cList, String id, int start)
    {
        if(cList == null || id == null) return null;
        if(start < 0) start = 0;
        
        Stop retVal = null;
        for(int i = start; i < cList.size(); i++)
        {
            Stop c = cList.get(i);
            if(c == null) continue;
            if((c.getStopId() != null && c.getStopId().compareToIgnoreCase(id) == 0) 
            || (c.getPlaceId()!= null && c.getPlaceId().compareToIgnoreCase(id) == 0))
            {
                retVal = c;
                break;
            }                
        }
        
        return retVal;
    }
    public static Stop findTimePointStatic(List<Stop> cList, String stopID)
    {
        return findTimePointStatic(cList, stopID, 0);   
    }
    public Stop findTimePoint(List<Stop> cList, String stopID)
    {
        return findTimePointStatic(cList, stopID, 0);
    }    
    
    public static Stop findPrevColumn(List<Stop> cList, String id)
    {
        if(cList == null || id == null) return null;
        
        Stop retVal  = null;
        Stop prevCol = null;
        
        for(Iterator<Stop>iter = cList.iterator(); iter.hasNext();)
        {
            Stop c = iter.next();
            if(c == null) continue;
            if(c.getStopId().compareToIgnoreCase(id) == 0 || c.getPlaceId().compareToIgnoreCase(id) == 0)
            {
                retVal = prevCol;
                break;
            }            
            prevCol = c;
        }
        
        return retVal;
    }
    public static Stop findNextColumn(List<Stop> cList, Integer id)
    {
        if(cList == null || id == null) return null;
        String idStr = Integer.toString(id);
        Stop retVal  = null;
        for(Iterator<Stop>iter = cList.iterator(); iter.hasNext();)
        {
            Stop c = iter.next();
            if(iter.hasNext() && c != null)
            {
                if(c.getStopId().equals(idStr) || c.getPlaceId().equals(idStr))
                {
                    retVal = iter.next();
                    break;
                }                
            }
        }
        
        return retVal;
    }

    
    public static Integer findColumnIndex(List<String> cols, String stopId, int startIndex)
    {
        Integer retVal = null;
        
        for(int i = startIndex; i < cols.size(); i++)
        {                   
            String c = cols.get(i);
            if(c != null && c.compareToIgnoreCase(stopId) == 0)
            {                
                retVal = i;
                break;
            }
        }        

        return retVal;
    }

    public static Integer findColumnIndex(List<Stop> cols, String stopId)
    {
        return findColumnIndex(cols, stopId, false);
    }
    public static Integer findColumnIndex(List<Stop> cols, String stopId, boolean fromRight)
    {
        Integer retVal;
        if(fromRight) {
            retVal = StopImpl.findColumnFromRight(cols, stopId, cols.size() - 1);
        }
        else {
            retVal = StopImpl.findColumnFromLeft(cols, stopId, 0);
        }

        return retVal;
    }
    public static Integer findColumnFromRight(List<Stop> cols, String stopId, int y)
    {
        Integer retVal = null;
        
        for(int i = y; i >= 0; i--)
        {                            
            Stop c = cols.get(i);
            if(c == null) continue;

            // double check: just in case the stop ID is really a place ID
            if(c.getPlaceId().compareToIgnoreCase(stopId) == 0 || c.getStopId().compareToIgnoreCase(stopId) == 0)
            {
                retVal = i;
                break;
            }
        }        

        return retVal;
    }
    public static Integer findColumnFromLeft(List<Stop> cols, String stopId, int y)
    {
        if(stopId == null || cols == null) return null;
        
        Integer retVal = null;
        
        for(int i = y; i < cols.size(); i++)
        {                            
            Stop c = cols.get(i);
            if(c == null || c.getStopId() == null) continue;

            // double check: just in case the stop ID is really a place ID
            if(c.getStopId().compareToIgnoreCase(stopId) == 0 || c.getPlaceId().compareToIgnoreCase(stopId) == 0)
            {
                retVal = i;
                break;
            }
        }        

        return retVal;
    }
    
    public static boolean isStopInListMoreThanOnce(List<Stop> list, String stopId)
    {
        boolean retVal = false;
        
        Integer i = findColumnFromLeft(list, stopId, 0);
        if(i != null && i < list.size())
        {
            Integer j = findColumnFromLeft(list, stopId, i+1);
            if(j != null && j < list.size())
                retVal = true;
        }
        
        return retVal;
    }
    
    public static boolean isStopInList(List<Stop> list, String stopId)
    {
        if(stopId == null || list == null) return false;
        
        boolean retVal = false;
        for(String i : StopImpl.columnsToStopIDs(list))
        {
            if(i == null) continue;
            if(i.compareToIgnoreCase(stopId) == 0)
            {
                retVal = true;
                break;                  
            }
        }
        return retVal;
    }
    
    public static Integer findSequence(List<Stop> cList, String stopID, Integer def)
    {
        Integer retVal = def; 
        
        Stop c = findTimePointStatic(cList, stopID);
        if(c != null && c.getSequence() != null)
        {                    
            retVal = c.getSequence();
        }
        
        return retVal;
    }
    
    /**
     * This is a generic column culling routine.  Below is a description why it's used at TriMet.
     * 
     * TRANS's ScheduledStopTime (SST) table, unlike the Early Schedule Data, does not contain 'layover' times (eg: multiple times 
     * at a given stop, representing the distinct arrival and departure times of a vechicle).  Rather, SST just has departure times
     * 
     * Because a few routes (eg: 31-Estacada and 32-Oatfield) are presented with both arrival & departure times in the printed material,
     * the configuration will have same-stop column pairs.  When TRANS data is applied, you'd end up with a blank column.  So rather than
     * that, this routine will cull the same-column layover pair from the input column

     * @param stops
     * @return
     */
    public static List<Stop> cullRepeatColumns(List<Stop> stops)
    {
        if(stops == null || stops.size() < 2) return null;
        
        List<Stop> retVal = stops;
        for(int i = 0; i < stops.size() - 1; i++)
        {
            Stop colA = stops.get(i);
            Stop colB = stops.get(i+1);            
            if(colA == null || colB == null) continue;
            
            String stopA = colA.getStopId();
            String stopB = colB.getStopId();
            if(stopA == null || stopB == null) continue;
            if(stopA.compareToIgnoreCase(stopB) == 0)
            {
                retVal.remove(i);
            }
        }
        
        return retVal;
    }
    
    public static void copyAttributes(Stop source, Stop target)
    {
        if(target == null || source == null) return;
        
        if(source.getLatitude()   != null)   target.setLatitude(source.getLatitude());
        if(source.getLongitude()  != null)   target.setLongitude(source.getLongitude());
        if(source.getDescription()!= null)   target.setDescription(source.getDescription());
        if(source.getStopId()     != null)   target.setStopId(source.getStopId());
        if(source.getPlaceId()    != null)   target.setPlaceId(source.getPlaceId());
        target.setPublic(source.isPublic());
        target.setHideStopId(source.hideStopId());
    }
    
    
    /**
     * QUICK & DIRTY way to TRIM A BIG TIMETABLE DOWN TO ONLY A HANDFUL OF STOPS
     * 
     * this method will trim down a large list of timepoints (as seen in the interpolated stop times of the GTFS)
     * and return back a list that culls out intemediary stops.
     * 
     * NOTE: this routine is fairly naive...it only culls based on the position in the table.
     *       it would be better to cull based on stop time or linear distance...
     * 
     * @param tt
     * @param timePoints
     * @param query 
     * @return List<Stop>
     */
    public static List<Stop> trimTimePointList(TimesTable tt, List<Stop> timePoints, ScheduleDataQuery query)
    {
        List<Stop> retVal = timePoints;
        
        try
        {
            // if we're dealing with configured stops, then don't cull the list of stops -- only when it's an unconfigured list do we cull to MAX
            List<TimePoints> tpList = tt.getConfiguration().findAllData(new TimePoints(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage()));
            if(tpList != null && tpList.size() > 0)
                return retVal;

            
            int max = TimeTableProperties.MAX_TIMEPOINTS.get(Integer.MAX_VALUE);
            double m = Math.ceil(timePoints.size() / (max * 1.0));
            int    n = (int) m;
            if(n > 1)
            {                
                List<Stop> tp = new ArrayList<Stop>();
                int end = timePoints.size() - 1;
                int skip;
                for(int i = 0; i < end; i+=skip)
                {
                    Stop s = timePoints.get(i);
                    if(s == null)
                    {
                        skip = 1;
                        continue;
                    }
                    tp.add(s);
                    skip = n;
                }

                // always put the last stop in our timepoint list
                Stop s = timePoints.get(end);
                if(s != null)
                {
                    if(tp.size() < max)
                        tp.add(s);
                    else if(tp.size() > 1)
                        tp.set(tp.size() - 1, s);
                }

                // and finally, assign our new list to our return value
                retVal = tp;
            }
        }
        catch(Exception e)
        {
            LOGGER.log(Level.ALL, "Didn't trim the timepoints -- could be wrong CONFIGURE_DIRECTORY setting in TimeTableProperties.properties", e);
        }

        return retVal;
    }
    
    
    public String getUrl() {
        return m_url;
    }
    public void setUrl(String url) {
        m_url = url;
    }
    public final boolean isRepeated()
    {
        return m_repeated;
    }
    public final void setRepeated(boolean repeated)
    {
        m_repeated = repeated;
    }
    
    /**
     * Will find timepoints in other routes direction
     */
    public static List<Stop> getTimePointsInOppositeRouteDirection(TimesTable tt, ScheduleDataQuery query)
    {
        return query.queryTimePoints(tt.getAgencyName(), tt.getRouteID(), tt.getDir().getOpposite(), tt.getKey(), tt.getActiveServiceKeys(), tt.bypassConfig(), tt.getDate());
    }
    
    /**
     * will fix up the stop & place ids 
     */
    public static void fixUpStops(List<StopImpl> fixMeList, List<Stop> fixerList)
    {
        for(StopImpl s : fixMeList)
        {
            Stop tmp = findTimePointStatic(fixerList, s.getStopId());
            s.fix(tmp);
        }
    }
    
    public void fix(Stop fixer)
    {
        if(fixer != null)
        {
            if(fixer.getStopId()  != null) setStopId(fixer.getStopId());
            if(fixer.getPlaceId() != null) setPlaceId(fixer.getPlaceId());
        }
    }
}
