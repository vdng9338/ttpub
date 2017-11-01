/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import java.util.Comparator;
import java.util.List;

import org.timetablepublisher.table.Row.Compare.TimeAndPos;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.utils.IntUtils;

public interface Row
{
    Cell getCell(int i);
    void setCell(int i, Cell input);
    Cell removeCell(int index);    
    boolean updateCell(int i, Cell input, boolean overwrite);

    int getLen();
    List<Cell> getRow();
    void setRow(List<Cell> row);

    int getTime(int i);
    String getTimeAsStr(int i);

    String  getRoute();
    DirType getDirection();
    String  getDate();
    String  getTrip();
    String  getBlock();
    String  getTripType();
    String  getRawSvcKey();
    void    setTrip(String t);
    void    setBlock(String b);
    void    setTripType(String t);
    int     getTripLength();
    
    void    setFootnoteSymbol(String s);
    void    setFootnoteSymbol(String s, boolean allRows);
    String  getFootnoteSymbol(int i);
    String  getFootnoteSymbol();    
    boolean isHighlighted(int i);

    void clearProcessedFlag();
    void clearProcessedFlag(List<Row> schedule);
    
    public Cell getFirstNonNullCell();
    public Cell getLastNonNullCell();
    public Cell getNextNonNullCell(int y);
    public Cell getPrevNonNullCell(int y);
    
    public Cell findStopTime(String stopId);
    
    boolean isProcessed();
    void setProcessed(boolean processed);

        
    static public class Compare implements Comparator<Row>
    {
        private int m_numberOfColumns = 0;

        public Compare(int cols)
        {
            super();
            m_numberOfColumns = cols;
        }

        //  compare
        //
        //  public int compare(java.lang.Object o1, java.lang.Object o2)
        //
        //      Return an integer that is negative, zero or positive depending on whether the first argument 
        //      is less than, equal to or greater than the second according to this ordering. This method 
        //      should obey the following contract:
        //          * if compare(a, b) < 0 then compare(b, a) > 0
        //          * if compare(a, b) throws an exception, so does compare(b, a)
        //          * if compare(a, b) < 0 and compare(b, c) < 0 then compare(a, c) < 0
        //          * if compare(a, b) == 0 then compare(a, c) and compare(b, c) must have the same sign
        //
        //      To be consistent with equals, the following additional constraint is in place:
        //          * if a.equals(b) or both a and b are null, then compare(a, b) == 0.
        //
        //      Although it is permissible for a comparator to provide an order inconsistent with equals, 
        //      that should be documented. 
        //
        public class TimeAndPos
        {
            public int lastPos   = 0;
            public int lastTime  = 0;
            public int firstPos  = 0;
            public int firstTime = -1;
            public boolean hasPrevTime = false;
            
            public void addTime(int time, int pos)
            {
                if(time >= 0)
                {
                    if(firstTime < 0)
                    {
                        firstTime = time;
                        firstPos  = pos;
                    }
                    lastTime = time;
                    lastPos  = pos;
                    hasPrevTime = true;
                }
                else
                    hasPrevTime = false;
            }
            
            final int STOP_DURR = 5 * 60;  // SECONDS
            public int getDurr(TimeAndPos other)
            {
                return STOP_DURR * (other.firstPos - this.lastPos); 
            }
            public int largerDurration(TimeAndPos other)
            {
                int durr = this.getDurr(other);
                if(this.lastTime + durr <= other.firstTime)
                    return -1;
                else
                    return  1;                
            }
            public int whichIsLarger(TimeAndPos other)
            {
                // alright, this ends before the other begins
                if(this.lastPos < other.firstPos)
                {
                    return this.largerDurration(other);
                }                            
                // well then, the other ends before the this begins
                else
                {
                    return other.largerDurration(this) * -1;                    
                }            
            }
        }
        
        public int compare(Row row1, Row row2)
        {
            TimeAndPos rowA = new TimeAndPos(); 
            TimeAndPos rowB = new TimeAndPos(); 
            
            // test 1: find a column that has times for both rows & compare
            for(int i = 0; i < m_numberOfColumns; i++)
            {
                int timeA = row1.getTime(i);
                int timeB = row2.getTime(i);

                // initial check -- same column in time table
                if(timeA >= 0 && timeB >= 0)
                {
                    if(timeA == timeB)
                    {
                        // promote times that have prev column time ahead of those that don't
                        if(rowA.hasPrevTime) return  1;
                        if(rowB.hasPrevTime) return -1;
                        
                        return  0;
                    }
                    if(timeA > timeB)  return  1;
                    if(timeA < timeB)  return -1;
                }
                
                rowA.addTime(timeA, i);
                rowB.addTime(timeB, i);
            }

            return rowA.whichIsLarger(rowB);
        }
    }
    
    static public class TripSort implements Comparator<Row>
    {
        public int compare(Row rowA, Row rowB)
        {
            String a = rowA.getTrip();
            String b = rowB.getTrip();
            if(a == null) a = "Z";
            if(b == null) b = "Z";            
            return a.compareTo(b);
        }        
    }    

    static public class BlockSort implements Comparator<Row>
    {
        public int compare(Row rowA, Row rowB)
        {
            if(rowA == null) return  1;
            if(rowB == null) return -1;
            
            String a = rowA.getBlock();
            String b = rowB.getBlock();            
            if(a == null) return  1;
            if(b == null) return -1;
            
            return a.compareTo(b);
        }        
    }
}


// NOTE: if here, test #1 failed (eg: didn't *return*), so
/*            
// test 2:  find a column that has times for both rows & compare
for(int i = 0; i < m_numberOfColumns; i++)
{
    int time1 = row1.getTime(i);
    int time2 = row2.getTime(i);

    if(time1 >= 0)
    {
        // now we'll compare this column time for row1 with latter columns of row2
        // if any of those row2 times are smaller, than we know that row1 occurs later than row2
        for(int j = i+1; j < m_numberOfColumns; j++)
        {
            time2 = row2.getTime(j);
            if(time2 >= 0)
            {
                if(time1 > time2) 
                {
                    return 1;
                }
            }
        }

        // now we'll compare this column time for row1 with 'earlier' columns of row2
        // if any of those row2 times are larger, than we know that row1 occurs earlier than row2
        for(int j = i-1; j >= 0; j--)
        {
            time2 = row2.getTime(j);
            if(time2 >= 0)
            {
                if(time1 < time2) 
                {
                    return -1;
                }
            }
        }
    }
}
*/
/*
if(aLastPos > bLastPos)
{
    if(aLastPos > bLastTime + durr)
        return 1;
    else
        return -1;
}
else
{
    if(bLastPos > aLastTime + durr)
        return -1;
    else
        return  1;                
}
}

/*            
int t1 = IntUtils.getIntegerFromString(row1.getTrip(), 1);
int t2 = IntUtils.getIntegerFromString(row2.getTrip(), 1);
if(t1 > t2)
    return 1;

return 0;
*/            
