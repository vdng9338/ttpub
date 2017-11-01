/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import java.util.Comparator;

import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;

public interface Cell
{
    DirType getDir();
    void setDir(DirType dir);

    KeyType getServiceKey();
    void setServiceKey(KeyType key);

    // the getServiceKey() returns a 'base' service key...this will return the specific key that roll into the base
    String getRawSvcKey();
    void   setRawSvcKey(String string);
    
    String getStopId();
    void setStopId(String stop);

    void setTime(int time);
    Integer getTime();
    String getTimeAsStr();
    String getHour();
    String getMinutes();
    String getTensOfMinutes();
    
    String getBlock();
    void setBlock(String block);

    String getTrip();
    void setTrip(String trip);

    String getTripType();
    void setTripType(String tripType);
    
    String getFootnoteSymbol();
    void setFootnoteSymbol(String footnoteSymbol);
    
    boolean isHighlighted();
    void setHighlighted(boolean highlighted);
    
    boolean isProcessed();
    void setProcessed(boolean processed);
        
    static public class Compare implements Comparator<Cell>
    {
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
        public int compare(Cell c1, Cell c2)
        {
            if(c1 == null || c2 == null)                     return  0;
            if(c1.getTime() == null || c2.getTime() == null) return  0;
            
            if(c1.getTime() == c2.getTime()) return  0;
            if(c1.getTime() >  c2.getTime()) return  1;
            else                             return -1;
        }
    }
}
