/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule;

import java.util.List;

import org.timetablepublisher.configure.MergeTPNotes.MergeRule;
import org.timetablepublisher.configure.loader.ConfigurationLoader;
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
 * CAUTION: this design can be the source of some really tricky bugs.  For example: in the ES_ 
 *          timestable, there is a special query.makeColumn() routine.  There is also a catch-all routine
 *          defined within ScheduleDataQueryImpl.  Well, if you change the parameters on the ES's makeColumn,
 *          without chaning the interface and/or the generic version, the specific routine (in this case ES's 
 *          makeColumn()) will be obscured, and it will be very hard to find unless you know that ES
 *          overrides a routine without chaning the interface.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 21, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public interface ScheduleDataQuery
{
    public enum PositionType {FIRST, LAST, EXISTS}
    
    void setServiceDates(TimesTable thisTT);    
    List<Row>  getSchedule(TimesTable tt);
    List<Row>  getSchedule(List<Stop> stops, String agency, String routeID, DirType dir, KeyType key, List<String> activeKeys, boolean bypassConfig, String date);
    List<Cell> getTimesByRoute(List<Stop> stops, String agencyName, String routeID, DirType opposite, KeyType key, List<String> activeServiceKeys, boolean bypassConfig, String date);
    List<Cell> getTimesByStopID(TimesTable tt, String stop, boolean fromRight);
    List<Cell> getTimesByStopID(TimesTable tt, String stop, boolean fromRight, String symbol, MergeRule rule);
    List<Cell> getTimesByStopID(TimesTable tt, String intRoute, DirType intDir, String stopID);

    
    /** The makeColumn routine will have to query data via a Stop ID to build a Column */ 
    Stop       makeStop(ConfigurationLoader loader, String agency, String stopID, String placeID, Integer seq);    
    List<Stop> queryTimePoints(String agencyName, String routeID, DirType dir, KeyType key, List<String> activeServiceKeys, boolean bypassConfig, String date);
    
    /** often, a PlaceID is just a psudonom for StopID -- but some scheduling systems have PlaceIDs, which map to one or more StopIDs */ 
    String     toPlaceId(ConfigurationLoader loader, String agency, String stopID);
    String     getLocationDescription(String agency, String stopID);
    
    List<TtTrip>   getTrips(TimesTable tt);
    List<TtTrip>   getTrips(String agency, String routeID, DirType dir, KeyType key, List<String> activeKeys, boolean bypassConfig, String date);
    List<String>   findTripsByStopID(TimesTable tt, String stopID, PositionType position);
    
    List<RouteDescription> getRouteNames(String date);
    String                 getRouteIDForList(String agencyName, String routeID);
    RouteDescription       getRouteDirectionName(TimesTable tt);
    
    // something that applies to tables that get their data from flat files (eg: GTimesTable) 
    String getScheduleDataDir();
}