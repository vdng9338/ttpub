/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.timetablepublisher.configure.CullTrips;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.IntUtils;

/**
 * this class is used to represent a single row in a table
 * 
 * <pre>
 * </pre>
 *
 * @see     
 * @version 1.0  June 1, 2006
 * @author  Frank Purcell (purcellf@trimet.org)
 */
public class RowImpl implements Row, Constants
{
    private static final Logger LOGGER = Logger.getLogger(RowImpl.class.getCanonicalName());
    
    protected List<Cell>    m_row = null;
    protected final String  m_route;
    protected final DirType m_direction;    
    protected final String  m_date;
    protected String        m_trip        = null;
    protected String        m_block       = null;
    protected String        m_tripType    = null;
    protected String        m_rawSvcKey   = null;    
    protected boolean       m_isProcessed = false;

    public RowImpl(String route, DirType dir, String date, int numTimePoints)
    {
        m_route     = route;
        m_direction = dir;
        m_date      = date;
        
        m_row = new ArrayList<Cell>();
        for(int i = 0; i < numTimePoints; i++)
            m_row.add(null);
    }
    
    public RowImpl(TimesTable tt)
    {
        this(tt.getRouteID(), tt.getDir(), tt.getDate(), tt.getTimePoints().size());   
    }

    public boolean isProcessed()
    {
        return m_isProcessed;
    }

    public void setProcessed(boolean isProcessed)
    {
        m_isProcessed = isProcessed;
    }
    
    public int getLen()
    {
        return m_row.size();
    }
    public String getDate()
    {
        return m_date;
    }
    public Cell removeCell(int index)
    {
        if(index < 0 || index >= m_row.size()) return null;
        
        Cell retVal = m_row.remove(index);
        return retVal;
    }

    public void setCell(int i, Cell input)
    {        
        try
        {
            m_row.set(i, input);     
        }
        catch(Exception e)
        {
            LOGGER.log(DEBUG, "setCol error");
        }
    }

    public boolean updateCell(int i, Cell input, boolean overwrite)
    {
        boolean updateMade = false;
        
        // if you've got a valid stop (with a valid stop time in it)
        if(input != null && input.getTime() >= 0 && i >= 0 && i < m_row.size())
        {
            // and if were either forcing an overwrite, or the existing cell time
            // is blank, set this cell to new stop time...
            if(overwrite || getTime(i) < 0)
            {
                setCell(i, input);
                updateMade = true;
            }
        }
        
        return updateMade;
    }

    public boolean addSetCell(int i, Cell input)
    {
        boolean retVal = true;
        try
        {
            m_row.set(i, input);     
        }
        catch(Exception e)
        {
            // try adding the cell, rather than setting
            try
            {
                m_row.add(i, input);
            }
            catch(Exception f) 
            {
                retVal = false;
            }
        }
        
        return retVal;        
    }
    
    
    /**
     * addUpdateCell
     *
     * @return boolean indicating whether an update was made
     * @date May 9, 2007
     */
    public boolean addUpdateCell(int i, Cell input, boolean overwrite)
    {        
        boolean retVal = false;
        
        // if you've got a valid stop (with a valid stop time in it)
        if(input != null && input.getTime() >= 0 && i >= 0)
        {
            // and if were either forcing an overwrite, or the existing cell time
            // is blank, set this cell to new stop time...
            if(overwrite || getTime(i) < 0)
            {
                retVal = addSetCell(i, input);
            }
        }
        return retVal;
    }

    // generic getter and setter methods
    // the i parameter defines the column field
    public Cell getCell(int i)
    { 
        try
        {
            return m_row.get(i);
        }
        catch(Exception e)
        {
            return null;
        }
    }

    
    public void setFootnoteSymbol(String s)
    {
        setFootnoteSymbol(s, false);
    }
    public void setFootnoteSymbol(String s, boolean allRows)
    {
        if(s == null || s.length() < 1) return;

        if(allRows)
        {
            for(int i = 0; i < getLen(); i++)
            {
                // find the next valid cell
                Integer k = getNextCellIndex(this, i);
                if(k == null)
                    break;
                i = k;
                
                // mark the cell's footnote
                Cell c = getCell(i);
                if(c != null) 
                {
                    c.setFootnoteSymbol(s);
                }
            }
        }
        else
        {
            // mark the cell's footnote
            Cell c = getFirstNonNullCell();
            if(c != null) 
            {
                c.setFootnoteSymbol(s);
            }
        }
    }
    public static int setFootnoteSymbol(List<Row> rows, String mark)
    {                
        int retVal = 0;
        if(rows == null || rows.size() < 1) return retVal;        
                
        for(Row r : rows)
        {
            Cell c = r.getFirstNonNullCell();
            if(c != null)
            {
                r.setFootnoteSymbol(mark);
                retVal++;
            }
        }
        
        return retVal;
    }

    public String getFootnoteSymbol()
    {
        String retVal = "";
        Integer i = getNextCellIndex(this, 0);
        if(i != null) {
            retVal = getFootnoteSymbol(i);
        }

        return retVal;
    }
    public String getFootnoteSymbol(int i)
    {
        try
        {
            return m_row.get(i).getFootnoteSymbol();
        }
        catch(Exception e)
        {
            return "";
        }
   }
    public boolean isHighlighted(int i)
    { 
        try
        {
            return getCell(i).isHighlighted();
        }
        catch(Exception e)
        {
            return false;
        }
    }
    
    public List<Cell> getRow()
    {
        return m_row;
    }

    public void setRow(List<Cell> row)
    {
        m_row = row;
    }

    public int getTime(int i)
    { 
        try
        {
            return getCell(i).getTime();
        }
        catch(Exception e)
        {
            return -1;
        }
    }

    public String getTimeAsStr(int i)
    { 
        try
        {
            return getCell(i).getTimeAsStr();
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public void clearProcessedFlag()
    { 
        try
        {
            for(Cell c : m_row)
            {
                if(c != null)
                    c.setProcessed(false);
            }
        }
        catch(Exception e)
        {
        }
    }
    
    public int getTripLength()
    {
        int retVal = 0;
        try
        {
            Cell f = getFirstNonNullCell();
            Cell l = getLastNonNullCell();            
            retVal = l.getTime() - f.getTime();
        }
        catch(Exception e)
        {
        }   
        
        return retVal;
    }


    

    public void clearProcessedFlag(List<Row> schedule)
    {
        clearProcessedFlagStatic(schedule);
    }
    public static void clearProcessedFlagStatic(TimesTable tt)
    {
        if(tt == null) return;
        clearProcessedFlagStatic(tt.getTimeTable());
    }
    public static void clearProcessedFlagStatic(List<Row> schedule)
    { 
        if(schedule == null) return;
        
        for(Row r : schedule)
        {
            r.setProcessed(false);
            r.clearProcessedFlag();
        }
    }
    public static void clearRowProcessedFlagStatic(List<Row> schedule)
    { 
        if(schedule == null) return;
        
        for(Row r : schedule)
        {
            r.setProcessed(false);
        }
    }

    public static List<Row> findUnProcessed(TimesTable tt)
    {
        if(tt == null) return null;
        return findByProcessedFlag(tt.getTimeTable(), false);
    }
    public static List<Row> findProcessed(TimesTable tt)
    {
        if(tt == null) return null;
        return findByProcessedFlag(tt.getTimeTable(), true);
    }
    public static List<Row> findByProcessedFlag(List<Row> schedule, boolean processed)
    { 
        if(schedule == null) return null;
        
        List<Row> retVal = new ArrayList<Row>();
        for(Row r : schedule)
        {
            if(r == null) continue;
            if(r.isProcessed() == processed)
                retVal.add(r);
        }
        
        return retVal;
    }
    
    
    public void init()
    {
        int mid = m_row.size() / 2;
        Cell c = null;
        for(int i = mid; i < m_row.size(); i++)
        {
            c = m_row.get(i);
            if(c != null && c.getTrip() != null)
                break;
        }

        if(c == null)
        {
            for(int i = mid; i >= 0; i--)
            {
                c = m_row.get(i);
                if(c != null && c.getTrip() != null)
                    break;
            }
        }
        
        if(c != null)
        {
            m_trip      = c.getTrip();
            m_tripType  = c.getTripType();
            m_block     = c.getBlock();
            m_rawSvcKey = c.getRawSvcKey();
        }
    }
    
    public String getTrip()
    {
        if(m_trip == null) {
            init();
        }
        
        return m_trip;
    }

    public void setTrip(String trip)
    {
        m_trip = trip;
    }
    
    
    public String getRawSvcKey()
    {
        // NOTE: we init svc key above
        if(m_rawSvcKey == null) {
            init();            
        }
        
        return m_rawSvcKey;
    }    


    
    public String getTripType()
    {
        // NOTE: we init tripType above
        if(m_tripType == null) {
            init();
        }
        
        return m_tripType;
    }   

    
    public void setTripType(String t)
    {
        m_tripType = t;
    }
    
    public String getBlock()
    {
        // NOTE: we init block above in init()
        if(m_block == null) {
            init();            
        }
        
        return m_block;
    }

    public void setBlock(String block)
    {
        m_block = block;
    }

    public Cell getFirstNonNullCell()
    {
        return getNextCell(this, 0);
    }    
    public Cell getNextNonNullCell(int y)
    {
        return getNextCell(this, y);
    }    
    public Cell getLastNonNullCell()
    {
        return getPrevCell(this, m_row.size() - 1);
    }    
    public Cell getPrevNonNullCell(int y)
    {
        return getPrevCell(this, y);
    }
    
    
    /*******************************
    
    STATIC UTILITY ROUTINES 
    
    TODO: could be cleaned up a bit (and some routines deleted)        

    ******************************/
    
   
    public static Row findLongestTrip(List<Row> rowList)
    {
        Row retVal = null;
        int len = 0;
        
        if(rowList != null && rowList.size() > 0)
        {
            retVal = rowList.get(0);
            for(Row r : rowList)
            {
                if(r == null) continue;
                
                int t = r.getTripLength();
                if(t > len)
                {
                    retVal = r;
                    len = t;
                }
            }
        }
        
        return retVal;
    }    

    
    public static Integer getNextCellIndex(Row row, int x)
    {
        Integer index = null;
                
        if(row != null) 
        {        
            for(int i = x; i < row.getLen(); i++)
            {
                Cell c = row.getCell(i);
                if(c == null) continue;
                if(c.getTime() <= 0 && (c.getTrip() == null || c.getTrip().length() < 1)) continue;

                // found a cell, now break;
                index = i;
                break;
            }        
        }
        return index;
    }
    public static Integer getPrevCellIndex(Row row, int x)
    {
        Integer index = null;
        if(row != null) 
        {
            if(x >= row.getLen()) x = row.getLen() - 1;
            
            for(int i = x; i >= 0; i--)
            {
                Cell c = row.getCell(i);
                if(c == null) continue;
                if(c.getTime() <= 0 && (c.getTrip() == null || c.getTrip().length() < 1)) continue;

                // found a cell, now break;
                index = i;
                break;
            }        
        }
        return index;
    }
    
    public static Cell getNextCell(Row row, int x)
    {
        Cell retVal = null;
        if(row != null)
        {
            Integer i = getNextCellIndex(row, x);
            if(i != null)
                retVal = row.getCell(i);
        }
        return retVal;
    }
    public static Cell getPrevCell(Row row, int y)
    {
        Cell retVal = null;
        if(row != null)
        {
            Integer i = getPrevCellIndex(row, y);
            if(i != null)
                retVal = row.getCell(i);
        }
        return retVal;
    }

    
    public static Cell getNextCell(List<Row> rows, int x, int y)
    {
        if(rows == null) return null;
        
        y++;
        for(int i = x; i < rows.size(); i++)
        {
            if(rows.get(i) == null) continue;
            
            Cell c = getNextCell(rows.get(i), y);
            y = 0; // on next iteration, reset to 0
            if(c == null) continue;
            if(c.getTime() <= 0 && (c.getTrip() == null || c.getTrip().length() < 1)) continue;

            return c;
            
        }        

        return null;
    }
    public static Cell getPrevCell(List<Row> rows, int x, int y)
    {
        if(rows == null) return null;
        
        boolean firstIteration = true;

        for(int i = x; i >= 0; i--)
        {
            // 
            if(rows.get(i) == null) continue;
            if(!firstIteration)
                y = rows.get(i).getLen();


            Cell c = getPrevCell(rows.get(i), y - 1);
            if(c == null) continue;
            firstIteration = false;
            
            if(c.getTime() <= 0 && (c.getTrip() == null || c.getTrip().length() < 1)) 
                continue;

            return c;            
        }        

        return null;
    }

    
    public static List<Row> cull(List<Row> rows, CullTrips cull)
    {
        if(rows == null) return null;
        if(cull == null) return null;
        
        // find all the rows that fit into a certain time-slice
        List<Row> newTable = new ArrayList<Row>();            
        for(Row r : rows)
        {       
            Cell c = null;
            if(cull.getRule() == CullTrips.CullRule.END_TRIP)
            {
                c = r.getLastNonNullCell();
            }
            else 
            {
                c = r.getFirstNonNullCell();
            }
            
            if(c == null) continue;                
            if(c.getTime() < cull.getStart())  continue;
            if(c.getTime() > cull.getFinish()) continue;
            
            newTable.add(r);
        }
        
        return newTable;
    }    
    
    
    public static int numNonNullCells(Row row)
    {
        int retVal = 0;
                       
        if(row != null) 
        {        
            for(Cell c : row.getRow())
            {
                if(c == null || c.getTime() <= 0) continue;

                // found a cell, count it
                retVal++;
            }        
        }
       
        return retVal;
    }
    
    public static final int NOT_FOUND = -111;
    public static boolean areTimesDifferent(Row row)
    {
        boolean timeDiff = false;
        if(row == null) return timeDiff;
        
        int time = NOT_FOUND;        
        for(int i = 0; i < row.getLen(); i++)
        {
            Cell c = row.getCell(i);
            if(c == null) continue;

            if(time == NOT_FOUND) 
            {
                time = c.getTime();
            }
            else if(time != c.getTime())
            {
                timeDiff = true;
                break;
            }
        }        

        return timeDiff;
    }    

    
    public Cell findStopTime(String stopId)
    {
        if(stopId == null) return null;
        Cell retVal = null;        
        
        for(Cell c : m_row)
        {
            if(c == null) continue;
            if(c.getStopId().equals(stopId))
            {
                retVal = c;
                break;
            }
        }
        
        return retVal;
    }
      
    
    public static int markTrips(List<Row> rows, List<String> trips, String mark)
    {
        int retVal = 0;        
        if(rows == null || trips == null || trips.size() < 1) 
            return retVal;
        
        for(Row r : rows)
        {
            if(r == null)           continue;
            if(r.getTrip() == null) continue;
            for(String t : trips)
            {
                if(r.getTrip().equals(t))
                {
                    r.setTripType(mark);
                    retVal++;
                    break;
                }
            }
        }    
        return retVal;
    }

    public static Integer getNearestNonNullIndex(Row row, Integer x, int time)
    {
        Integer retVal = null;        
        Cell    nC = getNextCell(row, x);
        Cell    pC = getPrevCell(row, x);

        if(nC != null && pC != null)
        {
            int nT = Math.abs(nC.getTime() - time);
            int pT = Math.abs(pC.getTime() - time);
            
            if(nT < pT) 
                retVal = row.getRow().indexOf(nC);
            else
                retVal = row.getRow().indexOf(pT);
        }
        else if(nC != null)
        {
            retVal = row.getRow().indexOf(nC);
        }
        else if(pC != null)
        {
            retVal = row.getRow().indexOf(pC);
        }

        return retVal;
    }

    
    /**
     * Finds the next row in a list, with a cell that matches the block parameter.
     * NOTE: that we're using the Row isProcessed check in order to limit the iterations & compares
     * 
     * 
     * @param timeTable
     * @param startRowIndex
     * @param block
     * @return
     */
    public static Integer findRowViaBlock(List<Row> timeTable, int startRowIndex, String block)
    {
        if(timeTable == null || block == null) return null;
        
        Integer rowIndex = null;
        for(int i = startRowIndex; i < timeTable.size(); i++)
        {
            Row row = timeTable.get(i);
            if(row == null || row.isProcessed()) continue;
            
            Cell cell = row.getFirstNonNullCell();
            if(cell == null) continue;
            
            if(block.equals(cell.getBlock()))
            {
                rowIndex = i;
                break;
            }                
        }
            
        return rowIndex;
    }

    public static Integer findRowViaTrip(List<Row> timeTable, String trip)
    {
        if(timeTable == null || trip == null) return null;
        
        Integer rowIndex = null;
        for(int i = 0; i < timeTable.size(); i++)
        {
            Row row = timeTable.get(i);
            if(row == null || row.isProcessed()) continue;
                
            
            Cell cell = row.getFirstNonNullCell();
            if(cell == null) continue;
            
            if(trip.equals(cell.getTrip()))
            {
                rowIndex = i;
                break;
            }                
        }
            
        return rowIndex;
    }
    
    
    /**
     * This routine is used (at least) for Loop Fill-Ins.  We'll take a new set of times, and append them
     * to the best fitting trip.  The way that Loop Fill-In uses this routine is to call it multiple
     * times to fill in multiple trips.  That's where the Row's isProcessed routine comes in handy.
     * 
     * NOTE: that we're calling findRowViaBlock, which uses the Row isProcessed check.  Thus, before
     *       we're calling the find routine, we're going to set the flag in this routine on any 
     *       trips that we've already processed. 
     * 
     * @param timeTable
     * @param columnIndex
     * @param fillIn
     * @param fromRight
     * @return boolean, which indicates to the caller that the fillTrip was added (or not) to the timeTable.
     */
    public static boolean fillInTrips(List<Row> timeTable, Integer columnIndex, List<Cell> fillIn, Integer tripTime)
    {
        final int DEF_TIME = 11111111;
        boolean retVal = false;
        if(timeTable == null || columnIndex == null || fillIn == null) 
            return false;
        
        if(tripTime == null)
            tripTime = DEF_TIME;
        
        String  block  = fillIn.get(0).getBlock();
        int     time   = fillIn.get(0).getTime();
                
        // part A: finder routine -- tries to find the best trip for the fill-in, based on times and block 
        int foundTime = DEF_TIME;
        Integer foundIndex = null;
        for(int i = 0; i < timeTable.size(); i++)
        {
            Integer thisRowIndex = findRowViaBlock(timeTable, i, block);
            if(thisRowIndex == null)
                break;
            
            i = thisRowIndex;
            Row     row   = timeTable.get(thisRowIndex);
            Integer index = getNearestNonNullIndex(row, columnIndex, time);
            Cell    cell  = row.getCell(index);
            if(cell == null) {
                LOGGER.warning("There is a row with all NULL times.  This shouldn't happen.");
                continue;
            }
            
            // if this found stop is in a position in the trip that comes before our fill, but the time is later, 
            // or if the found stop is futher in the trip, yet the time is earlier, then that's a stopping condition 
            if(index <= columnIndex)  {
                if(cell.getTime() > time) {
                    break;                
                }
            } else {
                if(cell.getTime() < time) {
                    break;                
                }
            }
            
            // compare to find best time-fit for this trip
            int thisTime = Math.abs(time - cell.getTime());            
            
            // if the time spread between stops is more than the entire trip lenght, then we're lookingat the wrong trip, so exit the loop.
            // NOTE: we don't continue because the rows are (assumed) to be ordered by time, thus this spread will only get larger
            if(thisTime > tripTime) {
                break;
            }                
            
            if(thisTime < foundTime)
            {
                foundTime = thisTime;
                foundIndex = thisRowIndex;
            }
        }

        // part B: fill-in assignment -- here's where we append the fill-in to the table
        // NOTE:   if we don't fill in anything, then this fillTrip is not being processed -- hence the boolean result
        if(foundIndex != null)
        {
            Row row = timeTable.get(foundIndex);
            row.setProcessed(true);

            for(int i = columnIndex, j = 0; i < row.getLen() && j < fillIn.size(); i++, j++)
            {
                Cell r = row.getCell(i);
                if(r == null)
                {
                    row.setCell(i, fillIn.get(j));
                }
            }
            retVal = true;
        }
            
        return retVal;
    }

    /**
     * adds a new row to the table, in semi-sorted order. 
     * 
     * @param m_tt
     * @param newRow
     */
    public static void placeTripIntoTable(TimesTable tt, Row newRow)
    {
        if(tt == null || tt.getTimeTable() == null || newRow == null) return;                        

        Cell t = newRow.getFirstNonNullCell();
        if(t == null) return;

        int time  = t.getTime();
        int index = 0;
        for(Row r : tt.getTimeTable())
        {
            Cell c = r.getFirstNonNullCell();
            if(c.getTime() > time)
                break;
            
            index++;
        }
        
        tt.getTimeTable().add(index, newRow);
    }
    
    public static int appendTripsToTable(TimesTable tt, List<Row> newRows, Integer minNumStops, String stopId)
    {                
        int retVal = 0;
        if(tt == null || IntUtils.isEmpty(newRows)) return retVal;
        
        for(Row r : newRows)
        {
            // filter out trips with less than N time points 
            if(minNumStops != null && (numNonNullCells(r) < minNumStops) ) {
                continue;
            }

            // filter out trips that don't have X stop id as a valid time
            if(stopId != null && r.findStopTime(stopId) == null) {
                continue;
            }
            
            placeTripIntoTable(tt, r);
            retVal++;
        }

        Collections.sort(tt.getTimeTable(), new Row.Compare(tt.getTimePoints().size()));
        
        return retVal;
    }

    public String getRoute()
    {
        return m_route;
    }

    public DirType getDirection()
    {
        return m_direction;
    }

    public int getCount()
    {
        int retVal = 0;
        if(m_row != null)
        {
            for(Cell c : m_row)
            {
                if(c != null) {
                    retVal++;
                }
            }            
        }
        return retVal;
    }

    /**
     * this method will determine whether two trips are the same
     * 
     * @param r1
     * @param r2
     * @return boolean to indicate whether the two trips are basically the same
     * 
     */
    public static boolean haveTimesInCommon(Row r1, Row r2, int buffer)
    {
        if(r1 == null || r2 == null) return false;
        if(r1.getRow() == null || r2.getRow() == null) return false;
        
        boolean retVal = false;
        
        // step 1: get our two cell lists and the smallest length (compare no more than that)
        List<Cell> c1 = r1.getRow();
        List<Cell> c2 = r2.getRow();
        int length = c1.size();
        if(c1.size() > c2.size())
        {
            length = c2.size(); // want the smallest value / size
        }
        
        for(int i = 0; i < length; i++)
        {
            Cell a = c1.get(i);
            Cell b = c2.get(i);
            
            if(a == null || b == null) continue;
            int timeA = a.getTime();
            int timeB = b.getTime();
            if(timeA == timeB)
            {
                retVal = true;
                break;
            }
            else if(buffer > 0)
            {
                int diff = Math.abs(timeA - timeB);
                if(diff <= buffer)
                {
                    retVal = true;
                    break;
                }
            }
        }
        
        
        return retVal;
    }
    public static boolean haveTimesInCommon(Row r1, Row r2)
    {
        return haveTimesInCommon(r1, r2, 0);
    }

    
    public static int mergeAIntoB(Row r1, Row r2)
    {
        int retVal = 0;
        if(r1          == null || r2          == null) return retVal;
        if(r1.getRow() == null || r2.getRow() == null) return retVal;

        RowImpl target = (RowImpl)r2;
        List<Cell> aList = r1.getRow();        
        List<Cell> bList = r2.getRow();

        // step 1: if list A is larger than B, then add the remainder of A into B 
        if(bList.size() < aList.size()) 
        {
            for(int i = bList.size(); i < aList.size(); i++)
            {
                bList.add(aList.get(i));
                retVal++;
            }
        }

        // step 2: merge all of A into B...the addUpdateCell
        for(int i = 0; i < aList.size(); i++)
        {
            Cell a = aList.get(i);
            if(a == null) continue;
            boolean updated = target.addUpdateCell(i, a, false);
            if(updated)
                retVal++;
        }

        return retVal;
    }

    public static List<Row> findRowsWithEmptyColumn(List<Row> rows, Integer columnIndex)
    {
        List<Row> retVal = new ArrayList<Row>();            
        for(Row r : rows)
        {                   
            // ok, this row has a null cell in column N, so add it
            if(r != null && r.getCell(columnIndex) == null)
            {
                retVal.add(r);
            }
        }
        
        return retVal;
    }

    public static boolean sameBlock(Row a, Row b)
    {
        if(a == null || b == null) return false;
        if(a.getBlock() == null || b.getBlock() == null) return false;

        return (a.getBlock().equals(b.getBlock()));
    }
    
    public static boolean sameTrip(Row a, Row b)
    {
        if(a == null || b == null) return false;
        if(a.getTrip() == null || b.getTrip() == null) return false;

        return (a.getTrip().equals(b.getTrip()));
    }

    public static boolean sameKey(Row a, Row b)
    {
        if(a == null || b == null) return false;
        if(a.getRawSvcKey() == null || b.getRawSvcKey()  == null) return false;        

        return (a.getRawSvcKey().equals(b.getRawSvcKey()));
    }

/*    
    public static void bb()
    {
        // step C: do the work
        List<Cell> retVal = new ArrayList<Cell>();
        for(Row r : getTimeTable())
        {
            // step 1: clone the cell (if not null)
            Cell c = r.getCell(i);
            if(c != null)
                c = new CellImpl(c);
            
            // step 2: add cloned cell to return list
            retVal.add(c);            
            
            // step 3: WARNING CONTINUES -- might add a NULL row to the list, so no further processing
            if(c == null) continue;
            
            if(LOGGER.isLoggable(DEBUG))
            {
                if(c.getStopId() == null || stopId == null)
                {
                    LOGGER.log(DEBUG, "either supplied and/or cell stopID is null...this is bad");
                }
                else if(stopId.equals(c.getStopId()))
                {
                    LOGGER.log(DEBUG, "supplied stopId " + stopId + " and target cell stopId " + c.getStopId() + " don't match; expect weird results");
                }                
            }
            
            
            // step 4: find & add any footnotes from the row to this cell
            String rSym = r.getFootnoteSymbol();
            if(rSym != null && rSym.length() > 0)
            {
                String cSym = c.getFootnoteSymbol();
                if(cSym == null || !rSym.equals(cSym))
                {
                    boolean passesFilter = true;

                    // step 2c: filter test -- make sure symbol is in any filter list
                    if(filters != null && filters.length > 0 && filters[0].length() > 0)                    
                    {
                        passesFilter = false;
                        for(String f : filters)
                        {
                            if(f == null) continue;
                            if(rSym.equals(f))
                            {
                                passesFilter = true;
                                break;
                            }
                        }
                    }
                    
                    if(passesFilter)
                    {
                        c.setFootnoteSymbol(rSym);
                    }
                }
            }
        }
    }
*/    
}