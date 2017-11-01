/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.trimet.ttpub.schedule.trans;

import java.util.ArrayList;
import java.util.List;

import org.timetablepublisher.configure.MergeTPNotes.MergeRule;
import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.schedule.ScheduleDataQueryImpl;
import org.timetablepublisher.schedule.TtTrip;
import org.timetablepublisher.schedule.TtTripImpl;
import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.CellImpl;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.RouteDescription;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.IntUtils;
import org.trimet.ttpub.db.Trip;


/**
 * The purpose of TransDataDataQuery is to satisfy the ScheduleDataQuery interface for TRANS query.  
 * This is primarily a wrapper around queries to TriMet's TRANS data, used by the Configuration routines.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 21, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0 extends ScheduleDataQueryImpl
 */
public class TransDataQueryImpl extends ScheduleDataQueryImpl implements ScheduleDataQuery
{
    protected final TimesTable m_tt;

    public TransDataQueryImpl(TimesTable tt)
    {
        m_tt = tt;
    }
    
    public String getLocationDescription(String agency, String stopID)
    {
        return TransQueryUtils.getLocationDescription(stopID);
    }
    
    public List<String> findTripsByStopID(TimesTable tt, String stopId, PositionType position)
    {
        return TransQueryUtils.findTripsByStopID(tt, stopId, position);
    }

    public List<Row> getSchedule(List<Stop> stops, String agency, String route, DirType dir, KeyType key, List<String> actSvcKeys, boolean bypassConfig, String date)
    {
        return TransQueryUtils.getSchedule(m_tt, route, dir, date, stops, IntUtils.toSQLString(actSvcKeys));
    }

    public List<Cell> getTimesByRoute(List<Stop> stops, String agency, String route, DirType dir, KeyType key, List<String> actSvcKeys, boolean bypassConfig, String date)
    {
        return TransQueryUtils.getTimesByRoute(stops, route, dir, date, IntUtils.toSQLString(actSvcKeys));
    }
    public List<Cell> getTimesByStopID(TimesTable tt, String stop, boolean fromRight)
    {
        return TransQueryUtils.getTimesByStopId(stop, tt);
    }
    public List<Cell> getTimesByStopID(TimesTable tt, String stop, boolean fromRight, String symbol, MergeRule rule)
    {
        List<Cell> retVal = TransQueryUtils.getTimesByStopId(stop, tt);
        
        // special rule (hack)
        // if this looks like a "Meet" rule, then filter out times that are not really part of the Meet
        // btw, Meets happen downtown, where the buses connect downtown at early morning / late evening hours 
        if(retVal != null && rule == MergeRule.FORCE && symbol != null && symbol.equals("A"))
        {
            retVal = CellImpl.filterByTime(retVal);
        }
        
        return retVal;
    }

    public List<Cell> getTimesByStopID(TimesTable tt, String intRouteID, DirType intDir, String stopID)
    {
        return TransQueryUtils.getTimesByStopId(stopID, intRouteID, intDir, tt.getDate(), TransQueryUtils.getSQLKeyString(tt));
    }    

    public List<TtTrip> getTrips(String agency, String route, DirType dir, KeyType key, List<String> actSvcKeys, boolean bypassConfig, String date)
    {
        List<Trip> trips  = TransQueryUtils.getTrips(route, dir, date, IntUtils.toSQLString(actSvcKeys));
        List<TtTrip>   retVal = new ArrayList<TtTrip>();
        for(Trip t : trips)
        {
            if(t == null) continue;
            if(t.getTripBeginTime() == null || t.getTripEndTime() == null) continue;            
            retVal.add(new TtTripImpl(t.getId().getTripNumber(), t.getTrain(), t.getTripBeginTime(), t.getTripEndTime()));
        }

        return retVal;
    }

    public List<RouteDescription> getRouteNames(String date)
    {
        return TransQueryUtils.getRouteNames(date);
    }

    public RouteDescription getRouteDirectionName(TimesTable tt)
    {
        return TransQueryUtils.getRouteDirectionName(tt); 
    }

    public List<Stop> queryTimePoints(String agencyName, String routeID, DirType dir, KeyType key, List<String> activeServiceKeys, boolean bypassConfig, String date)
    {
        return TransQueryUtils.getTimePoints(routeID, dir, key, date);
    }
    
    public Stop makeStop(ConfigurationLoader loader, String agency, String stopID, String placeID, Integer seq)
    {
        Stop retVal = TransQueryUtils.makeStop(stopID, seq);
        if(retVal != null)
        {
            retVal.setStopId(stopID);
            retVal.setPlaceId(stopID);
        }
        return retVal;
    }
    
    public void setServiceDates(TimesTable thisTT)
    {
        TransQueryUtils.setServiceDates(thisTT);
    }        
}
