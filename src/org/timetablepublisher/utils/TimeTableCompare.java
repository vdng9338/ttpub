/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.RowImpl;
import org.timetablepublisher.table.TimesTable;


/**
 * The purpose of TimeTableCompare is to compare two different TimesTables, and then
 * generate a list of rows (diffTable) that show a side-by-side (or row-by-row) compare
 * of the two
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Jan 11, 2007
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class TimeTableCompare implements Constants
{
    public static final Level   DEBUG   = Level.SEVERE; // change me to get debug messages
    private static final Logger LOGGER  = Logger.getLogger(TimeTableCompare.class.getCanonicalName());
    private static final String NO_COMP = "NO COMP";
    private static final int DEFAULT_TIME_BUFFER = 60 * 3; // default is 3 minutes
    
    TimesTable m_tableA = null;
    TimesTable m_tableB = null;
    
    protected final List<Row> m_diffTable;
    protected final boolean m_areStopTimesEqual;
    protected final boolean m_areTimePointsEqual;
    protected int m_timeBuffer = DEFAULT_TIME_BUFFER; 
    
    
    public TimeTableCompare(TimesTable a, TimesTable b, String buffer)
    {        
        m_diffTable = new ArrayList<Row>();
        m_tableA = a;
        m_tableB = b;
        setTimeBuffer(buffer);
        
        m_areTimePointsEqual = compareSchedulingTimepoints();        
        m_areStopTimesEqual  = compareStopTimes();
        
        if(diffTableNotOK())
        {
            LOGGER.log(DEBUG, "Uh-Oh: Diff Table for " + a.getRouteID() + " " + a.getDir() + " " + a.getKey() + " is out of whack.  Trip counts do not match up");
        }        
    }

    private boolean diffTableNotOK()
    {
        if(m_tableA == null) return true;
        if(m_tableB == null) return true;
        
        boolean retVal = false;
        if(LOGGER.isLoggable(DEBUG))
        {
            retVal |= testDiffTable(m_tableA.getTimeTable(), "A");
            retVal |= testDiffTable(m_tableB.getTimeTable(), "B");
        }        
        return retVal;
    }

    private boolean testDiffTable(List<Row> row, String rowName)
    {
        if(row == null)
        {
            LOGGER.log(DEBUG, "ROW " + rowName + " is NULL ... not good.");
            return true;
        }
        
        boolean retVal = false;
        for(Row r : row)
        {
            if(r == null) continue;

            int fi = m_diffTable.indexOf(r);
            int li = m_diffTable.lastIndexOf(r);
            if(fi < 0)
            {
                LOGGER.log(DEBUG, "ROW " + rowName + " - trip: " + r.getTrip() + " did not make it into the diff table.");
                retVal = true;
            }
            if(fi != li)
            {
                LOGGER.log(DEBUG, "ROW " + rowName + " - trip: " + r.getTrip() + " is repeated multiple times.");
                retVal = true;
            }
        }
        
        return retVal;
    }

    public TimesTable getScheduleA()
    {
        return m_tableA;
    }
    public TimesTable getScheduleB()
    {
        return m_tableB;
    }

    public boolean areStopTimesEqual()
    {
        return m_areStopTimesEqual;
    }

    public boolean areTimePointsEqual()
    {
        return m_areTimePointsEqual;
    }

    public List<Row> getDiffTable()
    {
        return m_diffTable;
    }

    public int getTimeBuffer()
    {
        return m_timeBuffer;
    }
    public int getTimeBufferInMinutes()
    {
        return m_timeBuffer / 60;
    }

    public void setTimeBuffer(int timeBufferInSeconds)
    {
        m_timeBuffer = timeBufferInSeconds;
    }    

    public void setTimeBuffer(String timeBuffer)
    {
        if(timeBuffer == null) return;
        
        try
        {
            int tmp = Integer.parseInt(timeBuffer);
            if(tmp >= 0 && tmp < 100)
            {
                // convert minutes to seconds -- since stop times are in seconds (past midnight)
                m_timeBuffer = tmp * 60;
            }
        }
        catch(Exception e)
        {
        }
    }    
    
    
    public boolean compareSchedulingTimepoints()
    {                
        Stop AAA[] = new Stop[m_tableA.getSchedulingTimePoints().size()];
        m_tableA.getSchedulingTimePoints().toArray(AAA);
        Stop BBB[] = new Stop[m_tableB.getSchedulingTimePoints().size()];
        m_tableB.getSchedulingTimePoints().toArray(BBB);
        
        // test 1: if the arrays aren't the same length, they are not the same
        if(AAA.length != BBB.length) 
            return false;
        
        for(int i = 0; i < AAA.length; i++)
        {
            Stop a = AAA[i];
            Stop b = BBB[i];  
            
            // test 2: check whether the stop ID is the same 
            if(a.getStopId().compareTo(b.getStopId()) != 0)
                return false;
            
            // test 3: check whether the stop description is the same
            String descA = a.getDescription().trim();
            String descB = b.getDescription().trim();
            if(!descA.equals(descB))
                return false;
        }
        
        // return TRUE (schedules are the same) when all the above tests are passed
        return true;        
    }

    

    /**
     * Core time compare processing method.
     *  
     * NOTE: Totally New Compare Method -- to see older method, look back in SVN past 1-1-2007
     * 
     * @return
     */
    public boolean compareStopTimes()
    {           
        boolean retVal = true;
        List<Row> aRowList = m_tableA.getTimeTable();
        List<Row> bRowList = m_tableB.getTimeTable();
        if(aRowList == null || bRowList == null) return retVal;
        
        // fix up the two tables, such that they have the same column indicies
        fix(m_tableA, m_tableB);
        
        // test 1: test if the number of rows in both tables aren't the same
        if(aRowList.size() != bRowList.size())
        {
            retVal = false;
        }
        
        // test 2: find all contiguous matching rows between two tables
        RowImpl.clearProcessedFlagStatic(aRowList);
        RowImpl.clearProcessedFlagStatic(bRowList);

        // step 1: pair up the trips
        BestFit best = new BestFit();
        for(Row a : aRowList)
        {   
            if(a == null) continue;

            BestFit.Item bi = best.getNewItem(a);
            Row target  = null;
            
            // find the best row in list BBB to compare with the current from list AAA
            for(Row b : bRowList)
            {
                if(b == null || b.isProcessed()) continue;
                
                // exactly the same...nothing else to prove
                if(compareTwoRows(a, b, 0))
                {
                    best.setPerfectMatch(bi, b);
                    target = b;
                    break;
                }

                // compare with time buffer...looking for the best target
                if(compareWithBuffer(a, b, m_timeBuffer))
                {
                    if(target != null)
                    {
                        target = compareThreeRows(a, b, target);
                    }
                    else
                    {
                        target = b;
                    }
                    
                    if(!bi.setOtherMatch(b))
                        break;
                }                 
            }
            
            // this is the best match for A
            if(target != null)
            {
                bi.setB(target);
            }
        }

        // step 3: eliminate multiple B's in the list (eg: A - B should be 1-1...the process above 
        //         produces 1-Many) thru rationalization process that finds the best match for A.
        best.rationalizeList();
        
        // step 4: we've processed all the compares above (finding the best fitting trips between to lists)
        //         so now it's time to lay the data out into our final diff list
        for(BestFit.Item bi : best.m_itemList)
        {
            if(bi == null) continue;
            Row aa = bi.getA();
            Row bb = bi.getB();
            if(bb == null) bb = bi.getOtherBs();
            if(bb == null)
            {
                if(oddTrip(aa))
                    retVal = false;
            }
            else
            {
                if(!bi.isPerfectMatch()) 
                {
                    boolean tmp = compareAndMarkRows(aa.getRow(), bb.getRow(), m_timeBuffer);
                    if(tmp == false)
                    {
                        retVal = false;
                    }
                }
                addToDiffList(aa, bb);
            }
        }
        
        return retVal;
    }

    
    private void addToDiffList(Row a, Row b)
    {
        a.setProcessed(true);
        b.setProcessed(true);
        m_diffTable.add(a);
        m_diffTable.add(b);
    }

    public boolean anyOddTrips(List<Row> rowList)
    {
        if(rowList == null) return false;
        
        boolean retVal = false;
        for(Row r : rowList)
        {
            if(r == null || r.isProcessed()) continue;
            retVal |= oddTrip(r);
        }
        
        return retVal;
    }
    
    public boolean oddTrip(Row r)
    {
        boolean retVal = false;
        
        Row odd = makeOddTrip(r);
        if(odd != null)
        {
            addToDiffList(r, odd);
            retVal = true;
        }
        
        return retVal;
    }
    public static Row makeOddTrip(Row r)
    {
        if(r == null) return null;

        Row retVal = new RowImpl(r.getRoute(), r.getDirection(), "ODD TRIP", 0);
        retVal.setTrip(NO_COMP);
        
        List<Cell> cList = r.getRow();
        if(cList != null && cList.size() > 0)
        {
            for(Cell c : cList)
            {
                if(c != null) 
                {
                    c.setHighlighted(true);
                }
            }
        }
        
        return retVal;
    }


    class BestFit
    {
        final Map<Row, Integer> m_refCount = new HashMap<Row, Integer>();
        final List<Item>        m_itemList = new ArrayList<Item>();
        
        public BestFit()
        {
        }
        
        public Item getNewItem(Row a, Integer pos)
        {
            Item i = new Item(a);

            // insert into list at point POS
            if(pos != null && pos < m_itemList.size())
                m_itemList.add(pos, i);
            else
                m_itemList.add(i);
            
            return i;
        }

        public Item getNewItem(Row a)
        {
            return getNewItem(a, null);
        }
        
        public Item getNewItem(Row a, Row b, Integer pos)
        {
            Item i = getNewItem(a, pos);
            i.setB(b);
            return i;
        }
        
        public void setPerfectMatch(Item i, Row b)
        {
            if(i == null) return;
            removeReferences(b);
            i.setB(b);
            i.setProcessed();
            i.setPerfectMatch(true);
        }
        
        private void removeReferences(Row x)
        {
            if(x == null) return;
            
            if(m_refCount.containsKey(x))
            {
                Integer cnt = m_refCount.get(x);
                assert(cnt != null);
                if(cnt == null) return;
                for(Item i : m_itemList)
                {
                    if(i == null && i.m_B != null) continue;
                    if(x.equals(i.m_B))
                    {
                        i.setB(null);
                    }
                }
                m_refCount.remove(x);
            }
        }
        
        
        // fix multiple B references
        public void rationalizeList()
        {
            RowImpl.clearProcessedFlagStatic(m_tableA);
            RowImpl.clearProcessedFlagStatic(m_tableB);
            for(Item i : m_itemList)
            {
                if(i != null && i.isPerfectMatch())
                    i.setProcessed();
            }
            
            for(Item i : m_itemList)
            {
                if(i == null || i.getA() == null || i.getA().isProcessed()) continue;
                if(!i.isPerfectMatch())
                {
                    Row target = i.getB();
                    if(target == null) target = i.getOtherBs();
                    if(target == null) continue;
                    
                    if(m_refCount.containsKey(target))
                    {
                        Integer cnt = m_refCount.get(target);
                        if(cnt != null && cnt > 1) 
                        {
                            List<Item> iList = findItems(target, cnt);
                            Item best = iList.get(0);
                            for(int k = 1; k < iList.size(); k++)
                            {
                                Item tmpItem = iList.get(k); 
                                if(tmpItem == null || tmpItem.getA() == null || tmpItem.getB() == null) continue;
                                if(best.equals(tmpItem)) continue;
                                
                                Row tmp = compareThreeRows(target, best.getA(), tmpItem.getA());
                                if(tmp == null) continue;
                                if(tmp.equals(tmpItem.getA()))
                                {
                                    best = tmpItem;
                                }
                            }
                            removeReferences(target);
                            best.setB(target);
                            best.setProcessed();
                        }
                    }
                }
                i.setProcessed();
            }
            
            // clean up any straglers
            List<Row> unPbList = RowImpl.findUnProcessed(m_tableB);
            if(unPbList != null && unPbList.size() > 0)
            {
                // one last chance to match A's with B's
                for(Item i : m_itemList)
                {
                    if(i == null || i.getA() == null) continue;
                    if(i.getB() == null)
                    {
                        Row b = findMatch(i.getA(), unPbList, m_timeBuffer);
                        i.setB(b);
                    }
                    i.setProcessed();
                }
                
                // OK, we have orpahed B's...fit them into the list via the trip number
                for(Row b : unPbList)
                {
                    if(b == null || b.isProcessed()) continue;
                    
                    Row odd = makeOddTrip(b);
                    if(odd != null)
                    {
                        String trip = b.getTrip();
                        if(trip == null) trip = "9999999";
                        Integer pos = posTripMatch(trip);                        
                        Item i = getNewItem(odd, b, pos);
                        i.setProcessed();
                    }
                }                            
            }
        }
        
        public Integer posTripMatch(String trip)
        {
            Integer retVal = null;
            if(trip != null && trip.length() > 0)
            {
                for(int i = 0; i < m_itemList.size(); i++)
                {
                    Item x = m_itemList.get(i);
                    if(x == null || x.getA() == null)       continue;
                    String tmp = x.getA().getTrip();
                    if(tmp == null || tmp.equals(NO_COMP)) continue;
                    
                    if(trip.compareTo(x.getA().getTrip()) <= 0)
                    {
                        retVal = i;
                        break;
                    }                            
                }
            }
            
            return retVal;
        }
        
        public List<Item> findItems(Row target)
        {
            return findItems(target, m_itemList.size());
        }
        public List<Item> findItems(Row target, Integer count)
        {
            if(target == null || count == null || count < 1) return null;
            
            List<Item> retVal = new ArrayList<Item>();
            for(Item i : m_itemList)
            {
                if(retVal.size() >= count) break;
                if(i == null || i.getB() == null) continue;
                
                if(target.equals(i.getB()))
                {
                    retVal.add(i);
                }
            }
            
            return retVal;
        }
        public class Item
        {
            Row m_A;
            Row m_B;
            Row[] m_otherMatches;
            int m_otherIndex = 0;
            boolean m_perfectMatch = false;
            
            protected Item()
            {                
            }

            public void setProcessed()
            {
                if(m_A != null) m_A.setProcessed(true);
                if(m_B != null) m_B.setProcessed(true);
            }

            public Item(Row a)
            {
                setA(a);
            }
            public Item(Row a, Row b)
            {
                setA(a);
                setB(b);
            }
            
            public Row[] getOtherMatches()
            {
                return m_otherMatches;
            }

            public boolean setOtherMatch(Row other)
            {
                if(other != null && !other.isProcessed())
                {
                    if(m_otherMatches == null) m_otherMatches = new Row[10];
                    if(m_otherIndex >= m_otherMatches.length) 
                        return false;
                    
                    m_otherMatches[m_otherIndex++] = other;                    
                }
                
                return true;
            }

            public Map<Row, Integer> getRefCount()
            {
                return m_refCount;
            }
            
            public Integer getRefCount(Row x)
            {
                Integer retVal = 0;
                if(m_refCount.containsKey(x))
                {
                    retVal = m_refCount.get(x);
                    if(retVal == null) retVal = 0;
                }
                return retVal;
            }

            public void setA(Row a)
            {
                m_A = a;
                refCnt(a);
            }

            public void setB(Row b)
            {
                if(b != null && b.isProcessed()) return;
                m_B = b;                
                refCnt(b);
            }
            
            public Row getA()
            {
                return m_A;
            }

            public Row getB()
            {
                return m_B;
            }

            public Row getOtherBs()
            {
                Row retVal = m_B;
                if(m_B == null && m_otherMatches != null)
                {
                    List<Row> tmp = new ArrayList<Row>();
                    for(Row r : m_otherMatches)
                    {
                        if(r == null || r.isProcessed()) continue;
                        tmp.add(r);
                    }
                    if(tmp.size() >= 1) 
                    {
                        Row bestRow = tmp.get(0);
                        if(tmp.size() > 1) 
                        { 
                            for(int i = 1; i < tmp.size(); i++)
                               bestRow = compareThreeRows(m_A, bestRow, tmp.get(i));
                        }
                        retVal = bestRow;
                    }
                }
                
                return retVal;
            }

            private void refCnt(Row x)
            {
                if(x == null) return;
                
                Integer cnt = 1;
                if(m_refCount.containsKey(x)) 
                {
                    Integer tmp = m_refCount.get(x);
                    if(tmp != null) cnt = tmp + 1;
                }
                m_refCount.put(x, cnt);
            }

            public boolean isPerfectMatch()
            {
                return m_perfectMatch;
            }

            public void setPerfectMatch(boolean perfectMatch)
            {
                this.m_perfectMatch = perfectMatch;
            }
        }        
    }
    

    private Row compareThreeRows(Row target, Row x, Row y)
    {
        if(target == null || target.getRow() == null) return null;
        if(x == null      || x.getRow()      == null) return y;
        if(y == null      || y.getRow()      == null) return x;

        double xRank = 0.0;
        double yRank = 0.0;
        
        // criteria 1: size
        if(target.getLen() == x.getLen()) xRank += 3.0;
        if(target.getLen() == y.getLen()) yRank += 3.0;

        // criteria 2: trip type
        if(target.getTripType() != null && target.getTripType().length() > 0)
        {
            if(target.getTripType().equals(x.getTripType()))
                xRank += 5.0;
            else
                xRank -= 3.0;
            
            if(target.getTripType().equals(y.getTripType()))            
                yRank += 5.0;
            else
                yRank -= 3.0;
        }
                
        // criteria 3: trip number
        if(target.getTrip() != null)
        {
            if(x.getTrip() != null && target.getTrip().equals(x.getTrip())) xRank += 2.5;
            if(y.getTrip() != null && target.getTrip().equals(y.getTrip())) yRank += 2.5;
        }
        
        // criteria 4: time positions & differences
        for(int i = 0; i < target.getLen(); i++)
        {
            Cell tCell = target.getCell(i);
            Cell xCell = x.getCell(i);
            Cell yCell = y.getCell(i);

            if(tCell == null)
            {
                // if target cell doesn't have a time, neither should the cell that matches the closest
                if(xCell == null) xRank += 1.5;
                if(yCell == null) yRank += 1.5;
            }
            else
            {
                // if target cell does have a time, then the cell that matches the closest should also have time
                if(xCell == null) xRank -= 1.5;
                if(yCell == null) yRank -= 1.5;
                if(xCell == null || yCell == null) continue;

                // since both candiates have time data, let's compare the diff in times 
                // NOTE: smaller is better here, so X's rank is determined by the diff subtracted
                double xDiff = Math.abs(tCell.getTime() - xCell.getTime()) / 180.0011;
                double yDiff = Math.abs(tCell.getTime() - yCell.getTime()) / 180.0011;
                xRank += (yDiff - xDiff);
                yRank += (xDiff - yDiff);
            }
        }

        // final result -- the larger rank'd row is the better match with the target
        if(xRank >= yRank) return x;
        else               return y;
    }
    
    public static int nearCompareOfTwoRows(Row AAA, Row BBB, int buffer)
    {
        if(AAA == null || BBB == null) return 0;
        
        List<Cell> A = AAA.getRow();
        List<Cell> B = BBB.getRow();        
        if(A == null || B == null) return 0;
        
        Cell[] a = new Cell[A.size()];
        Cell[] b = new Cell[B.size()];                
        a = A.toArray(a);
        b = B.toArray(b);
        
        // iterate over the cells of the row
        int hits = 0;
        for(int j = 0; j < a.length && j < b.length; j++)
        {
            Cell stopA = a[j];
            Cell stopB = b[j];
            
            if(stopA == null || stopB == null) continue;

            // test: see if any times are the same or not                
            int timeDiff = Math.abs(stopA.getTime() - stopB.getTime());
            if(timeDiff <= buffer) 
            {
                hits++;
            }
        }
        
        return hits;
    }

    
    public static Row findMatch(Row findMe, List<Row> rowList, int buffer)
    {
        if(findMe == null || rowList == null) return null;
        
        Row retVal = null;        
        for(Row r : rowList)
        {
            if(r == null || r.isProcessed()) continue;
            if(compareWithBuffer(findMe, r, buffer))
            {
                retVal = r;
                break;
            }
        }
        
        return retVal;
    }
    
    public static boolean compareWithBuffer(Row a, Row b, int buffer)
    {
        boolean retVal = false;
        if(compareTwoRows(a, b, buffer))
        {
            retVal = true;
        }
        else 
        {
            // make sure we have at least a 1 minute buffer for our near compare
            if(buffer < 60) 
                buffer = DEFAULT_TIME_BUFFER;
            
            for(int i = 1; i <= 10; i++)
            {
                if(i > 5 && buffer * i > 900) break; // just stop after 15 minutes...that's too long
                if(nearCompareOfTwoRows(a, b, buffer * i) > 0)
                {
                    retVal = true;
                    break; 
                }
            }
        }        

        return retVal;
    }
    
    public boolean compareTwoRows(Row AAA, Row BBB)
    {
        return compareTwoRows(AAA, BBB, m_timeBuffer);
    }
    public static boolean compareTwoRows(Row AAA, Row BBB, int buffer)
    {
        if(AAA == null || BBB == null) return false;
        
        List<Cell> A = AAA.getRow();
        List<Cell> B = BBB.getRow();        
        if(A == null || B == null) return false;
        
        Cell[] a = new Cell[A.size()];
        Cell[] b = new Cell[B.size()];                
        a = A.toArray(a);
        b = B.toArray(b);
        
        boolean retVal = true;
        
        // test 1: check whether the number of cells (columns) in the rows are equal
        int rowLen = a.length;
        if(a.length != b.length)
        {
            if(b.length < a.length) 
                rowLen = b.length; // make sure we only iterate over smallest array size
            
            retVal = false;
        }

        
        // iterate over the cells of the row
        int hits = 0;
        for(int j = 0; j < rowLen; j++)
        {
            Cell stopA = a[j];
            Cell stopB = b[j];
            
            // test 2: make sure that we've got some data here to deal with
            if(stopA == null || stopB == null) continue;

            // test 3: see if the times are the same or not
            int timeDiff = Math.abs(stopA.getTime() - stopB.getTime());
            if(timeDiff > buffer) 
            {
                retVal = false; // one time is not a match
                break;          // no need to process anymore
            }
            
            hits++;
        }
        
        if(hits == 0) {
            retVal = false;
        }
        
        return retVal;
    }


    public static boolean compareAndMarkRows(List<Cell> A, List<Cell> B, int timeBuffer)
    {
        if(A == null && B == null) return true;
        if(A == null || B == null) return false;
        
        Cell[] a = new Cell[A.size()];
        Cell[] b = new Cell[B.size()];                
        a = A.toArray(a);
        b = B.toArray(b);
        
        boolean retVal = true;
        
        // test 2: check whether the number of cells (columns) in the rows are equal
        int rowLen = a.length;
        if(a.length != b.length)
        {
            if(b.length < a.length) 
                rowLen = b.length; // make sure we only iterate over smallest array size
            
            retVal = false;
        }

        
        // iterate over the cells of the row
        for(int j = 0; j < rowLen; j++)
        {
            Cell stopA = a[j];
            Cell stopB = b[j];
            
            // test 3: make sure that we've got some data here to deal with
            if(stopA == null && stopB == null) continue;
            if(stopA == null)
            {
                stopB.setHighlighted(true);
                retVal = false;
                continue;
            }
            if(stopB == null)
            {
                stopA.setHighlighted(true);
                retVal = false;
                continue;
            }

            // test 4: see if the times are the same or not (if so, set a highlighted flag)                
            int timeDiff = Math.abs(stopA.getTime() - stopB.getTime());
            if(timeDiff > timeBuffer) 
            {
                stopA.setHighlighted(true);
                stopB.setHighlighted(true);
                retVal = false;
            }
        }
        
        return retVal;
    }
    
    // fix the columns and the rows, so that like timepoints match
    public static void fix(TimesTable tA, TimesTable tB)
    {
        if(tA == null || tB == null) return;
        if(tA.getTimePoints() == null || tA.getTimePoints().size() < 1) return;
        if(tB.getTimePoints() == null || tB.getTimePoints().size() < 1) return;
        
        List<Stop> template = new ArrayList<Stop>();
        template.addAll(tA.getTimePoints());
        
        List<Stop> bCols = tB.getTimePoints();
        int tempIndex = 0;
        boolean changes = false;
        for(int i = 0; i < bCols.size(); i++)
        {
            Stop b = bCols.get(i);
            
            Integer tmp = StopImpl.findColumnFromLeft(template, b.getPlaceId(), tempIndex);
            if(tmp != null)
            {
                tempIndex = tmp;
            }
            else
            {
                tempIndex++;
                if(tempIndex < template.size())
                    template.add(tempIndex, b);
                else
                    template.add(b);
                
                changes = true;
            }
        }
        
        if(changes)
        {
            fixCells(tA, template);
            fixCells(tB, template);
        }        
    }

    // add NULL cells into the Row, so that times line up with new TimePoint listing
    public static void fixCells(TimesTable tt, List<Stop> template)
    {
        List<Stop> tp = tt.getTimePoints(); 
        
        for(int tempIndex = 0, i = 0; i < tp.size(); i++, tempIndex++)
        {
            Stop c = tp.get(i);

            Integer tmp = StopImpl.findColumnFromLeft(template, c.getPlaceId(), tempIndex);
            if(tmp == null || tmp > template.size())
                continue; // problems if you get here
            
            if(tmp > tempIndex)
            {
                int numNewCells = tmp - tempIndex;               
                for(Row r : tt.getTimeTable())
                {
                    List<Cell> cells = r.getRow();
                    for(int k = 0; k < numNewCells; k++)
                    {
                        if(tempIndex < cells.size())
                            cells.add(tempIndex, null);
                        else
                            cells.add(null);
                    }
                }
                tempIndex = tmp;                
            }
        }
        
        // and of course, set the new list of timepoints
        tt.setTimepoints(template);
    }
}
