/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.IntUtils;
import org.timetablepublisher.utils.Params;


/**
 * The purpose of TimePoints is to define which route stops are to be displayed
 * within the Time Table.  Often, the schedule data contains far more data than
 * would fit within a readable timetable.  This Configuration will cull out
 * timepoints, only showing which stops / columns that are defined within.
 * 
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2007
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class TimePoints extends Configure 
{
    @CsvColumn(index=6, name="Stop ID",   details="a specified stop id for this route / dir / key (one of N, where N > 1)")
    public String   m_stopID   = null;
    
    @CsvColumn(index=7, name="Stop Name", details="an optional re-naming of this stop (NOTE: it's better to use the RenameStops Configuration)")
    public String   m_name     = null;
    
    @CsvColumn(index=8, name="Sequence",  details="the sequence, relative to the other defined route stops (also TimePoints Configurations), which determines the stop position in the table.")
    public Integer  m_sequence = null;
    
    public String   m_uniqueID = null;

    public TimePoints()
    {        
    }
    
    public TimePoints(String agency, String route, String dir, String key, String lang)
    {
        super.setValues(agency, route, dir, key, lang);
    }

    public void setValues(String agency, String route, String dir, String key, String lang, String[] csv)
    {
        super.setValues(agency, route, dir, key, lang, csv);
        fixLanguage();        
    }

    protected void fixLanguage()
    {
        // If the stop name is not edited, then there's no need to localize this TimePoint.  
        // NOTE: The m_lang variable, if present, will influence the stop configuration  
        if(m_name == null || m_name.length() < 1)
        {
            m_lang = null;
        }
    }

    /**
     * getPlaceID is here because of HASTUS PLACE IDs, and the RenameTimePoint configuration.
     * If we rename the a place name, this method will return that renamed id.  Else, it
     * will return the stop id.
     * @param m_tt 
     * 
     * @return either a stop ID, or else maybe a HASTUS place ID (if this stop was renamed)
     * @see RenameTimePoint.findOriginalStopID
     */
    public String getPlaceId(TimesTable tt) 
    {
        if(!okToProcess(tt, RenameTimePoint.class)) 
            return m_stopID;
        
        return RenameTimePoint.findOriginalStopID(tt.getConfiguration(), this, m_stopID);
    }


    /**
     * getStopID will return either the RAW stop ID, or else the renamed stopID from 
     * 
     * @return either a stop ID, or else maybe a HASTUS place ID (if this stop was renamed)
     * @see RenameTimePoint.findRenamedStopID
     */
    public String getStopID(ConfigurationLoader loader)
    {
        if(!okToProcess(loader, RenameTimePoint.class)) 
            return m_stopID;
        
        return RenameTimePoint.findRenamedStopID(loader, this, m_stopID);
    }
    public String getStopID()
    {
        return m_stopID;
    }    
    public void setStopID(String stopID)
    {
        if(stopID == null) return;
        m_stopID = stopID.trim();
    }

    public String getName() 
    {
        return m_name;
    }
    public void setName(String description) 
    {
        if(description == null) return;
        m_name = description.trim();
    }

    public int getSequence() 
    {
        if(m_sequence == null) 
            m_sequence = 111;
        
        return m_sequence;
    }
    public void setSequence(String sequence) 
    {
        m_sequence = IntUtils.getIntegerFromString(sequence, 100);
    }    
    public void setSequence(Integer sequence) 
    {
        m_sequence = sequence;
    }
    
    
    /**
     * This routine gives a semi 'unique' ID to each TimePoints object.  Where two TimePoints objects can share the same
     * ID is when they share the same 5 attributes.  EG: when a route has the same stop used multiple times (eg: layovers),
     * then all of these objects end up with the same Unique ID.  Other than that condition (same stop ID), these thing should
     * be fairly unique.
     * 
     * @return a semi-unique ID for this TimePoint that is unique to StopID / Route / Direction / Service Key / Language
     * @see findUniqueStopIDs() to see how this is used
     */
    public String getUniqueId()
    {
        if(m_uniqueID == null)
        {
            m_uniqueID = m_agency + m_routeID + m_stopID + m_dir + m_key + m_lang;
        }
        return m_uniqueID;
    }

    /**
     * Returns a TimePoints object filled with this route's parameters
     * 
     * @param params
     * @return
     */
    public static TimePoints getTimePoint(Params params)
    {   
        String agency = params.getAgency();
        String  route = params.getRouteID();
        String  lang  = null;  // NOTE: only want to set lang information when a Stop Name is present -- not here
        String  dir   = "*";
        String  key   = "*";
        
        // check the config filters, and apply appropriately 
        if(params.isUseKey()) key = params.getKey().toString();
        if(params.isUseDir()) dir = params.getDir().toString();
        
        TimePoints tp = new TimePoints(agency, route, dir, key, lang);
        return tp;
    }
    
    /**
     * Returns a TimePoints object filled with this route's parameters, providing that
     * the coresponding 'use' parameters are set by the web app.  EG: these are the 
     * CHECKBOXs for route/dir/key on the CONFIGURE page  
     * 
     * @param params
     * @return
     */
    public static TimePoints getFinderTimePoint(Params params)
    {        
        String agency = params.getAgency();
        String  route = params.isUseRoute() ? params.getRouteID()        : null;
        String  dir   = params.isUseDir()   ? params.getDir().toString() : null;
        String  key   = params.isUseKey()   ? params.getKey().toString() : null;
        String  lang  = params.getLanguage();
        
        TimePoints findMe = new TimePoints(agency, route, dir, key, lang);
        return findMe;
    }    
    
    /**
     * finds all the TimePoints that match this object
     * @param m_tt 
     * 
     * @return List of TimePoints
     */
    public List<TimePoints> find(TimesTable tt)
    {
        if(tt == null || tt.getConfiguration() == null) return null;
        
        List<TimePoints> retVal = new ArrayList<TimePoints>();
        List<Configure>  points = tt.getConfiguration().findAllData(this);

        if(points != null)
        for(Configure in : points)
        {
            TimePoints t = (TimePoints)in;
            
            // compare any / all TimePoint parameters -- one is not null and it doesn't match, continue to the next Index
            if(diff(m_stopID,      t.m_stopID))   continue;
            if(diff(m_name,        t.m_name))     continue;
            if(diff(m_sequence,    t.m_sequence)) continue;
            
            // if we get here, we've got a match
            retVal.add(t);
        }
        
        return retVal;
    }
    
    /**
     * like find(), finds all the TimePoints that match this object.  BUT we then filter on the
     * stop ID, such that there are no two TimePoints objects in the returned list with the
     * same stopID.  This helps when trying to delete a stop from the talbe, where you usually
     * want to delete one of the two, and not both (eg: at least in the Configure tool).
     * @param m_tt 
     * 
     * @see find()
     * @return List of TimePoints
     */
    public List<TimePoints> findUniqueStopIDs(TimesTable tt)
    {
        List<TimePoints> retVal  = new ArrayList<TimePoints>();
        List<TimePoints> tmpList = find(tt);

        if(tmpList != null)
        {
            Set<String> uniqueIDs = new HashSet<String>();        
            for(TimePoints tp : tmpList)
            {
                // filter: only add a TimePoints object to the return list when no matching stop ID has been seen
                String unique = tp.getUniqueId();
                if(!uniqueIDs.contains(unique))
                {
                    retVal.add(tp);
                    uniqueIDs.add(tp.getUniqueId());
                }
            }            
        }        
        return retVal;
    }

    
    /**
     * Makes a list of TimePoints objects based on (just) the TimeTable -- so it's specific to this TT's route / dir / key
     * 
     * @param m_tt
     * @return
     */
    public static List<TimePoints> makeTimePoints(TimesTable tt)
    {
        if(tt == null || tt.getTimePoints() == null || tt.getConfiguration() == null) return null;
        
        List<TimePoints> retVal = new ArrayList<TimePoints>();        
        for(Stop c : tt.getTimePoints())
        {
            TimePoints tp = new TimePoints(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage());
            tp.setStopID(RenameTimePoint.findRenamedStopID(tt.getConfiguration(), tp, c.getStopId()));
            tp.setSequence(c.getSequence());
            
            retVal.add(tp);
        }   
                
        return retVal;
    }

    /**
     * Makes a list of TimePoints objects based on this Route -- but it is NOT specific to this TT's dir & key.  Rather, it will
     * include all directions & keys, or limit those based on the passed in params.
     * 
     * @param m_tt
     * @return
     */
    public static List<TimePoints> makeTimePoints(TimesTable tt, Params params)
    {
        if(tt == null || tt.getTimePoints() == null || tt.getConfiguration() == null) return null;
        
        List<TimePoints> retVal = new ArrayList<TimePoints>();

        KeyType[] keysArray = KeyType.toKeyArray(params.isUseKey() ? tt.getKey() : null);
        DirType[] dirsArray = DirType.toDirArray(params.isUseDir() ? tt.getDir() : null, tt.getDir());
        
        for(Stop c : tt.getTimePoints())
        {
            Integer seq = c.getSequence();
            if(seq == null || seq == Constants.PHANTOM_SEQ) continue;
            
            String stopID = RenameTimePoint.findRenamedStopID(tt, c.getStopId());
            if(stopID != null)
            for(DirType dir : dirsArray)
            {
                for(KeyType key : keysArray)
                {
                    TimePoints tp = new TimePoints(tt.getAgencyName(), tt.getRouteID(), dir.toString(), key.toString(), tt.getLanguage());
                    tp.setStopID(stopID);
                    tp.setSequence(c.getSequence());
                    tp.fixLanguage();
                    retVal.add(tp);
                }
            }
        }   
                
        return retVal;
        
    }

    public static List<TimePoints> makeTimePoints(String[] newStopIDs, TimesTable tt, Params params)
    {
        if(params == null || tt == null)                return null;
        if(newStopIDs == null && newStopIDs.length < 1) return null;
        
        List<TimePoints> retVal = new ArrayList<TimePoints>();

        KeyType[] keysArray = KeyType.toKeyArray(params.isUseKey() ? tt.getKey() : null);
        DirType[] dirsArray = DirType.toDirArray(params.isUseDir() ? tt.getDir() : null, tt.getDir());

        for(String sId : newStopIDs)
        {            
            if(sId == null) continue;
            
            String  stopId   = RenameTimePoint.findRenamedStopID(tt, sId);            
            Integer sequence = StopImpl.findSequence(tt.getRouteStops(), stopId, null);
            if(sequence == null)
            {
                // try one more time to find the sequence
                sequence = StopImpl.findSequence(tt.getRouteStops(), sId, 111);
            }
                        
            // have to put this stop id into all specified directions and service keys
            for(DirType dir : dirsArray)
            {
                for(KeyType key : keysArray)
                {
                    TimePoints addMe = new TimePoints(tt.getAgencyName(), tt.getRouteID(), dir.toString(), key.toString(), tt.getLanguage());
                    addMe.setStopID(stopId);
                    addMe.setSequence(sequence);
                    addMe.fixLanguage();
                    retVal.add(addMe);
                }
            }
        }

        return retVal;
    }    
    

    public static List<Stop> getTimePoints(TimesTable tt, ScheduleDataQuery query)
    {
        return getTimePoints(tt, query, false);
    }
    
    /**
     * this method will query the timepoints from data store for the default timepoints, then apply 
     * the configured timepoints to build the set of timepoints based on the configuration.
     * 
     * if the default timepoints lack a timepoint that is specified in the configuration, then this
     * routine will build a new timepoint (Column object) for the missing stop.  
     * 
     * NOTE: allowPlaceNameReplacment should only be *true* when dealing with data that has
     *       Place Name mappings to StopIDs (eg: for TriMet, this is early schedule 
     *       data that is output directly out of Hastus just after blocking is done).
     * 
     * @param m_tt
     * @param query
     * @return List<Column> 
     */
    public static List<Stop> getTimePoints(TimesTable tt, ScheduleDataQuery query, boolean allowPlaceNameReplacment)
    {
        if(tt == null || query == null) return null; 
        
        // TODO: Improve Performance Here
        // step 1: as a default, get all the time points for this route
        List<Stop> retVal = query.queryTimePoints(tt.getAgencyName(), tt.getRouteID(), tt.getDir(), tt.getKey(), tt.getActiveServiceKeys(), tt.bypassConfig(), tt.getDate()); 
        
        if(tt.getTimeTable() == null || tt.getConfiguration() == null || tt.bypassConfig()) 
        {
            return retVal;
        }

        // step 2: a last filter to map any StopIDs / PlaceNames correctly, given the RenameTimePoint config
        if(allowPlaceNameReplacment)
            RenameTimePoint.checkPlaceNames(tt, query, retVal);

        // step 3: if the list of stops has been configured, filter the all time points list down to match
        List<TimePoints> tpList = tt.getConfiguration().findAllData(new TimePoints(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage()));
        if(tpList != null && tpList.size() > 0)
        {
            List<Stop> newList  = new ArrayList<Stop>();
            List<Stop> stopList = retVal;
            retVal = newList;      // return is our new list  
            
            int i = 0;
            for(TimePoints tp : tpList)
            {
                if(tp == null) continue;
                
                // step A: More place ID BS...want to get stop id & place id populated  
                String stopID  = tp.getStopID();
                String placeID = stopID;
                if(allowPlaceNameReplacment)
                {
                    placeID = tp.getPlaceId(tt);
                    if(placeID == null) placeID = stopID;
                    if(stopID  == null) stopID  = placeID;
                }
                if(stopID == null) continue; 

                // step B: find the matching column in the allTimePoints list
                Stop c = StopImpl.findTimePointStatic(stopList, placeID, i++);
                if(c == null)
                {
                    // step B': maybe the configured sequence is wrong, so let's look from the start of the list
                    c = StopImpl.findTimePointStatic(stopList, placeID, 0);
                }
                if(c == null && stopID.compareToIgnoreCase(placeID) != 0)
                {
                    // step B'': maybe the placeID / stopID crap is getting in the way...
                    c = StopImpl.findTimePointStatic(stopList, stopID, 0);
                }
                
                // step C: if this stop was not in the allList (it can happen -- eg: loop/combo routes), then let's create a new column
                if(c == null)
                {
                    c  = query.makeStop(tt.getConfiguration(), tt.getAgencyName(), stopID, placeID, tp.getSequence());
                    if(c == null) continue;
                    
                    // NOTE: added after the changes to HASTUS_PLACE_LOCATION table....
                    // a bit of indirection to make sure we have a correct place name from this created stop
                    // problem is with query.makeStop() is that it maps to a place name from the hash, which might be different than that from RenameTimepont.
                    if(allowPlaceNameReplacment)
                    {
                        String renamedPlace = RenameTimePoint.findOriginalStopID(tt, c.getStopId());
                        if(renamedPlace != null)
                        {
                            c.setPlaceId(renamedPlace);
                        }
                    }
                }

                // step D: if there's name in the CSV, use it to override the stop name from the DB
                if(tp.getName() != null && tp.getName().length() > 0)
                {
                    c.setDescription(tp.getName());
                }
                
                // step E: override the DB sequence with any configured sequence
                c.setSequence(tp.getSequence());

                retVal.add(c);
            }
        }
        
        // step 4: checks this list of stops for repeated stopids (loops), and marks those stops
        StopImpl.processStopRepeats(retVal);

        return retVal;
    }
    
    
    /**
     * comparitor interface:  Remember, Configure (and thus TimePoints) implements the
     *       Comparitor interface.  Thus, the Configure structures are sorted.  And the
     *       TimePoints structure has an additional sort parameter, being the sequence.
     * 
     * NOTE: DO NOT DELETE ME...I'm VERY IMPORTANT.   
     * 
     */ 
    public int compare(Configure inA, Configure inB)
    {
        int diff;
        
        // make sure this is a TimePoint compare
        if(inA == null) return -1;
        if(inB == null) return  1;
        
        // sort by route firs, as they are often unequal        
        if(inA.getRouteID() != null && inB.getRouteID() != null) 
        {
            diff = inA.getRouteID().compareTo(inB.getRouteID());
            if(diff != 0) return diff; 
        }
        
        // sort by direction, as the directions are uneqal
        if(inA.getDir() != null && inB.getDir() != null) 
        {
            diff = inA.getDir().compareTo(inB.getDir());
            if(diff != 0) return diff; 
        }
        
        // at this point, make sure we're dealing with TimePoints
        if(inA instanceof TimePoints == false) return super.compare(inA, inB);
        if(inB instanceof TimePoints == false) return super.compare(inA, inB);        
        
        Integer sA = ((TimePoints)inA).getSequence();
        Integer sB = ((TimePoints)inB).getSequence();
        
        if(sA == null) return  -1;
        if(sB == null) return   1;        
        if(sA == sB)   return   0;
        if(sA >  sB)   return   1;
        else           return  -1;
    }
    
    
    public static void sort(List<Configure> tp)
    {
        Collections.sort(tp, new TimePoints());
    }


    public static void sort(TimesTable tt)
    {
        if(tt == null || tt.getConfiguration() == null) return;
        
        List l = tt.getConfiguration().getData(TimePoints.class);
        if(l != null) 
        {
            sort(l);            
        }
    }
}    

