/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule;

import java.util.List;

import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;


/**
 * The purpose of ScheduleDataQueryImpl is to provide a set of catch-all methods in support of the 
 * generic ScheduleQuery inteface.  Each TimesTable instance will also be defining some set of methods
 * that over-ride these catch-all's defined here.
 * 
 * CAUTION: this design can be the source of some really tricky bugs.  For example: in the query for the ES_ 
 *          timestable, there is a special query.makeColumn() routine.  There is also the catch-all routine
 *          defined here.  Well, if you change the parameters on the ES's makeColumn without chaning the 
 *          interface and/or the generic version here, the specific routine (in this case ES's 
 *          makeColumn()) will be obscured, and it will be very hard to find out why your code suddnly stopped
 *          working -- unless of course you know that the ES overrides a routine without chaning the interface.
 *          (I wrote the code only a month ago...and just today spent many hours trying to find out why things
 *          broke from the condition above...hopefully this NOTE will help you not to repeat this).
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 21, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public abstract class ScheduleDataQueryImpl implements ScheduleDataQuery
{
    /** often, a PlaceID is just a psudonom for StopID -- but some scheduling systems have PlaceIDs, which map to one or more StopIDs */    
    public String toPlaceId(ConfigurationLoader loader, String agency, String stopId)
    {
        return stopId;
    }

    /** only those tables that get their data from files (eg: GTimesTable) will override this routine */
    public String getScheduleDataDir()
    {
        return ".";
    }
    
    public List<Row> getSchedule(TimesTable tt)
    {
        return getSchedule(tt.getTimePoints(), tt.getAgencyName(), tt.getRouteID(), tt.getDir(), tt.getKey(), tt.getActiveServiceKeys(), tt.bypassConfig(), tt.getDate());
    }

    public List<TtTrip> getTrips(TimesTable tt)
    {
        if(tt == null) return null;
        return getTrips(tt.getAgencyName(), tt.getRouteID(), tt.getDir(), tt.getKey(), tt.getActiveServiceKeys(), tt.bypassConfig(), tt.getDate());
    }
    
    public Stop makeStop(ConfigurationLoader loader, String agency, String stopID, Integer sequence)
    {
        String place = stopID;
        String desc  = stopID;
        int    seq   = 111;
        if(sequence != null) 
            seq = sequence; 
        
        Stop retVal = new StopImpl(stopID, place, seq, desc);        
        return retVal;
    }
        
    public String getLocationDescription(String agency, String stopID)
    {
        return "unimplemented routine -- no location name for stop #" + stopID; 
    }

    public String getRouteIDForList(String agencyName, String routeID)
    {
        return routeID;
    }
    
    public void setServiceDates(TimesTable thisTT)
    {        
    }
}
