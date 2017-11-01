/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.RowImpl;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.IntUtils;

/**
 * The purpose of CullTrips is to limit the output trips in a timetable, based on
 * a start and end time.  E.g., simply, the configuration allows you to say, "only show
 * trips that start a time X and end at time Y during a given day".
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class CullTrips extends Configure
{
    public enum CullRule 
    {
        BEGIN_TRIP, END_TRIP, BLOCKCULL, VERTICULL;

        public static CullRule construct(String str)
        {
            CullRule retVal;
            try
            {
                if(str != null && str.length() > 1)
                {
                     retVal = CullRule.valueOf(str);
                }
                else
                {
                    retVal = CullRule.BLOCKCULL;
                }
            }
            catch (RuntimeException e)
            {
                e.printStackTrace();
                retVal = CullRule.BLOCKCULL;
            }  
            
            return retVal;
        }
    }
    
    @CsvColumn(index=6, name="Rule: BEGIN_TRIP, END_TRIP, VERTICULL",  details="One of these Cull-Rules determines where the bounds of the cull happen -- VERTICULL is not time based...rather, it compares two sequenctial trips,  and if it find trips that appear to be duplicates,  it will cull one of the two.")
    public CullRule m_rule;

    @CsvColumn(index=7, name="Start", details="Cull Boundry: stop times after start(and before finish) are NOT culled.  Value is in hours past midnight (eg: 0 = 12:AM, 12 = 12:PM, 24 = 12:AM is the next physical day)")
    public int m_start;
    
    @CsvColumn(index=8, name="Finish", details="Cull Boundry: stop times after start(and before finish) are NOT culled.  Value is in hours past midnight (eg: 0 = 12:AM, 12 = 12:PM, 24 = 12:AM is the next physical day)")
    public int m_finish;

    @CsvColumn(index=9, name="Buffer", details="VERTICULL culls trips w/same block & time.  Buffer is a time window between the two trips (so times don't need to be the same).")
    public Integer m_buffer;
    
    public CullTrips()
    {        
    }

    public CullTrips(CullRule rule, int start, int finish, int bufferInMinutes)
    {
        m_rule   = rule;
        m_start  = start;
        m_finish = finish;
        m_buffer = bufferInMinutes;
    }

    public CullTrips(String agency, String route, String dir, String key)
    {
        super(agency, route, dir, key);
    }

    public int getStart()
    {
        return m_start * 60 * 60;  // seconds past midnight
    }
    
    public int getFinish()
    {
        return m_finish * 60 * 60;  // seconds past midnight
    }

    public Integer getBuffer()
    {
        return m_buffer;
    }

    public int getBufferInSeconds()
    {
        return m_buffer == null ? 0 : m_buffer * 60;
    }
    
    public CullRule getRule()
    {
        return m_rule;
    }
        
    
    // STRING SETTERS -- NEEDED FOR REFLECTION / EDITOR TOOL
    public void setFinish(String finish)
    {        
        m_finish = IntUtils.getIntegerFromString(finish, 0);
    }
    public void setRule(String rule)
    {
        m_rule = CullRule.construct(rule);
    }
    public void setStart(String start)
    {
        m_start = IntUtils.getIntegerFromString(start, 0);
    }

    public void setBuffer(String buff)
    {
        m_buffer = IntUtils.getIntegerFromString(buff);
    }
    
    
    /**
     * Will read the configuration, and cull trips from the input TimeTable for the matching parameters
     * 
     * @param m_tt
     */
    synchronized public static void process(TimesTable tt)
    {
        if(!okToProcessHasTimeTable(tt, Configure.class)) return;

        CullTrips index = new CullTrips(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString());
        List ctList = tt.getConfiguration().findAllData(index);
        if(ctList == null || ctList.size() < 1) return;
                
        Row lr = RowImpl.findLongestTrip(tt.getTimeTable());
        int maxBuff = lr.getTripLength();

        for(CullTrips ct : (List<CullTrips>)ctList)
        {
            if(ct == null) continue;
            int buff = ct.getBufferInSeconds();
            if(ct.getBuffer() == null)
                buff = maxBuff;
            
            if(ct.getRule() == CullRule.BLOCKCULL)
            {
                List<Row> newRows = new ArrayList<Row>();
                List<Row> rows    = tt.getTimeTable();
                
                // sort the rows by block to get them all together for comparison 
                Collections.sort(rows, new Row.BlockSort());
                Row currRow = null;                
                for(Row r : rows)
                {
                    if(r == null) continue;                
                    if(currRow != null && r.getBlock() != null && r.getBlock().equals(currRow.getBlock()))
                    {
                        // TODO - we might add more logic here regarding service key...but that's a big if
                        
                        if(RowImpl.haveTimesInCommon(currRow, r, buff))
                            continue;
                    }
                    
                    currRow = r;
                    newRows.add(r);
                }
                
                // now we'll end up by sorting our new set of rows by time, and putting them into the table 
                Collections.sort(newRows, new Row.Compare(newRows.size()));
                tt.setTimeTable(newRows);
            }
            else if(ct.getRule() == CullRule.VERTICULL)
            {
                verticull(tt, buff);
            }
            else
            {
                tt.cull(ct);
            }
        }            
    }
    
    public static List<Row> verticull(TimesTable tt)
    {
        Row lr = RowImpl.findLongestTrip(tt.getTimeTable());
        int maxBuff = lr.getTripLength();
        return verticull(tt, maxBuff);
    }
    
    public static List<Row> verticull(TimesTable tt, int buff)
    {
        if(tt == null) return null;
        
        List<Row> newRows = new ArrayList<Row>();
        List<Row> rows = tt.getTimeTable();
        for(int i = 0; rows != null && i < rows.size() - 1; i++)            
        {
            Row r1 = rows.get(i);
            Row r2 = rows.get(i+1);
            if(r1 == null) continue;
            if(r2 != null && r1.getBlock().equals(r2.getBlock()))
            {
                if(RowImpl.haveTimesInCommon(r1, r2, buff))
                {
                    cull(r1, r2, newRows);
                    continue; // NOTE CONTINUE -- avoids putting r1 back in list (below)
                }
            }

            // for some reason the block / common times don't match -- so add this row if not already there
            if(!newRows.contains(r1)) {
                newRows.add(r1);
            }                        
        }

        // and don't forget the last element
        Row last = rows.get(rows.size() - 1);
        if(!newRows.contains(last)) {
            newRows.add(last);
        }
        
        if(newRows != null && newRows.size() > 0)
        {
            tt.setTimeTable(newRows);
        }

        return newRows;
    }

    /**
     * currently only used by ComboRoutes
     * might not be a general purpose routine outside of ComboRoutes
     */
    public static List<Row> duplicull(TimesTable tt)
    {
        if(tt == null || tt.getTimeTable() == null || tt.getTimeTable().size() < 1) return null;
        
        List<Row> newRows = new ArrayList<Row>();
        List<Row> rows = tt.getTimeTable();
        
        Row lr = RowImpl.findLongestTrip(tt.getTimeTable());
        int maxBuff = lr.getTripLength();

        
        int rowSize = rows.size() - 1;
        RowImpl.clearProcessedFlagStatic(rows);        
        for(int i = 0; rows != null && i < rowSize; i++)            
        {
            Row a = rows.get(i);
            Row b = rows.get(i+1);

            if(a == null || b == null)                       continue;            
            if(a.getBlock() == null || b.getBlock() == null) continue;

            boolean sameBlock = RowImpl.sameBlock(a, b);
            boolean sameTrip  = RowImpl.sameTrip(a, b);
            boolean sameKey   = RowImpl.sameKey(a, b);
            
            
            if(sameBlock)
            {
                if(sameTrip && sameKey)
                {
                    cull(a, b, newRows);
                }
                else if(sameKey)
                {
                    if(RowImpl.haveTimesInCommon(a, b, maxBuff))
                    {
                        cull(a, b, newRows);       
                    }
                }
                else if(i < rowSize - 2)
                {
                    /*
                     *  TODO: cleanup
                     *  this is kind of a hack right now...
                     *  the merge 
                     */
                    Row c = rows.get(i+2);
                    if(RowImpl.sameBlock(a, c))
                    {
                        cull(a, b, newRows);
                        i++; 
                        cull(a, c, newRows);
                        i++;
                    }
                }
            }
            
            // for some reason the block / common times don't match -- so add this row if not already there
            if(!a.isProcessed()) {
                newRows.add(a);
                a.setProcessed(true);
            }                        
        }

        // and don't forget the last element
        Row last = rows.get(rows.size() - 1);
        if(!newRows.contains(last)) {
            newRows.add(last);
        }
        
        // lastly, reset rows in TT
        if(newRows != null && newRows.size() > 0)
        {
            tt.setTimeTable(newRows);
        }
        
        return newRows;
    }

    private static void cull(Row a, Row b, List<Row> newRows)
    {
        RowImpl.mergeAIntoB(a, b);
        newRows.remove(a);
        newRows.add(b);
        a.setProcessed(true);
        b.setProcessed(true);                    
    }
}

