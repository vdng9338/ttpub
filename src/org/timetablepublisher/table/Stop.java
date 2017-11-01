/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import java.util.Comparator;
import java.util.List;

/**
 * Column is the data representation of the stops (or timepoints) in a TimeTable.  Following the Table motif,
 * these are usually shown as columns in a time-table.  That said, a view with stops a rows (with columns of times)
 * can also be rendered by TTPUB -- so Column is a bit-misnamed.  
 *  
 * NOTE: placeID and stopID will be identical, unless a constructor that accepts a placeID is used.  Place ID
 *       is more an internal ID for a stop, whereas stopID would be the public ID (in instances where there are
 *       multiple ID sets -- eg: at TriMet, our scheduling software has a different ID than our public stop IDs,
 *       so the placeID is used for when we tie TTPUB up to our raw scheduling data). 
 *        
 * 
 * @author  Frank Purcell
 * @version 1.0 
 */
public interface Stop
{
    public String  getUrl();
    
    public boolean isRepeated();
    public void    setRepeated(boolean repeated);
    
    public boolean isProcessed();
    public void setProcessed(boolean printable);

    public boolean isPublic();
    public void setPublic(boolean pub);
    public void setPublic(Character passengerAccessCode);

    public boolean hideStopId();
    public void setHideStopId(boolean hide);
    
    public String getStopId();
    public void setStopId(String stop);

    public String getPlaceId();
    public void setPlaceId(String place);
    
    public String getDescription();
    public void   setDescription(String description);
    public String getDescription(List<Stop> cList, String stopID);
    
    public Integer getSequence();
    public void    setSequence(Integer sequence);
    
    public Double  getLatitude();
    public Double  getLongitude();
    public void    setLatitude(Double latitude);
    public void    setLongitude(Double longitude);

    public Stop findTimePoint(List<Stop> cList, String stopID);
    
    static public class Compare implements Comparator<Stop>
    {
        //  compare
        //
        //  public int compare(java.lang.Object o1, java.lang.Object o2)
        //
        //      Return an integer that is negative, zero or positive depending on whether the first argument 
        //      is less than, equal to or greater than the second
        //
        public int compare(Stop c1, Stop c2)
        {
            if(c1 == null || c2 == null)             return  0;
            if(c1.getSequence() == c2.getSequence()) return  0;
            if(c1.getSequence() >  c2.getSequence()) return  1;
            else                                     return -1;
        }
    }
}
