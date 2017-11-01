/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.timetablepublisher.configure.MergeTPNotes.MergeRule;
import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.schedule.ScheduleDataQueryImpl;
import org.timetablepublisher.schedule.TtTrip;
import org.timetablepublisher.schedule.TtTripImpl;
import org.timetablepublisher.schedule.gtfs.loader.GTFSDataLoader;
import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.RouteDescription;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;

/**
 * The purpose of DataQuery is to provide a generic interface for accessing raw schedule data,
 * whether that be from CSV file and/or a database.  The primary user of this interface will
 * be from those configure routines that require additional schedule data.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 21, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 * @see     http://code.google.com/transit/spec/transit_feed_specification.htm
 */
public class GTFSDataQueryImpl extends ScheduleDataQueryImpl implements ScheduleDataQuery 
{
    private static final Logger LOGGER = Logger.getLogger(GTFSDataQueryImpl.class.getCanonicalName());    
    
    protected final GTFSDataLoader m_loader;
    
    public GTFSDataQueryImpl(GTFSDataLoader loader)
    {
        m_loader = loader;
    }
    
    public String getScheduleDataDir()
    {
        if(m_loader == null) {
            return super.getScheduleDataDir();
        }
        
        return m_loader.getCsvDir();
    }

    
    public List<Row> getSchedule(List<Stop> stops, String agency, String route, DirType dir, KeyType key, List<String> actSvcKeys, boolean bypassConfig, String date)
    {
        return GTFSDataUtils.getSchedule(m_loader, agency, route, dir, key, date, stops, actSvcKeys, bypassConfig);
    }

    public List<Cell> getTimesByRoute(List<Stop> stops, String agency, String route, DirType dir, KeyType key, List<String> actSvcKeys, boolean bypassConfig, String date)
    {
        LOGGER.warning("GTimesTable: feature unimplemented.");
        return null;
    }
    
    public String getLocationDescription(String agency, String stopID)
    {
        Stops s = Stops.getStop(agency, stopID, m_loader);
        if(s == null) return null;
        
        return s.getName(); 
    }    

    public Stop makeStop(ConfigurationLoader loader, String agency, String stopID, String placeID, Integer seq)
    {
        Stop retVal = GTFSDataUtils.makeColumn(m_loader, agency, stopID, seq);
        if(retVal != null)
        {
            retVal.setStopId(stopID);
            retVal.setPlaceId(placeID);
        }
        return retVal;
    }

    public List<Cell> getTimesByStopID(TimesTable tt, String stopID, boolean fromRight)
    {
        return GTFSDataUtils.getTimesByStopID(m_loader, tt, stopID, fromRight);
    }
    public List<Cell> getTimesByStopID(TimesTable tt, String intRouteID, DirType intDir, String stopID)
    {
        return GTFSDataUtils.getTimesByStopID(m_loader, tt, intRouteID, intDir, stopID, false);
    }
    public List<Cell> getTimesByStopID(TimesTable tt, String stop, boolean fromRight, String symbol, MergeRule rule)
    {
        return getTimesByStopID(tt, stop, fromRight);
    }

    
    public List<String> findTripsByStopID(TimesTable tt, String stopID, PositionType position)
    {
        return GTFSDataUtils.findTripIDsByTT(m_loader, tt, stopID, position);
    }

    public List<TtTrip> getTrips(String agency, String route, DirType dir, KeyType key, List<String> actSvcKeys, boolean bypassConfig, String date)
    {
        List<TtTrip> retVal = new ArrayList<TtTrip>();
        List<Trips>  trips = GTFSDataUtils.findTrips(m_loader, agency, route, dir, key, actSvcKeys, bypassConfig, date);
        if(trips != null)
        {
            // List<Index> stopTimeList = loader.getData(agency, StopTimes.class);
            for(Trips t : trips)
            {
                if(t == null) continue;
                
                // TODO: FIX ME -- find start & end times of this trip
                // List<StopTimes> tripTimes = getTripTimes(stopTimeList, t.getTrip());
                // tripTimes.get(0).getValidTime();  trips.get(trips.size() - 1).getValidTime();
                retVal.add(new TtTripImpl(t.getTripID(), t.getBlock(), 0, 0));
            }
        }
        return retVal;
    }

    public RouteDescription getRouteDirectionName(TimesTable tt)
    {
        return GTFSDataUtils.getRouteDirectionName(m_loader, tt);
    }

    public List<RouteDescription> getRouteNames(String date)
    {
        return GTFSDataUtils.getRouteNames(m_loader, date);
    }

    public List<Stop> queryTimePoints(String agencyName, String routeID, DirType dir, KeyType key, List<String> activeServiceKeys, boolean bypassConfig, String date)
    {
        return GTFSDataUtils.getRouteStops(m_loader, agencyName, routeID, dir, key, activeServiceKeys, bypassConfig, date, false);
    }

    public String getRouteIDForList(String agencyName, String routeID)
    {
        return GTFSDataUtils.getRouteIDForList(agencyName, routeID);
    }
}
