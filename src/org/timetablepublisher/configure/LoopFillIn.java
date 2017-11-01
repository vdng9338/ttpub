/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.CellImpl;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.RowImpl;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.IntUtils;

/**
 * The purpose of LoopFillIn is a Configuration which uses the times from the reverse
 * direction of a route to 'fill in' missing stop times in the current direction.  EG, 
 * TriMet's schedule often has the bus following a loop pattern near the end of the outbound
 * direction of the route.  Rather than coding both inbound and oubbound times for these
 * stops within the loop, the schedule usually contains only a single time in either 
 * route direction.  The job of this Configuration is to query the loop stop times in 
 * an opposite direction, and complete the time table for this direction.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class LoopFillIn extends Configure
{
    private static final Logger LOGGER = Logger.getLogger(LoopFillIn.class.getCanonicalName()); 
    public enum LoopFillInRule 
    {
        LEFT, RIGHT;
        
        public static LoopFillInRule construct(String rule)
        {
            LoopFillInRule retVal = LEFT;
            try
            {
                if(rule != null && rule.length() > 0)
                {
                    retVal = LoopFillInRule.valueOf(rule);
                }
            }
            catch (RuntimeException e)
            {
                LOGGER.log(DEBUG, "enum error for string " + rule, e);
                retVal = LoopFillInRule.LEFT;
            }

            return retVal;
        }
    }

    @CsvColumn(index=6, name="Rule: LEFT, RIGHT", details="loop fill-in queries the opposite direction, looking for stop times to fill empty cells -- the LEFT or RIGHT is the position, relative to the StopID, of the stops in the schedule needing fill-in.")
    public LoopFillInRule m_rule;
    
    @CsvColumn(index=7, name="Stop ID", details="the stop (in the existing table) just prior to the stops you need fill-in times for.")
    public String  m_stopID;
    
    @CsvColumn(index=8, name="Trip Length", details="Time (in minutes) of a trip -- helps to fit the fill-in times, where any times that exceed this buffer are not considered.")
    public int m_tripLength;
    
    
    public LoopFillIn()
    {        
    }
    public LoopFillIn(String agency, String route, String dir, String key)
    {
        super(agency, route, dir, key);
    }
    
    public boolean fromRight()
    {        
        return getRule() == LoopFillInRule.RIGHT; 
    }

    public int getTripLength()
    {
        return m_tripLength * 60;  // convert time in minutes to seconds
    }
    public String getStopID()
    {
        return m_stopID;
    }
    public LoopFillInRule getRule()
    {
        return m_rule;
    }
    public List<String> getStopIDs()
    {
        return split(m_stopID);
    }
    
        
    public void setStopID(String stopID)
    {
        if(stopID == null) return;
        m_stopID = stopID.trim();
    }
    public void setRule(String rule)
    {
        m_rule = LoopFillInRule.construct(rule);
    }
    public void setTripLength(String len)
    {
        m_tripLength = IntUtils.getIntegerFromString(len, 30);
    }
    
    
    //
    // Operations on TimesTables
    //
    public static LoopFillIn instantiate(TimesTable tt)
    {   
        if(tt == null || tt.getConfiguration() == null) return null;
        
        return (LoopFillIn)tt.getConfiguration().findData(new LoopFillIn(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString()));
    }
    public static boolean fromRight(TimesTable tt)
    {   
        if(tt == null || tt.getConfiguration() == null) return false;
        
        LoopFillIn lr = (LoopFillIn)tt.getConfiguration().findData(new LoopFillIn(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString()));
        if(lr == null) return false;
        
        return (lr.getRule() == LoopFillIn.LoopFillInRule.RIGHT); 
    }
    

    /**
     * Process the Loop Fill In
     *  
     * step 1: query in opposite direction
     * step 2: from unorganized list of times, get a set of times making up a trip (contiguous set of times with same trip number)
     * step 3: ask the time-table (Row) where this trip best fits (are they actually a prior trip not in table?) -- return Row Index
     *         - get first time from fill-in trip
     *         - loop through times-table, looking ahead one row ... finding the best fit
     *         - get next time from fill-in trip 
     *         - do the same thing -- is this better?
     * 
     * step 4: insert this trip into the 'found' row
     * step 5: repeat    
     */ 
    
    synchronized public static void process(TimesTable tt, ScheduleDataQuery query)
    {
        if(query == null || !okToProcessHasTimeTable(tt, Configure.class)) return;
        
        // is this a loop route ???
        LoopFillIn loop = instantiate(tt);
        if(loop != null)
        {
            // if loop route, do a query in the opposite direction
            String stop = loop.getStopID(); 
            List<Stop> stops = StopImpl.findColumns(tt.getTimePoints(), stop, loop.fromRight(), true);

            // condition where stops have some, but not all PlaceIDs (different from StopIDs), 
            // so look in reverse (loop) direction for those PlaceIDs 
            if(StopImpl.hasMixedPlaceIDs(stops))
            {
                List flist = StopImpl.findStopsWithEqualIDs(stops);
                List<Stop> fixer = StopImpl.getTimePointsInOppositeRouteDirection(tt, query);
                StopImpl.fixUpStops(flist, fixer);
            }


            List<Cell> times = query.getTimesByRoute(stops, tt.getAgencyName(), tt.getRouteID(), tt.getDir().getOpposite(), tt.getKey(), tt.getActiveServiceKeys(), tt.bypassConfig(), tt.getDate());
            SvcKeyNormalizer skn = new SvcKeyNormalizer(tt);
            skn.normalizeServiceKey(times);
            fillInTimes(loop, skn, stops, times, tt);
        }
    }
    
    /**
     * Kind of confusing, but here goes...
     * 1.  We're given a list of stops, and a list of stop times for those stops as input.
     * 2.  We'll work one stop at a time, working from the middle of the table, filling times outward to an end.
     * 3.  We'll loop though all of the stops times from the fill-in (other direction's times)
     * 4.  We'll loop through all of the trips in the existing row.
     * 5.  We're looking for interesting candidate fill-in times that best match our trip
     * 6.  And we'll look at all candidate times to find the loop fill-in time that best fits our table
     * 
     * BTW. It seems a bit backwards to loop through the times first, and then the rows of the table, but it's not.
     *      It makes sense, since if we did the opposite (row as outer loop, time as inner), we're going to be putting
     *      times that 
     */
    //
    // 1. Don't do the isProcessed and not null checks above...rather, keep checking times and all rows: 
    //    -- the way the algo is written now, we may still be choosing the wrong time for a row, since we're only 
    //       looking at that early fill-in time.  maybe the next time in our list would be better...but since we've
    //       already set and processed this row spot with the earlier time, it's frozen.
    //    -- we shouldn't have an issue here, since we're using the 'MAX TIME' parameter in the Config, and thus we'd
    //       assume that each trip won't be completed within that time frame, and thus we won't have times that
    //       would screw
    //    
    public static void fillInTimes(LoopFillIn loop, SvcKeyNormalizer skn, List<Stop> stops, List<Cell> times, TimesTable tt)
    {
        // step 1: inputs -- check to make sure we got them all
        if(loop == null || stops == null || stops.size() < 1 || times == null || times.size() < 1 || tt == null)
        {
            LOGGER.log(SEVERE, "NULL parameter in call to fillInTimes...exiting method");
            return;
        }
               
        // want to work with interior columns of data first, and work our way out
        if(!loop.fromRight())
        {
            Collections.reverse(stops);
        }



        // step 2: treat each stop differently, working outward to an end/beginning
        for(Stop fs : stops)
        {
            Integer columnIndex = StopImpl.findColumnIndex(tt.getTimePoints(), fs.getPlaceId(), loop.fromRight());           
            if(columnIndex == null) continue;

            List<Cell> stopTimes = CellImpl.findStopTimes(times, fs.getPlaceId());
            if(stopTimes == null) continue;

            // init: get a list of the rows that have a null entry in this column (eg: won't consider a fill-in if the stop already has a time)
            List<Row> rows = RowImpl.findRowsWithEmptyColumn(tt.getTimeTable(), columnIndex);
            if(rows == null || rows.size() < 1) continue;   // if there are no empty rows in this stop, then we can move on to the next stop
            
            // init: clear out processed status flag (it's used below)
            fillIn(skn, stopTimes, rows, null, columnIndex, loop.getTripLength(), loop.fromRight());
        } // end Columns for loop        
    }
    
    
    /**
     * This method is the meat of the fillIn process (seperated here, so it can be re-used -- eg: InterliningNotes) 
     * 
     * @param stopTimes
     * @param rows
     * @param columnIndex
     * @param tripLength
     * @param fromRight
     */
    public static int fillIn(SvcKeyNormalizer skn, List<Cell> stopTimes, List<Row> rows, String fnSymbol, Integer columnIndex, Integer tripLength, boolean fromRight)
    {
        if(stopTimes == null || rows == null) return 0;
        
        int numInterlines = 0;
        
        // step 3 (continued from fillInTimes above): loop through the fill-in stops times
        RowImpl.clearRowProcessedFlagStatic(rows);
        for(Cell c : stopTimes)
        {
            if(c == null || c.getBlock() == null) continue;

            // step 4: now loop through the rows of the table, trying to find the best row for this time
            String  cellKey = c.getRawSvcKey();
            Row tRow = null;
            int comp = 111111111; 
            for(Row r : rows)
            {
                if(r == null || r.getBlock() == null) continue;    
                if(r.isProcessed())                   continue;  // we've already added a time for this row, so don't include it
                if(r.getCell(columnIndex) != null)    continue;  // don't overwrite existing times with fill-in times 

                // step 5: make sure the time in question is from the same BLOCK (eg: same vechicle)
                if(r.getBlock().equals(c.getBlock()))
                {
                    // make sure that the rawServiceKeys (if provided) also match up 
                    String rowKey = r.getRawSvcKey();
                    if(!SvcKeyNormalizer.isEqual(cellKey, rowKey))
                    {                        
                        if(!skn.isRelatedAndNotSiblings(cellKey, rowKey)) 
                            continue;
                        
                        c.setRawSvcKey(rowKey);
                    }
                    

                    // step 5a: find closest time in the existing table to the column we're looking at
                    //          and calculate a difference between that time and our fill-in candidate time
                    Integer  thisDiff;
                    if(fromRight)
                    {
                        thisDiff = fromRight(c, r, columnIndex);
                        if(thisDiff == null) {
                            thisDiff = fromLeft(c, r, columnIndex);
                        }                            
                    }
                    else
                    {
                        thisDiff = fromLeft(c, r, columnIndex);
                        if(thisDiff == null) {
                            thisDiff = fromRight(c, r, columnIndex);
                        }                            
                    }
                                            
                    // step 5b: the time difference has to be positive (eg: in position), and within our configured 'trip length'
                    if(thisDiff == null || tripLength == null) continue;
                    if(thisDiff < 0 || thisDiff > tripLength)  continue;

                    // step 6: OK, we have a candidate fill-in that looks interesting...now we'll compare it with any other interesting
                    //         candidate fill-in times to see which candidate time is the best match for this trip
                    if(comp > thisDiff)
                    {
                        comp = thisDiff;
                        tRow = r;                                
                    }
                    else
                    {
                        // stopping condition: the comp is now smaller than the diff, and since (it's assumed)
                        // that trips are sorted by time, this diff will only get larger...so this is the proper trip
                        break;
                    }
                }
            } // end Rows for loop
            
            if(tRow != null)
            {
                tRow.setCell(columnIndex, c);
                tRow.setProcessed(true);
                numInterlines++;
                if(fnSymbol != null)
                {
                    // set footnote
                    c.setFootnoteSymbol(fnSymbol);
                }
            }
        } // end Cells for loop
        
        return numInterlines;
    }
    
    public static Integer fromRight(Cell c, Row r, Integer columnIndex)
    {
        if(c == null || r == null || columnIndex == null) return null;
        
        Integer retVal = null;        
        Cell tmp = r.getPrevNonNullCell(columnIndex);
        if(tmp != null) 
        {
            retVal = c.getTime() - tmp.getTime();
        }
        
        return retVal;        
    }
    public static Integer fromLeft(Cell c, Row r, Integer columnIndex)
    {
        if(c == null || r == null || columnIndex == null) return null;
        
        Integer retVal = null;        
        Cell tmp = r.getNextNonNullCell(columnIndex);
        if(tmp != null) 
        {
            retVal = tmp.getTime() - c.getTime();
        }
        return retVal;        
    }
}    
