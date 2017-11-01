/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.TimesTable;

/**
 * The purpose of RenameTimePoint is to rename stops...either by renaming the stop
 * name (description) or else renaming the stop ID.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class RenameTimePoint extends Configure 
{
    @CsvColumn(index=6, name="Original StopID / PlaceID", details="the stopID of the stop you're looking to rename")    
    public String   m_originalStopId = null;    
    
    @CsvColumn(index=7, name="Printed Stop ID", details="the stopID, as it will appear in the table")
    public String   m_publicStopId   = null;
    
    @CsvColumn(index=8, name="Printed Name / Description", details="the name of the stop, as it will appear in the table")
    public String   m_publicStopName = null;

    public RenameTimePoint()
    {        
        super(); 
    }

    public RenameTimePoint(TimesTable tt)
    {        
        super(tt); 
    }
    
    public RenameTimePoint(String agency, String route, String dir, String key, String lang)
    {
        super.setValues(agency, route, dir, key, lang);
    }

    
    public void setValues(String agency, String route, String dir, String key, String lang, String[] csv)
    {
        super.setValues(agency, route, dir, key, lang, csv);
        getOriginalStopId();
    }
    
    public String getOriginalStopId() 
    {
        // want the timePointStopId to have something valid, even if the .CSV config file has nothing (which is legit in the CSV)
        if(m_originalStopId == null || m_originalStopId.length() < 1) {
            m_originalStopId = m_publicStopId; 
        }
        
        return m_originalStopId;
    }
    public void setOriginalStopId(String originalStopId)
    {
        if(originalStopId == null) return;
        m_originalStopId = originalStopId.trim();
        
        // want the timePointStopId to have something valid, even if the .CSV config file has nothing (which is legit in the CSV)
        if(m_originalStopId == null || m_originalStopId.length() < 1) {
            m_originalStopId = m_publicStopId; 
        }        
    }

    public String getPublicStopName() 
    {
        return m_publicStopName;
    }
    public void setPublicStopName(String description) {
        if(description == null || description.trim().length() < 1) return;
        m_publicStopName = description.trim();
    }

    public String getPublicStopId()
    {
        return m_publicStopId;
    }
    public void setPublicStopId(String publicStopId)
    {
        if(publicStopId == null) 
            m_publicStopId = null;
        else
            m_publicStopId = publicStopId.trim();
    }
    

    public static Hashtable<String, String> getRenameTimePointMapping(List<RenameTimePoint> data, boolean cullMatches)
    {
        Hashtable<String, String> retVal = new Hashtable<String, String>();
        if(data == null || data.size() < 1) return retVal;
        
        for(RenameTimePoint tp : data)
        {
            // make a column from the database, using the id & sequence from the CSV file
            String pubStopID = tp.getPublicStopId();
            String tpStopID  = tp.getOriginalStopId();
            if(pubStopID == null || tpStopID == null) continue;
            if(cullMatches && isSameStopID(tpStopID, pubStopID)) 
                continue; // eg: don't add to return if pubStopID & tpStopID are same...only uniques returned when culling matches
            retVal.put(pubStopID, tpStopID);
        }
        
        return retVal;
    }
    public static Hashtable<String, String> getRenameTimePointMapping(ConfigurationLoader loader)
    {
        if(loader == null) return null;
        List<RenameTimePoint> data = loader.findAllData(new RenameTimePoint());
        return getRenameTimePointMapping(data, false);
    }
    public static Hashtable<String, String> getRenameTimePointMapping(TimesTable tt, boolean cullMatches)
    {
        if(tt == null || tt.getConfiguration() == null) return null;
        
        List<RenameTimePoint> data = tt.getConfiguration().findAllData(new RenameTimePoint(tt));
        return getRenameTimePointMapping(data, cullMatches);
    }
    public static Hashtable<String, String> getRenameTimePointMapping(TimesTable tt)    
    {
        return getRenameTimePointMapping(tt, false);
    }
    
    
    /**
     * checkPlaceNames is a method that will re-do stops, based on the RenameTimePoint configuration.,
     *
     * @param m_tt
     * @param query
     * @param stopList void
     * @date May 1, 2007
     */
    public static void checkPlaceNames(TimesTable tt, ScheduleDataQuery query, List<Stop> stopList)
    {
        if(!okToProcess(tt, RenameTimePoint.class)) return;
        
        List<Stop> placeIdList = StopImpl.findStopsWithPlaceIDs(stopList);
        if(placeIdList != null && placeIdList.size() > 0)
        {
            Hashtable<String, String> ht = RenameTimePoint.getRenameTimePointMapping(tt, true);
            if(ht != null && ht.size() > 0)
            {
                Set<String> keyList = ht.keySet(); 
                for(Stop stop : placeIdList)
                {
                    for(String pubStopID : keyList)
                    {
                        String tpStopID = ht.get(pubStopID);
                        if(tpStopID == null) continue;
                        
                        if(tpStopID.compareToIgnoreCase(stop.getPlaceId()) == 0 
                        && pubStopID.compareToIgnoreCase(stop.getStopId()) != 0)
                        {
                             Stop newStop = query.makeStop(tt.getConfiguration(), tt.getAgencyName(), pubStopID, tpStopID, stop.getSequence());
                             StopImpl.copyAttributes(newStop, stop);
                             stop.setPlaceId(tpStopID);
                             break;
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     *  make sure there are distinct place names within timepoints
     *  NOTE: we don't get place names when get a place
     */ 
    public static void addPlaceNames(TimesTable tt, ScheduleDataQuery query)
    {
        // step 1: make sure we're OK
        if(tt != null && tt.getTimePoints() != null && !tt.bypassConfig() && query != null) 
        {
            // step 2: find all stops that have the same stop & place id value
            List<Stop> targetList = StopImpl.findStopsWithEqualIDs(tt.getTimePoints());
            if(targetList != null && targetList.size() > 0)
            {
                // step 3a: get the table of mappings 
                Hashtable<String, String> ht = RenameTimePoint.getRenameTimePointMapping(tt, true);                
                
                // step 3: all of the stops from the database
                List<Stop> schTPs = query.queryTimePoints(tt.getAgencyName(), tt.getRouteID(), tt.getDir(), tt.getKey(), tt.getActiveServiceKeys(), tt.bypassConfig(), tt.getDate());
                if(schTPs != null && schTPs.size() > 0)
                {
                    // step 4: now we're going to try & find a place match in the timepoints from scheduling
                    for(Stop s : targetList)
                    {
                        if(s == null) continue;
                        String stopID = s.getStopId();
                        if(stopID == null) continue;
                        
                        // step 5: find the placeID - first in Rename / else in the schedule
                        String placeID = null;                        
                        if(ht != null && ht.containsKey(stopID))
                        {
                            placeID = ht.get(stopID);
                        }
                        else
                        {
                            Stop y = StopImpl.findTimePointStatic(schTPs, stopID);
                            if(y != null) 
                                placeID = y.getPlaceId();                            
                        }
                        
                        // step 6: put the place into our target stop
                        if(placeID != null)
                        {
                            s.setPlaceId(placeID);
                        }
                    }
                }
            }
        }            
    }
           

    /**
     * Returns the 'Origianl Stop ID'
     * Input a StopID.  If that maps to a 'Public Stop ID' in the config, this routine will return the 'Original Stop ID'
     * 
     * @param in
     * @param stopID
     * @return
     */
    public static String findOriginalStopID(TimesTable tt, String stopID)
    {
        return findOriginalStopID(tt.getConfiguration(), tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage(), stopID);        
    }

    public static String findOriginalStopID(ConfigurationLoader loader, Configure in, String stopID)
    {
        return findOriginalStopID(loader, in.getAgency(), in.getRouteID(), in.getDir(), in.getKey(), in.getLang(), stopID);        
    }

    
    /**
     * Returns the 'Origianl Stop ID'
     * Input a StopID.  If that maps to a 'Public Stop ID' in the config, this routine will return the 'Original Stop ID'
     * 
     * @param route
     * @param dir
     * @param key
     * @param lang
     * @param stopId
     * @return
     */
    public static String findOriginalStopID(ConfigurationLoader loader, String agency, String route, String dir, String key, String lang, String stopID)
    {
        if(loader == null || stopID == null) 
            return stopID;
        
        List data = loader.findAllData(new RenameTimePoint(agency, route, dir, key, lang));        
        if(data != null && data.size() > 0)
        {
            for(RenameTimePoint tp : (List<RenameTimePoint>)data)
            {
                String tpStopID = tp.getOriginalStopId();
                if(tpStopID == null) continue;

                String pubStopID = tp.getPublicStopId();
                if(pubStopID == null) continue;
                
                // don't process if both original & public are the same
                if(pubStopID.compareToIgnoreCase(tpStopID) == 0) 
                    continue;
                
                if(stopID.compareToIgnoreCase(pubStopID) == 0)
                {
                    return tpStopID;
                }
            }
        }
        
        return stopID;
    }


    /**
     * Returns the 'Public Stop ID'
     * Input a StopID.  If that maps to an 'Original Stop ID' in the config, this routine will return the matching 'Public Stop ID'
     * 
     * @param TimesTable
     * @param stopId
     * @return
     */
    public static String findRenamedStopID(TimesTable tt, String stopID)
    {
        if(tt == null) 
            return stopID;

        return findRenamedStopID(tt.getConfiguration(), tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage(), stopID);
    }    

    /**
     * Returns the 'Public Stop ID'
     * Input a StopID.  If that maps to an 'Original Stop ID' in the config, this routine will return the matching 'Public Stop ID'
     * @param loader 
     * 
     * @param in
     * @param stopID
     * @return
     */
    public static String findRenamedStopID(ConfigurationLoader loader, Configure in, String stopID)
    {
        return findRenamedStopID(loader, in.getAgency(), in.getRouteID(), in.getDir(), in.getKey(), in.getLang(), stopID);        
    }
    
    /**
     * Returns the 'Public Stop ID'
     * Input a StopID.  If that maps to an 'Original Stop ID' in the config, this routine will return the matching 'Public Stop ID'
     * 
     * @param route
     * @param dir
     * @param key
     * @param lang
     * @param stopId
     * @return
     */
    public static String findRenamedStopID(ConfigurationLoader loader, String agency, String route, String dir, String key, String lang, String stopId)
    {        
        if(loader == null || stopId == null) 
            return stopId; 
            
        
        List data = loader.findAllData(new RenameTimePoint(agency, route, dir, key, lang));        
        if(data != null && data.size() > 0)
        {
            for(RenameTimePoint tp : (List<RenameTimePoint>)data)
            {
                if(tp == null || tp.getOriginalStopId() == null || tp.getPublicStopId() == null) continue;
                if(stopId.compareToIgnoreCase(tp.getOriginalStopId()) == 0)
                {
                    return tp.getPublicStopId();
                }
            }
        }
        
        return stopId;
    }
    
    /**
     * Returns either NULL (if match not found), or the renamed stop
     * 
     * @param m_tt
     * @param stopID
     * @param query
     * @return
     */
    public static String findStopReName(String stopID, TimesTable tt)
    {
        String retVal = stopID;
        if(!okToProcess(tt, Configure.class)) 
            return retVal;

        List rnTpList = tt.getConfiguration().findAllData(new RenameTimePoint(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage()));
        if(rnTpList == null || rnTpList.size() < 1) 
            return retVal;

        for(RenameTimePoint tp : (List<RenameTimePoint>)rnTpList)
        {
            if(matchesStopId(tp, stopID))
            {
                String tmp = tp.getPublicStopName();
                if(tmp == null || tmp.length() < 1) continue;

                retVal = tmp;
                break;
            }
        }
        
        return retVal;
    }    
    
    public static void process(TimesTable tt, ScheduleDataQuery query)
    {
        if(!okToProcess(tt, Configure.class)) return;
        process(query, tt.getConfiguration(), tt.getTimePoints(), tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage());
    }
    public static void process(TimesTable tt, List<Stop> stops, ScheduleDataQuery query)
    {
        if(!okToProcess(tt, Configure.class)) return;
        process(query,  tt.getConfiguration(), stops, tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage());
    }
    
    synchronized public static void process(ScheduleDataQuery query, ConfigurationLoader loader, List<Stop> stops, String agency, String route, String dir, String key, String lang)
    {
        if(loader == null || stops == null || query == null) return;
        
        List rnTpList = loader.findAllData(new RenameTimePoint(agency, route, dir, key, lang));
        if(rnTpList == null || rnTpList.size() < 1) return;

        for(RenameTimePoint rn : (List<RenameTimePoint>)rnTpList)
        {
            if(rn == null) continue;
            
            String tpStopID  = getTimePointStopID(rn);
            String pubStopID = rn.getPublicStopId();           

            // this condition won't happen unless both stopID's are null / blank 
            if(tpStopID == null || tpStopID.length() < 1) continue;
            
            boolean ignoreConfig = loader.ignoreConfig(RenameTimePoint.class);
            boolean hideStopID   = (pubStopID == null || pubStopID.length() < 1);
            boolean hasStopName  = (rn.getPublicStopName() != null && rn.getPublicStopName().length() > 0);
            boolean isDifferentStopID = isDifferentStopID(tpStopID, pubStopID);
            
            for(Stop c : stops)
            {
                if(c == null || c.getStopId() == null || c.getStopId().compareToIgnoreCase(tpStopID) != 0) continue;
                
                // if the rename wants to map to a new stop id, then go here
                if(isDifferentStopID)
                {
                    if(hideStopID)
                    {
                        // note: the only condition that ignore config doesn't cover is hiding stop ids
                        c.setHideStopId(true);
                    }
                    else if(!ignoreConfig)
                    {
                        Stop ns = query.makeStop(loader, agency, pubStopID, tpStopID, 1);
                        StopImpl.copyAttributes(ns, c);
                    }                                            
                }

                // rename this stop via the given name from the configuration
                if(hasStopName && !ignoreConfig)
                {
                    c.setDescription(rn.getPublicStopName());
                }                    
            }                
        }
    }
    
    private static boolean isSameStopID(String tpStopID, String pubStopID)
    {
        if(pubStopID == null || tpStopID == null) return false;
        return tpStopID.compareToIgnoreCase(pubStopID) == 0;
    }
    private static boolean isDifferentStopID(String tpStopID, String pubStopID)
    {
        if(pubStopID == null && tpStopID == null) return false;
        if(pubStopID == null)                     return true;
        return tpStopID.compareToIgnoreCase(pubStopID) != 0;
    }

    private static String getTimePointStopID(RenameTimePoint tp)
    {
        String retVal = tp.getOriginalStopId();
        if(retVal == null || retVal.length() < 1) 
        {
            retVal = tp.getPublicStopId();           
        }
        
        return retVal; 
    }
    
    private static boolean matchesStopId(RenameTimePoint tp, String stopID)
    {
        boolean retVal = false;
        
        String tpStopID = getTimePointStopID(tp);
        if(stopID != null && tpStopID != null)
        {
            retVal = stopID.compareToIgnoreCase(tpStopID) == 0;
        }
        
        return retVal;
    }
}    
