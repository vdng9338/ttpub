/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.ArrayList;
import java.util.List;

import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.schedule.TtTrip;
import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.Footnote;
import org.timetablepublisher.table.FootnoteImpl;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.RowImpl;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.utils.IntUtils;

/**
 * The purpose of InterliningNotes is to either mark and/or append trips to a timetable
 * based on an Agency's inter-lining rules (eg: Interlining is when a single vechicle 
 * runs on multiple bus routes in sequence in a given service day).  Other route data
 * can be appended if the interlining rules are satisfied in this configuration.
 * 
 * NOTE: the current implementation reflects the rules at TriMet, where the block number 
 * (which at TriMet is a unique number on any given service key) is used to identify
 * a particular vechicle operating between multiple routes.  
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class InterliningNotes extends Configure
{
    public enum InterliningRule 
    {
        APPEND_TRIP, MERGE_TIMES, ORIGINATES, CONTINUES, UNDEFINED;
        
        public static InterliningRule construct(String str)
        {
            InterliningRule retVal;
            try
            {
                if(str != null && str.length() > 1)
                {
                     retVal = InterliningRule.valueOf(str);
                }
                else
                {
                    retVal = InterliningRule.UNDEFINED;
                }
            }
            catch (RuntimeException e)
            {
                e.printStackTrace();
                retVal = InterliningRule.UNDEFINED;
            }  
            
            return retVal;
        }
    }
        
    @CsvColumn(index=6, name="Rule: APPEND_TRIP, MERGE_TIMES, ORIGINATES, CONTINUES", details="rules to govern interlining -- append will go out to another schedule and add data to the table")
    public InterliningRule m_rule;
    
    @CsvColumn(index=7, name="Interlining Route", details="the routeID of the other line that is being compared to the blocks in this route")
    public String m_intRoute;
    
    @CsvColumn(index=8, name="Interlining Route's Direction", details="the direction of travel of the interlining route (which may / may-not be different from the time-table direction)")
    public DirType m_intDir;

    @CsvColumn(index=9, name="Minimum Number of Stops", details="regarding APPEND_TRIP: only append an interlined trip when N or more stops are in that trip.")
    public int m_minNumStops;

    @CsvColumn(index=10, name="Stop ID", details="regarding ORIGINATE & CONTINUE: this is a stop common to both routes")
    public String m_stopID;
    
    @CsvColumn(index=11, name="Minutes Between Trips", details="a buffer of time (in minutes), that helps to find the best fitting blocks in the compare.")
    public int m_time;
    
    @CsvColumn(index=12, name="FN Symbol", details="footnote symbol -- to be added to the time-table")
    public String m_symbol;
    
    @CsvColumn(index=13, name="Footnote (FN)", details="customer friendly information explaining how / where the routes interline.  NOTE: you can use the following macros: STOP.NAME, STOP.ID, STOP.TIMES, need to put more here, and better document")
    public String m_footNote;
    
    @CsvColumn(index=14, name="FN Sequence", details="the placement of the footnote, in relation to other footnotes (smaller is higher)")
    public int m_sequence;
    

    public InterliningNotes()
    {        
    }

    public InterliningNotes(String agency, String route, String dir, String key, String lang)
    {
        super(agency, route, dir, key, lang);
    }

    public String getFootNote() 
    {
        return m_footNote;
    }

    public Integer getSequence() 
    {
        return m_sequence;
    }

    public String getSymbol()
    {
         return m_symbol;   
    }
    
    public String getStopID()
    {
        return m_stopID;
    }

    public DirType getIntDir()
    {
        return m_intDir;
    }

    public String getIntRoute()
    {
        return m_intRoute;
    }

    public InterliningRule getRule()
    {
        return m_rule;
    }

    public Integer getMinNumStops()
    {
        return m_minNumStops;
    }

    public int getTime()
    {
        return m_time * 60;  // convert minutes to seconds
    }

    // STRING-BASED SETTERS -- needed for the editor tool
    public void setFootNote(String footNote)
    {
        if(footNote == null) return;
        m_footNote = footNote.trim();
    }

    public void setIntDir(String intDir)
    {
        m_intDir = DirType.construct(intDir);
    }

    public void setIntRoute(String intRoute)
    {
        if(intRoute == null) return;
        m_intRoute = intRoute.trim();
    }

    public void setMinNumStops(String minNumStops)
    {
        m_minNumStops = IntUtils.getIntegerFromString(minNumStops, 0);
    }

    public void setRule(String rule)
    {
        m_rule = InterliningRule.construct(rule);
    }

    public void setSequence(String sequence)
    {
        m_sequence = IntUtils.getIntegerFromString(sequence, 30);
    }

    public void setStopID(String stopID)
    {
        if(stopID == null) return;
        m_stopID = stopID.trim();
    }

    public void setSymbol(String symbol)
    {
        if(symbol == null) return;
        m_symbol = symbol.trim();
    }

    public void setTime(String time)
    {
        m_time = IntUtils.getIntegerFromString(time, 0);
    }
    
    
    
    /**
     * Interlining notes are for when a route needs to get data from another line.  For example,
     * the MAX Yellow line has a handful of trips that travel on MAX Blue line tracks.  The stop
     * times (Blue stops) for these yellow line trips are shown on the MAX Blue timetable.  The
     * Interlining configuration will pull that data from MAX Yellow, append the trips into the
     * Blue schedule, and footnote these trips as specified.      
     * 
     * @param m_tt
     * @return
     */
    synchronized public static List<Footnote> process(TimesTable tt, ScheduleDataQuery query)
    {
        if(query == null || !okToProcessHasTimeTable(tt, Configure.class)) return null;

        InterliningNotes index = new InterliningNotes(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage());
        List inList = tt.getConfiguration().findAllData(index);
        if(inList == null || inList.size() < 1) return null;
        
        List<Footnote> retVal = new ArrayList<Footnote>();
        for(InterliningNotes in : (List<InterliningNotes>) inList)
        {
            if(in == null || in.getIntRoute() == null) continue; 
    
            DirType intDir  = in.getIntDir() != null ? in.getIntDir() : tt.getDir();
            String intRoute = in.getIntRoute(); 
            
            int numNotes = 0;
            if(in.getRule() == InterliningRule.APPEND_TRIP)                 
            {
                List<Row> rows = query.getSchedule(tt.getTimePoints(), tt.getAgencyName(), intRoute, intDir, tt.getKey(), tt.getActiveServiceKeys(), tt.bypassConfig(), tt.getDate());
                
                // add the footnote symbol to all of these trips
                if(!IntUtils.isEmpty(in.getSymbol()))
                    RowImpl.setFootnoteSymbol(rows, in.getSymbol());
                
                // get a place id from any configured stopid
                String place = null;
                if(!IntUtils.isEmpty(in.getStopID()))
                {
                    place = query.toPlaceId(tt.getConfiguration(), tt.getAgencyName(), in.getStopID());
                    if(place == null)
                    {
                        place = in.getStopID();
                    }
                }
                numNotes = RowImpl.appendTripsToTable(tt, rows, in.getMinNumStops(), place);
            }  
            else if(in.getRule() == InterliningRule.MERGE_TIMES)
            {
                // going to pull times from the interline route, and stick them in here
                String stopID = in.getStopID();
                if(stopID != null && stopID.length() > 0)
                {
                    SvcKeyNormalizer skn = new SvcKeyNormalizer(tt);  
                    if(stopID.equals("*"))
                    {
                        for(Stop s : tt.getTimePoints())
                        {
                            numNotes += mergeTimes(tt, skn, s.getStopId(), intRoute, intDir, query, in);
                        }
                    }
                    else
                    {
                        numNotes += mergeTimes(tt, skn, stopID, intRoute, intDir, query, in);
                    }
                }
                
            }
            else if(in.getRule() == InterliningRule.ORIGINATES || in.getRule() == InterliningRule.CONTINUES)
            {
                // step 1: query the other line's trips/blocks, then compare those blocks to these...all matches get the interline note
                List<TtTrip> trips = query.getTrips(tt.getAgencyName(), intRoute, intDir, tt.getKey(), tt.getActiveServiceKeys(), tt.bypassConfig(), tt.getDate());
                
                // step 2: loop through the existing TimesTable, looking for Block matches (eg: same vechicle) in this other route
                for(Row r : tt.getTimeTable())
                {                    
                    if(r == null || r.getBlock() == null || trips == null) continue;
                    
                    String block = r.getBlock();
                    for(TtTrip t : trips)
                    {
                        if(t == null || t.getBlock() == null) continue; 
                        
                        // step 3: OK, we have a Block match, now compare the start/end times to confirm this is an interline 
                        if(block.equals(t.getBlock().toString()))
                        {                            
                            // step 3a: compare whether the start of our route, and the end of the ORIGINATING interline route match the 'configured' buffer time
                            boolean doesInterline = false;                            
                            if(in.getRule() == InterliningRule.ORIGINATES)
                            {
                                Cell first = r.getFirstNonNullCell();
                                int  eTime = t.getEndTime();
                                int  diff  = first.getTime() - eTime;
    
                                if(diff <= in.getTime())
                                {
                                    doesInterline = true;
                                }                                
                            }
                            else if(in.getRule() == InterliningRule.CONTINUES)
                            {
                                // step 3b: compare whether the end of our route, and the start of the interline route that CONTINUES on fall w/in the time buffer
                                Cell last  = r.getLastNonNullCell();                            
                                int  sTime = t.getStartTime();
                                int  diff  = sTime - last.getTime(); 
                                    
                                if(diff <= in.getTime()) 
                                {
                                    doesInterline = true;
                                }                                
                            }
    
                            // step 4: it does interline, so mark the trip
                            if(doesInterline)
                            {
                                // set footnote
                                r.setFootnoteSymbol(in.getSymbol());
                                numNotes++;
                                break; // break out of inner loop, moving to next trip
                            }
                        }
                    }
                }
            }
            
            
            // create the footnote
            if(numNotes > 0 && !IntUtils.isEmpty(in.getSymbol()) && !IntUtils.isEmpty(in.getFootNote()))  
            {                
                Footnote fn = new FootnoteImpl(in.getSymbol() + in.getSequence(), in.getSymbol(), in.getFootNote(), in.getSequence());
                retVal.add(fn);
            }
        }
        
        return retVal;
    }

    private static int mergeTimes(TimesTable tt, SvcKeyNormalizer skn, String stopID, String interlinedRoute, DirType interlinedDir, ScheduleDataQuery query, InterliningNotes in)
    {
        int retVal = 0;
        if(interlinedRoute != null && stopID != null && StopImpl.isStopInList(tt.getTimePoints(), stopID))
        {
            // init: find the column index for this stop
            Integer columnIndex = StopImpl.findColumnIndex(tt.getTimePoints(), stopID);
            if(columnIndex == null) 
                return retVal;

            // init: get a list of the rows that have a null entry in this column (eg: won't consider a fill-in if the stop already has a time)
            List<Row> rows = RowImpl.findRowsWithEmptyColumn(tt.getTimeTable(), columnIndex);
            if(rows == null || rows.size() < 1) 
                return retVal;   // NOTE: if there are no empty rows in this stop, then we can move on to the next stop
                        
            // going to query the other route for times
            List<Cell> stopTimes = query.getTimesByStopID(tt, interlinedRoute, interlinedDir, stopID);
            skn.normalizeServiceKey(stopTimes);
            
            Integer tripLength = in.getTime() > 0 ? in.getTime() : null;
            retVal = LoopFillIn.fillIn(skn, stopTimes, rows, in.getSymbol(), columnIndex, tripLength, false);                  
        }
        
        return retVal;
    }

    /**
     *  This routine will set certain InterliningNotes configurations to ingore (effectively turning them off)
     *  TODO: It's a work in progress, and needs more thought...  
     */
    public static List<Configure> pause(InterliningNotes index, ConfigurationLoader loader, List<String> routeList)
    {
        List<Configure> retVal = new ArrayList<Configure>();
        List inList = loader.findAllData(index);
        for(InterliningNotes in : (List<InterliningNotes>) inList)
        {
            if(in == null || in.getIntRoute() == null) continue; 
            String intRoute = in.getIntRoute();
            if(in.getRule() == InterliningRule.MERGE_TIMES)
            {
                for(String iRte : routeList)
                {
                    if(iRte == null) continue;
                    if(iRte.startsWith(intRoute))
                    {
                        in.setIgnore(true);
                        retVal.add(in);
                    }
                }
            }
        }

        return retVal;
    }
}

