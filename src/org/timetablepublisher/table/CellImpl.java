/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Constants;


/**
 * The purpose of CellImpl is to implement the default Cell interface.  Note that there 
 * are a lot of static methods here, which do things like find cells in a list, etc... 
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 19, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class CellImpl implements Cell
{
    private String  m_stop;
    private Integer m_time;
    private String  m_trip        = null;
    private String  m_tripType;
    private String  m_block;
    private DirType m_dir;
    private KeyType m_key;
    private String  m_rawSvcKey;
    private String  m_footnoteSymbol = "";

    private boolean m_processed   = false;
    private boolean m_highlighted = false;   

    public CellImpl(Cell s)
    {
        this(s.getStopId(), s.getTime(), s.getBlock(), s.getTrip(), s.getDir(), s.getTripType(), s.getRawSvcKey(), s.getFootnoteSymbol());
    }    
    public CellImpl(Integer stop, Integer time, Short block,   Short trip,   int dir,     Character tripType, String key)
    {
        this(stop, time, block, trip, dir, tripType != null ? tripType + "" : null, key);
    }
    public CellImpl(Integer stop, Integer time, Short block,   Short trip,   int dir,     String type,        String key)
    {
        this(stop.toString(), time, block.toString(), trip.toString(), DirType.construct(dir), type, key);
    }
    public CellImpl(String stop,  Integer time, Integer block, Integer trip, Integer dir, Character tripType, String key)
    {
        this(stop, time, block.toString(), trip.toString(), DirType.construct(dir), tripType != null ? tripType + "" : null, key); 
    }
    public CellImpl(String stop,  Integer time, String block,  String trip,  DirType dir, String tripType, String key)
    {
        this(stop, time, block, trip, dir, tripType, key, KeyType.Weekday, ""); 
    }
    public CellImpl(String stop,  Integer time, String block,  String trip,  DirType dir, String tripType, String key, String fnSymbol)
    {
        this(stop, time, block, trip, dir, tripType, key, KeyType.Weekday, fnSymbol); 
    }
    public CellImpl(String stop,  Integer time, String block,  String trip,  DirType dir, String tripType, String key, KeyType defKey)
    {
        this(stop, time, block, trip, dir, tripType, key, defKey, "");
    }
    public CellImpl(String stop,  Integer time, String block,  String trip,  DirType dir, String tripType, String key, KeyType defKey, String fnSymbol)
    {
        m_stop       = stop  != null ? stop.trim() : null;
        m_time       = time;
        m_block      = block != null ? block.trim(): null;
        m_trip       = trip  != null ? trip.trim() : null;
        m_dir        = dir;
        m_tripType   = tripType != null ? tripType.trim() : null;
        m_rawSvcKey  = key      != null ? key.trim()      : null;
        m_key        = KeyType.construct(key, defKey != null ? defKey : KeyType.Weekday);
        
        if(fnSymbol == null) fnSymbol = "";
        m_footnoteSymbol = fnSymbol;
    }    
 
    public DirType getDir()
    {
        return m_dir;
    }
    public boolean isProcessed()
    {
        return m_processed;
    }
    public KeyType getServiceKey()
    {
        return m_key;
    }
    public String getRawSvcKey()
    {
        return m_rawSvcKey;
    }    
    public String getStopId()
    {
        return m_stop;
    }
    public Integer getTime()
    {
        return m_time;
    }
    public String getTimeAsStr()
    { 
        if(m_time == null || m_time < 0) return null;
        return secondsToString( m_time );        
    }

    public String getHour()
    {
        if(m_time == null || m_time < 0) return null;
        return secondsToHour( m_time );        
    }
    public String getMinutes()
    {
        if(m_time == null || m_time < 0) return null;
        return secondsToMinutes( m_time );
    }
    public String getTensOfMinutes()
    {
        if(m_time == null || m_time < 0) return null;

        String retVal = secondsToMinutes( m_time );
        if(retVal != null && retVal.length() > 0) 
        {
            retVal = retVal.substring(0, 1);
            if(retVal == null || retVal.length() < 1) 
            {
                // this 'should' never happen, butt...
                retVal = secondsToMinutes( m_time );
            }
        }
        
        return retVal;
    }
    
    public String getBlock()
    {
        return m_block;
    }
    public String getTrip()
    {
        return m_trip;
    }
    public String getTripType()
    {
        return m_tripType;
    }
    public boolean isHighlighted()
    {
        return m_highlighted;
    }
    public void setHighlighted(boolean highlighted)
    {
        m_highlighted = highlighted;
    }
    public String getFootnoteSymbol()
    {
        return m_footnoteSymbol;
    }

    
    /**
     * Either set / append a symbol.  Note that this method will handle NULL values,
     * and thus a call with NULL will remove any / all appended FN symbol info. 
     * 
     * @see org.timetablepublisher.table.Cell#setFootnoteSymbol(java.lang.String)
     */
    public void setFootnoteSymbol(String symbol)
    {
        // only append this *new* symbol when it's not null, and there is some other symbol here already 
        if(symbol != null && m_footnoteSymbol != null && m_footnoteSymbol.length() > 0)
        {
            // only append when the current symbol does not contain the symbol
            // eg: it makes no sense to have the same cell symbol more than once
            if(!m_footnoteSymbol.contains(symbol))
                m_footnoteSymbol += symbol;
        } 
        else 
        {
            m_footnoteSymbol = symbol;   
        }
    }
    public void setDir(DirType dir)
    {
        m_dir = dir;
    }
    public void setProcessed(boolean processed)
    {
        m_processed = processed;
    }
    public void setServiceKey(KeyType key)
    {
        m_key = key;
    }
    public void setRawSvcKey(String key)
    {
        m_rawSvcKey = key;
    }        
    public void setStopId(String stop)
    {
        m_stop = stop;
    }
    public void setTime(int time)
    {
        m_time = time;
    }
    public void setBlock(String block)
    {
        m_block = block;
    }
    public void setTrip(String trip)
    {
        m_trip = trip;
    }
    public void setTripType(String tripType)
    {
        m_tripType = tripType;
    }
    
    /*******************************
    
    STATIC CELL FINDER ROUTINES
    
    TODO: could be cleaned up a bit (and some routines deleted)        
    ******************************/
    
    public static Cell findSameTrip(List<Cell> times, String trip)
    {
        return findSameTrip(times, trip, true);
    }

    public static Cell findSameTrip(List<Cell> times, String trip, boolean returnLatestTime)
    {
        if(times == null || trip == null) return null;
        
        Cell retVal = null;
        
        for(int i = 0; i < times.size(); i++)
        {
            Cell c = times.get(i);
            if(c == null || c.getTime() == null) continue;
            
            // stop on first trip match
            if(trip.equals(c.getTrip()))
            {
                // but only return a value if the time is valid (eg: might have a cell with valid trip, but no time)
                if(c.getTime() > 0) {
                    retVal = c; 
                }
                
                // if we're to return the latest time, then keep iterating over this trip, until the last stop is seen
                if(returnLatestTime)
                {
                    for(; i < times.size(); i++)
                    {
                        c = times.get(i);
                        if(c == null) continue;
                        
                        // stopping condition is when the trip changes
                        if(!trip.equals(c.getTrip())) {  
                            break;
                        }

                        // again, only assign something with a time in it
                        if(c.getTime() > 0) {
                            retVal = c; 
                        }
                    }                    
                }
                
                // break out of outer for loop once we've got a value...otherwise, keep looking
                if(retVal != null) {
                    break;
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Will look at a list of stop times (cell), and filter out all contiguous cells with the same trip number, 
     * except for the right-most trip. 
     * 
     * Purpose:     good for finding the actual departure time from a list times for a given stop.
     * Assumptions: the list is sorted by trip, then departure time.  Thus, trips are contiguous, and latter 
     *              stops times in a given trip are to the right of earlier trips in the list.
     * 
     * @return List<Cell> 
     */
    public static List<Cell> filterForRightMostStopTimes(List<Cell> input)
    {
        List<Cell> retVal = new ArrayList<Cell>();
        
        // from right means we'll CULL duplicate times, saving only a latter time.
        for(int i = 0; i < input.size() - 1; i++)
        {
            Cell a = input.get(i);
            if(a == null) continue;                


            // need trip ID...if not there, just add the cell to return and move on
            String tripA = a.getTrip();
            if(tripA == null)                    
            {
                retVal.add(a);
                continue;
            }
                

            // initial condition -- we're going to add something to the list
            Cell rightMost = a;
            int  newI = i;
            
            // find latter stop times (cells) for a given trip (eg right most stop time is often the depart time after a layover)
            for(int j = i+1; j < input.size(); j++)
            {
                Cell b = input.get(j);
                if(b == null) continue;
                
                String tripB = b.getTrip();
                if(tripB != null && tripB.equals(tripA))
                {
                    rightMost = b;
                    newI = j;
                }
                else
                {
                    break;
                }
            }
            
            // assignment
            retVal.add(rightMost);
            i = newI;
        }
        
        return retVal;        
    }
    
    public static Cell findClosest(List<Cell> times, Cell compare, boolean fromRight)
    {
        if(compare == null || compare.getTime() == null) return null;
        
        Cell retVal = null;
        
        int len = 111111111;        
        for(Cell c : times)
        {
            if(c == null || c.getTime() == null || c.getTime() <= 0) continue;
            
            if(compare.getBlock() == null || 
               c.getBlock()       == null || 
               !c.getBlock().equals(compare.getBlock())) continue;
                
            
            // is the timepoint (times variable) further along the route, and thus to the right in the table, than the compare
            int tmpLen = 0;            
            if(fromRight)
                tmpLen = c.getTime() - compare.getTime();
            else
                tmpLen = compare.getTime() - c.getTime();
            
            if(tmpLen > 0 && tmpLen < len)
            {
                // TODO: note that we may want to test if(tmpLen > 60) ... eg: greater than 1 minute between TP's                
                len = tmpLen;
                retVal = c;                    
            }
        }        
        
        return retVal;
    }
    /**
     * Converts time in seconds to a <code>String</code> in the format h:mm.
     *
     * @param time the time in seconds.
     * @return a <code>String</code> representing the time in the format h:mm
     */
    public static String secondsToString(int time)
    {
        if(time < 0) return null;

        String minutesStr = secondsToMinutes(time);
        String hoursStr   = secondsToHour(time);
        return new String(hoursStr + ":" + minutesStr);
    }
    
    public static String secondsToHour(int time)
    {
        if(time < 0) return null;        
        int hours = (time/3600) % 12;
        String hoursStr = hours == 0 ? "12" : hours + "";
        return new String(hoursStr);
    }

    
    public static String secondsToMinutes(int time)
    {
        if(time < 0) return null;
        
        int minutes = (time/60) % 60;
        String minutesStr = (minutes < 10 ? "0" : "") + minutes;
        return new String(minutesStr);
    }
    
    public static String getAmPm(int time)
    {
        return getAmPm(time, Constants.AM, Constants.PM);
    }
    public static String getAmPm(int time, String am, String pm)
    {
        if(time % 86400 >= 43200)
            return pm;
        else
            return am;
    }
    
    public static Collection<String> getTrips(List<Cell> cells)
    {
        Set<String> trips = new HashSet<String>();
        
        for(Cell c : cells)
        {
            if(c == null) continue;
            trips.add(c.getTrip());
        }        
        return trips;
    }
    
    /**
     * In a list of cells, find a contigous set of times all matching on the same TRIP
     * NOTE: this method *must* have a sorted times list to perform reliably. 
     * 
     *  
     * 
     * @param times
     * @param startIndex
     * @return
     */
    public static List<Cell> findTrip(List<Cell> times, int startIndex)
    {
        List<Cell> retVal = new ArrayList<Cell>();
        if(times == null) return retVal;
        
        String trip = null;
        for(int i = startIndex; i < times.size(); i++)
        {
            Cell c = times.get(i);
            if(c == null) continue;

            // want to match trips
            if(trip == null) {
                trip = c.getTrip();
            }
            
            // stopping condition
            if(!trip.equals(c.getTrip())) {
                break;
            }
            
            // assignment
            c.setProcessed(false);
            retVal.add(c);
        }
        
        return retVal;
    }

    /**
     * this method will return a single trip times that are sorted against the list of 
     * stops.
     *  
     * NOTE: this method *must* have a sorted times list to perform reliably.
     *  
     * @param  route stops
     * @param  time cells for a single trip
     * @return List<Cell> which is the final trip, mapped to 
     */
    public static List<Cell> mapTripTimesToStops(List<Stop> stops, List<Cell> times)
    {
        List<Cell> retVal = new ArrayList<Cell>();
        if(times == null || stops == null) return retVal;

        // step 1: aim to get a sorted trip back
        int index = 0;
        for(int i = 0; i < stops.size(); i++)
        {            
            Stop s = stops.get(i);
            retVal.add(i, null);  // create cell for output trip
            if(s == null || s.getPlaceId() == null) continue;
            
            s.setProcessed(false);
            boolean cellsSkipped = false;
            for(int j = index; j < times.size(); j++)
            {
                Cell c = times.get(j);
                if(c == null || c.isProcessed()) continue;
                if(c.getStopId().compareToIgnoreCase(s.getPlaceId()) == 0)
                {
                    // if earlier times are being skipped, and this same stop is listed later in the timetable, then let's skip this
                    if(cellsSkipped)
                    {
                       // by breaking here, we're going to process this cell again later 
                       Integer n = StopImpl.findColumnIndex(stops, s.getPlaceId(), true);
                       if(n != null && n > i) {
                           break;
                       }
                    }
                    
                    c.setProcessed(true);
                    s.setProcessed(true);
                    retVal.set(i, c);
                    index = j + 1;
                    break;
                }
                cellsSkipped = true;
            }
        }

        
        // step 2: fill in any times we'd missed above...not
        for(int i = 0; i < stops.size(); i++)
        {            
            if(retVal.get(i) != null) continue;
            
            Stop s = stops.get(i);
            if(s == null || s.getPlaceId() == null) continue;
            
            for(int j = 0; j < times.size(); j++)
            {
                Cell c = times.get(j);
                if(c == null || c.isProcessed()) continue;
                if(c.getStopId().compareToIgnoreCase(s.getPlaceId()) == 0)
                {
                    c.setProcessed(true);
                    retVal.set(i, c);
                    break;
                }
            }
        }
        
        // step 3: cull cells that don't fit the time sequence of the table      
        cullCellsOutOfSequence(stops, retVal);
        
        return retVal;
    }

    /**
     * cull (stop times) where the time does not fit the sequence of the other cells in the table
     * NOTE: this only takes effect on stops that are 'repeated' multiple times (loops) in the table.
     *       the routine StopImpl.processStopRepeats() must be run prior to this routinte to mark
     *       the stops that do repeat in the table.
     */
    public static void cullCellsOutOfSequence(List<Stop> stops, List<Cell> cells)
    {
        for(int i = 0; i < stops.size(); i++)
        {      
            Cell c = cells.get(i);
            if(c == null) continue;
            
            Stop s = stops.get(i);
            if(s == null || !s.isRepeated() || s.getPlaceId() == null) continue;

            // find the next (prev) cell, and compare times to see if it fits the sequence
            Cell n = findNextCell(cells, i+1);
            if(n != null)
            {
                int cT = c.getTime();
                int nT = n.getTime();

                // TIME in this Cell doesn't match the sequence, so eliminate it 
                if(cT > nT)
                    cells.set(i, null);                
            }
            else
            {
                // we're out of next cells, so look at a previous cell
                // in this case, the time of this cell should be greater than
                // the previous cell time
                n = findPrevCell(cells, i-1);
                if(n != null)
                {
                    int cT = c.getTime();
                    int nT = n.getTime();

                    // TIME in this Cell position doesn't match the sequence, so eliminate it 
                    if(cT < nT)
                        cells.set(i, null);                
                }
            }
        }
    }
    
    
    public static Cell findNextCell(List<Cell> times, int startIndex)
    {        
        Cell retVal = null;

        if(times != null)
        for(int i = startIndex; i < times.size(); i++)
        {
            Cell c = times.get(i);
            if(c != null)
            {
                retVal = c;
                break;
            }
        }
        
        return retVal;
    }

    public static Cell findPrevCell(List<Cell> times, int startIndex)
    {        
        Cell retVal = null;

        if(startIndex >= times.size())
            startIndex = times.size() - 1;
            
        if(times != null)
        for(int i = startIndex; i >= 0; i--)
        {
            Cell c = times.get(i);
            if(c != null)
            {
                retVal = c;
                break;
            }
        }
        
        return retVal;
    }
    
    /**
     * Find all stop times by way of the stop id
     * 
     * @param times
     * @param startIndex
     * @return
     */
    public static List<Cell> findStopTimes(List<Cell> times, String stopId)
    {
        List<Cell> retVal = new ArrayList<Cell>();
        if(times  == null) return retVal;
        if(stopId == null)
        {
            retVal.addAll(times);
            return retVal;
        }
        
        for(Cell c : times)
        {
            if(c == null) continue;
            
            // filter -- only add cells that match the stop ID
            if(stopId.compareToIgnoreCase(c.getStopId()) == 0) 
            {
                retVal.add(c);
            }
        }
        
        return retVal;
    }
    
    public static void clearProcessedFlag(List<Cell> cells)
    {
        try
        {
            for(Cell c : cells)
            {
                if(c != null)
                    c.setProcessed(false);
            }
        }
        catch(Exception e)
        {
        }
    }
    
    /**
     * Goal...find all the stoptimes in a trip with the same stop, but with different service keys (eg: F and f ... Mon-Thur and Friday),
     * and normalize this trip to the normal-key (eg: Weekday)
     *   
     * NOTE: this method will only work 'well' when the trip has an equal number of cells assigned to key1 and key2 (and it will not detect
     *       when this is not the case).  So if you have two trips (with the same trip number) that are indeed different in some way, and the
     *       key is meant to differentiate, then this routine will probably screw that data up more than clear it up.  This routine is designed
     *       around a specific TriMet TRANS issue with stop times for MAX Blue.  It may not work well in other circumstances
     *   
     * @param trip
     * @param raw service key1 (eg: F)
     * @param raw service key2 (eg: f)
     * @param normal key       (eg: W)
     * @return same trip you input...nothing allocated...but data within the list might be changed by this routine. 
     */
    public static List<Cell> nomalizeServiceKey(List<Cell> trip, String baseKey, String key1, String key2)
    {
        if(trip == null || baseKey == null || key1 == null || key2 == null) return null;
        if(!containsKey(trip, key1)) return null;
        
        List<Cell> retVal = trip;
        for(int i = 0; i < trip.size() - 1; i+=2)
        {
            Cell cellA = trip.get(i);
            Cell cellB = trip.get(i+1);            
            if(cellA == null || cellB == null) continue;
            
            // do we have times for the same stop?
            String stopA = cellA.getStopId();
            String stopB = cellB.getStopId();
            if(stopA == null || stopB == null) continue;
            if(!stopA.equals(stopB))           continue;
                        
            // and do we have times for key1 or key2?
            String keyA = cellA.getRawSvcKey();
            String keyB = cellB.getRawSvcKey();
            if(keyA == null || keyB == null)             continue;
            if(!keyA.equals(key1) && !keyA.equals(key2)) continue;
            if(!keyB.equals(key1) && !keyB.equals(key2)) continue;            
            
            // and do we the keys not match (hence, keyA = [Ff] and keyB = [fF], or mutually exclusive)
            if(!keyA.equals(keyB))
            {
                // if we get here, we know that the two cells are for the same stop, with different 'target' service keys
                // so let's normalize them...
                cellA.setRawSvcKey(baseKey);
                cellB.setRawSvcKey(baseKey);
            }
        }
        
        return retVal;
    }

    
    public static boolean containsKey(List<Cell> tripList, String key)
    {
        if(tripList == null || key == null) return false;        
        
        for(Cell c : tripList)
        {
            if(c == null) continue;
            if(key.equals(c.getRawSvcKey()))
                return true;
        }

        return false;
    }
    /**
     * filterByTime will cull all cells that have a stop time between the start & end times
     *
     * @param cellList
     * @param start time in seconds past midnight
     * @param end time in seconds past midnight
     * @return List<Cell>
     * @date Apr 10, 2007
     */
    public static List<Cell> filterByTime(List<Cell> cellList, int start, int end)
    {
        List<Cell> retVal = cellList;
        if(cellList != null && cellList.size() > 0)
        {
            retVal = new ArrayList<Cell>();
            for(Cell c : cellList)
            {
                if(c == null || c.getTime() == null) continue;
                if(c.getTime() <= start || c.getTime() >= end) 
                {
                    retVal.add(c);
                }
            }
        }

        return retVal;
    }
    
    public static List<Cell> filterByTime(List<Cell> cells)
    {
        final int SEVEN_AM = 60 * 60 * 7;
        final int NINE_PM  = 60 * 60 * 21;        
        return filterByTime(cells, SEVEN_AM, NINE_PM);
    }    
}
