/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.trimet.ttpub.schedule.trans;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.exception.DataException;

import org.timetablepublisher.configure.ActiveServiceKeys;
import org.timetablepublisher.configure.LoopFillIn;
import org.timetablepublisher.configure.SvcKeyNormalizer;
import org.timetablepublisher.schedule.ScheduleDataQuery.PositionType;
import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.CellImpl;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.RouteDescription;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.RowImpl;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.IntUtils;
import org.timetablepublisher.utils.KeyValue;
import org.trimet.ttpub.db.*;

/**
 * the purpose of this class is to provide Hibernate Queries to TriMet's Enterprise Database
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @version 1.0
 * @date    Oct 18, 2006
 * @since   1.0
 * @see     
 */
@SuppressWarnings("unchecked")
public class TransQueryUtils implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(TransQueryUtils.class.getCanonicalName());
        
    static final int fiveMinutes  = 60 * 5;
    static final int eightMinutes = 60 * 8;
    static final int tenMinutes   = 60 * 10;

    public static List<RouteDescription> getRouteNames(String date)
    {
        List<RouteDescription> retVal = null;

        try
        {
            // STEP A: CONNECT TO THE DATABASE & START A SESSION
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "route names query start: " + new Date().getTime());
            Session session = TransHibernateUtil.getSession();

            // STEP B: QUERY THE DATABASE   FOR ROUTE NAME & NUMBER
            Query routeQ = session.createQuery(
                    " select new org.timetablepublisher.table.RouteDescriptionImpl(r.publicRouteDescription, r.id.routeNumber) " + 
                    " from Route r " +
                    " where        " +
                    "  TO_DATE(:date, 'MM-DD-YYYY') between r.id.routeBeginDate and r.routeEndDate " +                                 
                    "  and r.routeUsage = 'R'                               " +
/*
  1-15-2008: COMMENTED OUT FOR PERF REASONS 
  W/Join to Trip Table, /Q runs at 30 seconds
  
                    "  and EXISTS (                                         " +
                    "      SELECT 1 FROM Trip t                             " +
                    "      WHERE t.id.routeNumber = r.id.routeNumber        " +
                    "        and TO_DATE(:date, 'MM-DD-YYYY') between t.id.tripBeginDate and t.tripEndDate " +
                    "      )                    " + 
*/
                    " order by r.routeSortOrder "
            );
            routeQ.setString("date", date);
            
            // STEP C: CALL DB QUERY to get LIST of RESULTS
            retVal = routeQ.list();
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "route names query end: " + new Date().getTime());
        }
        catch(DataException e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n" + e.getSQL(), e);
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n", e);
        }

        return retVal;
    }
    
    
    // 
    // ROUTE & DIRECTION NAME
    // 
    public static RouteDescription getRouteDirectionName(TimesTable tt)
    {
        Integer route     = IntUtils.getIntFromString(tt.getRouteID());
        Integer direction = tt.getDir().value();
        String  date      = tt.getDate();
        if(route == null) return null;
        
        RouteDescription retVal = null;
        
        try
        {
            // STEP A: CONNECT TO THE DATABASE & START A SESSION
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "route direction name query start: " + new Date().getTime());
            Session session = TransHibernateUtil.getSession();
            
            // STEP B: QUERY THE DATABASE FOR ROUTE & DIRECTION NAME
            Query routeQ = session.createQuery(
                    "select new org.timetablepublisher.table.RouteDescriptionImpl(r.publicRouteDescription, rd.publicDirectionDescription, r.id.routeNumber) " +
                    "from Route r, RouteDirection rd                  " + 
                    "where r.id.routeNumber    = rd.id.routeNumber    " +
                    "  and r.id.routeBeginDate = rd.id.routeBeginDate " +
                    "  and r.id.routeNumber    = :route               " +
                    "  and rd.id.dir           = :direction           " +
                    "  and TO_DATE(:date, 'MM-DD-YYYY') between r.id.routeBeginDate and r.routeEndDate "
            );
            routeQ.setInteger("route",     route);
            routeQ.setInteger("direction", direction);
            routeQ.setString ("date",      date);
            
            // STEP C: CALL DB QUERY to get a UNIQUE (or at least it should be -- return first) RESULT        
            retVal = (RouteDescription) routeQ.list().get(0);
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query end: " + new Date().getTime());
        }
        catch (DataException e)
        {
            LOGGER.log(DEBUG, " route: " + route + " dir: " + direction + " date: " + date + "\n" + e.getSQL(), e);
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n", e);
        }

        return retVal;
    }


    /**
     * Get service dates from db, and assign them to our table
     */
    public static void setServiceDates(TimesTable tt)
    {
        try
        {
            // STEP A: CONNECT TO THE DATABASE & START A SESSION
            LOGGER.log(DEBUG, "effective date query start");
            Session session = TransHibernateUtil.getSession();

            // STEP B: QUERY THE DATABASE FOR ALL STOPS
            Query timesQ = session.createQuery(
                " from   Trip t                                             " +
                " where  TO_DATE(:date, 'MM-DD-YYYY') between t.id.tripBeginDate and t.tripEndDate " +
                "  and   :route           =  t.id.routeNumber               " +
                "  and   :direction       =  t.id.dir                       " +
                "  and   t.id.serviceKey in (" + getSQLKeyString(tt) + ")   "
            );

            timesQ.setString ("route",      tt.getRouteID());
            timesQ.setInteger("direction",  tt.getDir().value());
            timesQ.setString ("date",       tt.getDate());
            
            // STEP C: CALL DB QUERY to get LIST of RESULTS
            List l  = timesQ.list();
            Trip ed = (Trip)l.get(0);
            tt.setServiceDates(ed.getId().getTripBeginDate(), ed.getTripEndDate());
            
            LOGGER.log(DEBUG, "effective date query end");            
        }
        catch(DataException e)
        {
            LOGGER.log(DEBUG, " route: " + tt.getRouteID() + " dir: " + tt.getDir() + " date: " + tt.getDate() + "\n" + e.getSQL(), e);
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n", e);
        }
        finally
        {
        }
    }    
    
    
    public static List<Trip> getTrips(TimesTable tt)
    {
        if(tt == null) return null;        
        return getTrips(tt.getRouteID(), tt.getDir(), tt.getDate(), getSQLKeyString(tt));
    }
    public static List<Trip> getTrips(String route, DirType dir, String date, String keySQLString)
    {
        List<Trip> retVal = null;        
        try
        {
            // STEP A: CONNECT TO THE DATABASE & START A SESSION
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query start: " + new Date().getTime());            
            Session session = TransHibernateUtil.getSession();

            // STEP B: QUERY THE DATABASE FOR ALL STOPS
            Query timesQ = session.createQuery(
                    " from   Trip t                                       " +
                    " where  TO_DATE(:date, 'MM-DD-YYYY') between t.id.tripBeginDate and t.tripEndDate " +
                    "  and   :route           =  t.id.routeNumber               " +
                    "  and   :direction       =  t.id.dir                       " +
                    "  and   t.id.serviceKey in (" + keySQLString + ")   "
            );            
            timesQ.setString ("route",      route);
            timesQ.setInteger("direction",  dir.value());
            timesQ.setString ("date",       date);
            
            // STEP C: CALL DB QUERY to get LIST of RESULTS
            retVal = timesQ.list();
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query end: " + new Date().getTime());            
        }
        catch(DataException e)
        {
            LOGGER.severe(e.getMessage() + "\n" + e.getSQL());
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n", e);
        }
        finally
        {
        }
        
        return retVal;
    }
    
    public static List<KeyValue> getTripsWithStopID(TimesTable tt, String minOrMax)
    {
        if(tt == null) return null;        
        return getTripsWithStopID(tt.getRouteID(), tt.getDir(), tt.getDate(), getSQLKeyString(tt), minOrMax);
    }
    public static List<KeyValue> getTripsWithStopID(String route, DirType dir, String date, String keySQLString, String minOrMax)
    {
        List<KeyValue> retVal = null;        
        try
        {
            // STEP A: CONNECT TO THE DATABASE & START A SESSION
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query start: " + new Date().getTime());
            Session session = TransHibernateUtil.getSession();

            String queryString =  
                 " select new org.timetablepublisher.utils.KeyValue(t.id.tripNumber, l.id.locationId) " +
                 " from   Trip t, StopDistance sd, Location l                       " +
                 " where  t.id.routeNumber     = :route                             " +
                 " and    t.id.dir             = :direction                         " +
                 " and    t.id.serviceKey    in (" + keySQLString + ")              " +
                 " and    TO_DATE(:date, 'MM-DD-YYYY') between t.id.tripBeginDate and t.tripEndDate        " +
                 " and    sd.id.routeNumber    = t.id.routeNumber                   " +
                 " and    sd.id.dir            = t.id.dir                           " +
                 " and    sd.id.routeBeginDate = t.routeBeginDate                   " +
                 " and    sd.id.patternId      = t.patternId                        " +
                 " and    l.locationId         = sd.id.locationId                   " +
                 " and    l.passengerAccessCode <> 'N'                              ";

            if(minOrMax != null)
            {
                queryString +=
                " and    sd.id.stopDistance = (                                    " +
                "         select " + minOrMax + "(b.id.stopDistance)               " +
                "         from  StopDistance b                                     " +
                "         where b.id.routeNumber    = t.id.routeNumber             " +
                "         and   b.id.dir            = t.id.dir                     " +
                "         and   b.id.patternId      = t.patternId                  " +
                "         and   b.id.routeBeginDate = t.routeBeginDate)            ";
            }
            
            // STEP B: QUERY THE DATABASE FOR ALL STOPS
            Query timesQ = session.createQuery(queryString);            
            timesQ.setString ("route",         route);
            timesQ.setInteger("direction",     dir.value());
            timesQ.setString ("date",          date);
            
            // STEP C: CALL DB QUERY to get LIST of RESULTS
            retVal = timesQ.list();
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query end: " + new Date().getTime());
        }
        catch(DataException e)
        {
            LOGGER.log(SEVERE, "uh oh" + "\n" + e.getSQL(), e);
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n", e);
        }
        finally
        {
        }

        return retVal;
    }
    
    /**
     * queries for all trips given a route/direction/key/date, and from that finds those trips that
     * start -or- end with the given target stopID
     * 
     * @param m_tt
     * @param targetStopId
     * @param endingPlace
     * @return list of strings representing trip numbers
     */
    public static List<String> findTripsByStopID(TimesTable tt, String stopId, PositionType position)
    {
        return findTrips(tt, stopId, position);
    }
    public static List<String> findTrips(TimesTable tt, String targetStopId, PositionType position)
    {
        if(tt == null || targetStopId == null) return null;
        
        // step 1: determine if we're looking for the min or max stop (see sql query)
        String minOrMax = null;
        switch(position)
        {
            case FIRST:
                minOrMax = "min";
                break;
            case LAST:
                minOrMax = "max";
                break;
        }
        
        // step 2: get a list of KeyValues, where trips (keys) are paired with first / last stop (value) on trip
        List<KeyValue> trips = getTripsWithStopID(tt, minOrMax);
        if(trips == null) return null;
        
        List<String> retVal = new ArrayList<String>();
        for(KeyValue t : trips)
        {
            if(t == null || t.getKey() == null || t.getValue() == null) continue;
            
            // step 3: assignment when the trip's first/last stop matches the target stop
            if(targetStopId.equals(t.getValue()))
            {
                retVal.add(t.getKey());
            }
        }
        
        // step 4: return list of strings representing trip numbers
        return retVal;        
    }
    
    
    
    public static List<Short> getUniqueTrips(String route, DirType dir, String date, String key1, String key2)
    {
        List<Short> retVal = null;

        try
        {
            // STEP A: CONNECT TO THE DATABASE & START A SESSION
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query start: " + new Date().getTime());
            Session session = TransHibernateUtil.getSession();

            // STEP B: QUERY THE DATABASE FOR ALL STOPS
            Query timesQ = session.createQuery(
                    " select t.id.tripNumber                                        " + 
                    " from Trip t                                                   " + 
                    " where   t.id.routeNumber = :route                             " + 
                    " and     t.id.dir         = :direction                         " + 
                    " and     TO_DATE(:date, 'MM-DD-YYYY') between t.id.tripBeginDate and t.tripEndDate    " + 
                    " and     t.id.serviceKey  = '" + key1 + "'                     " + 
                    " and not t.id.tripNumber in                                    " +
                    " (                                                             " +                    
                    "   select distinct TT.id.tripNumber                            " + 
                    "   from   Trip TT                                              " + 
                    "   where  t.id.routeNumber = TT.id.routeNumber                 " + 
                    "   and    t.id.dir         = TT.id.dir                         " + 
                    "   and    TO_DATE(:date, 'MM-DD-YYYY') between TT.id.tripBeginDate and TT.tripEndDate " + 
                    "   and    TT.id.serviceKey = '" + key2 + "'                    " + 
                    " )                                                             " +
                    " order by t.id.tripNumber                                      "
            );
            timesQ.setInteger("route",     IntUtils.getIntFromString(route));
            timesQ.setInteger("direction", dir.value());
            timesQ.setString("date",       date);

            // STEP C: CALL DB QUERY to get LIST of RESULTS
            retVal = timesQ.list();
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query end: " + new Date().getTime());
        }
        catch(DataException e)
        {            
            LOGGER.log(SEVERE, "Exception caught in generating the schedules (returning empty List): " + e.getLocalizedMessage() + "\n" + e.getSQL(), e);
            retVal = new ArrayList<Short>();
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n", e);
        }
        finally
        {
        }

        return retVal;
    }


    //
    //
    //  S C H E D U L E   R O U T I N E S
    //
    //
    
    
    /**
     * @param m_tt
     * @return a string of the ActiveSvcKeys for this table, formatted for a SQL 'in' clause 
     */
    public static String getSQLKeyString(TimesTable tt)
    {
        return IntUtils.toSQLString(tt.getActiveServiceKeys(), "'W', 'F', 'f'");
    }
    
    public static List<Cell> getTimesByStopId(String stop, String sqlString, TimesTable tt)
    {
        return getTimesByStopId(stop, tt.getRouteID(), tt.getDir(), tt.getDate(), sqlString);
    }    
    public static List<Cell> getTimesByStopId(String stop, TimesTable tt)
    {
        return getTimesByStopId(stop, tt.getRouteID(), tt.getDir(), tt.getDate(), getSQLKeyString(tt));
    }    
    public static List<Cell> getTimesByStopId(String stopId, String route, DirType dir, String date, String keySQLString)
    {
        List<Cell> retVal = null;
        try
        {
            // STEP A: CONNECT TO THE DATABASE & START A SESSION
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query start: " + new Date().getTime());            
            Session session = TransHibernateUtil.getSession();

            // STEP B: QUERY THE DATABASE FOR ALL STOPS
            Query timesQ = session.createQuery(
                      "select distinct new org.timetablepublisher.table.CellImpl( "
                    + "  sst.locationId, sst.id.stopTime, t.train, t.id.tripNumber, t.id.dir, t.tripType, t.id.serviceKey) "
                    + " from Trip t, ScheduledStopTime sst                        "
                    + " where TO_DATE(:date, 'MM-DD-YYYY') between t.id.tripBeginDate and t.tripEndDate  "
                    + "   and :route             =  t.id.routeNumber              "
                    + "   and :direction         =  t.id.dir                      "
                    + "   and :stopId            =  sst.locationId                "
                    + "   and t.id.serviceKey   in ( " + keySQLString + " )  " 
                    + "   and t.id.routeNumber   =  sst.id.routeNumber            "
                    + "   and t.id.dir           =  sst.id.dir                    "
                    + "   and t.id.serviceKey    =  sst.id.serviceKey             "
                    + "   and t.id.tripNumber    =  sst.id.tripNumber             "
                    + "   and t.id.tripBeginDate =  sst.id.tripBeginDate          "
                    + " order by t.id.tripNumber, sst.id.stopTime asc             "                    

            );
            timesQ.setInteger("stopId",    IntUtils.getIntFromString(stopId));
            timesQ.setInteger("route",     IntUtils.getIntFromString(route));
            timesQ.setInteger("direction", dir.value());
            timesQ.setString("date", date);

            // STEP C: CALL DB QUERY to get LIST of RESULTS
            retVal = timesQ.list();
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query end: " + new Date().getTime());
        }
        catch(DataException e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n" + e.getSQL(), e);
        }        
        catch(Exception e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n", e);
        }
        finally
        {
        }

        return retVal;
    }

    public static List<Cell> getTimesByRoute(TimesTable tt)
    {
        return getTimesByRoute(tt.getTimePoints(), tt.getRouteID(), tt.getDir(), tt.getDate(), getSQLKeyString(tt));
    }
    public static List<Cell> getTimesByRoute(TimesTable tt, String route)
    {
        return getTimesByRoute(tt.getTimePoints(), route, tt.getDir(), tt.getDate(), getSQLKeyString(tt));
    }
    public static List<Cell> getTimesByRoute(List<Stop> stops, String route, DirType dir, String date, String keySQLString)    
    {
        List<Cell> times = null;        
        try
        {
            List<Integer> stopIDs = StopImpl.toStopIDsAsIntegers(stops);
            if(stopIDs == null || stopIDs.size() < 1) return null;
            
            // STEP A: CONNECT TO THE DATABASE & START A SESSION
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "time by route with list of stops query start: " + new Date().getTime());
            Session session = TransHibernateUtil.getSession();

            // STEP B: QUERY THE DATABASE FOR ALL STOPS
            Query timesQ = session.createQuery(
                      "select distinct new org.timetablepublisher.table.CellImpl( "
                    + "  sst.locationId, sst.id.stopTime, t.train, t.id.tripNumber, t.id.dir, t.tripType, t.id.serviceKey) "
                    + " from Trip t, ScheduledStopTime sst                        "
                    + " where TO_DATE(:date, 'MM-DD-YYYY') between t.id.tripBeginDate and t.tripEndDate  "
                    + "   and t.id.routeNumber     = :route                       "
                    + "   and t.id.dir             = :direction                   "
                    + "   and t.id.serviceKey    in ( " + keySQLString + " ) "
                    + "   and sst.locationId     in ( :timepoints )               "
                    + "   and sst.id.routeNumber   =  t.id.routeNumber            "
                    + "   and sst.id.dir           =  t.id.dir                    "
                    + "   and sst.id.serviceKey    =  t.id.serviceKey             "
                    + "   and sst.id.tripNumber    =  t.id.tripNumber             "
                    + "   and sst.id.tripBeginDate =  t.id.tripBeginDate          "
                    + " order by t.id.tripNumber, sst.id.stopTime asc             "
            );
            timesQ.setString("route",             route);
            timesQ.setInteger("direction",        dir.value());
            timesQ.setString ("date",             date);
            timesQ.setParameterList("timepoints", stopIDs);
            
            // STEP C: CALL DB QUERY to get LIST of RESULTS
            times = timesQ.list();
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "times by route with list of stops query end: " + new Date().getTime());
        }
        catch(DataException e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n" + e.getSQL(), e);
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n", e);
        }
        finally
        {
        }
        
        return times;
    }
    
    
    //
    // S C H E D U L E   C R E A T I O N
    //     


    /**
     * NOTE: this is almost the 'exact' same code found in EsQueryUtils.java
     * 
     * This routine will take the list of stops times as input, and return back a set of
     * Rows that lay those timepoints out into the respective trips.  The bulk of the work is
     * finding each contiguous trip (eg: assumes that times in assending order, and that the 
     * entire list of times is sorted into trips), and then finding in what position in the row 
     * they belong.
     * 
     * @param timePoints
     * @param stopTimes
     * @param m_tt
     * @return
     */
    public static List<Row> layoutDataIntoSchedule(TimesTable tt, List<Stop> stops, List<Cell> allTimes, String route, DirType dir, String date)
    {    
        List<Row> schedule = new ArrayList<Row>();
        
        SvcKeyNormalizer skn = new SvcKeyNormalizer(tt);        
        for (int i = 0; i < allTimes.size(); i++)
        {
            // step 1: get a set of related times (eg: a trip) from the stopTime array
            List<Cell> trip = CellImpl.findTrip(allTimes, i);
            if(trip == null || trip.size() < 1)
            {
                LOGGER.warning("Strange ... we got and empty list (blank trip) from searching for times in the stop time array.");
                continue;
            }
            // step 1b: don't even bother with trips of less than two stop times
            if(trip.size() < 2)
                continue;
            
            // step 1c: advance our times counter beyond the current trip (minus the one gonna be subtracted by the for loop above)
            i += trip.size() - 1;

            
            // step 2: normalize service key -- might get both F and f trip times -- specific cleanup issue with MAX & Trans
            skn.normalizeServiceKey(trip);
            
            
            // step 3: find the times that map to these particular stops 
            List<Cell> tripTimes = CellImpl.mapTripTimesToStops(stops, trip);

            // step 3b: don't even bother with trips of less than two stop times
            if(tripTimes.size() <= 1)
                continue;
                        
            // step 4: place this trip into a new row
            RowImpl row = new RowImpl(route, dir, date, stops.size());
            row.setRow(tripTimes);
            row.getTrip();          // auto assigns block, trip and tripType to row 

            // step 5: ONLY add the row to the table where there is more than one UNIQUE cell
            if(RowImpl.areTimesDifferent(row)) 
            {
                schedule.add(row);
            }
        }

        return schedule;
    }
    

    public static List<Row> getSchedule(TimesTable tt)
    {
        return getSchedule(tt, tt.getRouteID(), tt.getDir(), tt.getDate(), tt.getTimePoints(), getSQLKeyString(tt)); 
    }
    public static List<Row> getSchedule(TimesTable tt, String route, DirType dir, String date, List<Stop> stops, String keySQLString)    
    {
        List<Row> schedule = null;        
        try
        {
            List<Cell> stopTimes = getTimesByRoute(stops, route, dir, date, keySQLString);            
            schedule = layoutDataIntoSchedule(tt, stops, stopTimes, route, dir, date);
            Collections.sort(schedule, new Row.Compare(stops.size()));
        }
        catch(DataException e)
        {
            LOGGER.log(SEVERE, "Exception caught in generating the schedules (returning empty List): " + e.toString()  + "\n" + e.getSQL());
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n", e);
        }
        finally
        {
            if(schedule == null)
                schedule = new ArrayList<Row>();
        }
        
        return schedule;
    }
    

    
    //
    // used for loop route time appendages
    // 
    public static void appendTimes(LoopFillIn loop, List<Cell> times, List<Row> schedule, Integer columnIndex)
    {   
        if(loop == null || times == null || schedule == null)
        {
            LOGGER.log(SEVERE, "NULL parameter in call to appendTimes...exiting method");
            return;
        }
        if(columnIndex == null)
        {
            LOGGER.log(DEBUG, "NULL column...exiting method...this may be a a minor problem, with a likely cause is that stop id is not in timepoints");
            return;
        }
               
        // a bit of init -- clear out processed status flag (it's used below)
        for(Cell c : times)
        {
            c.setProcessed(false);
        }
                
        for(Row row : schedule)
        {
            if(loop.getRule() == LoopFillIn.LoopFillInRule.LEFT)
            {
                // step 1: get a stop time for this trip, that's latter than target timepoint
                Cell rowCell = RowImpl.getNextCell(row, columnIndex + 1);  
                if(rowCell == null  || rowCell.getTime() <= 0) continue;
                
                // step 2: loop through target stop times, finding the one closest (in time) to this timepoint 
                Cell target = CellImpl.findClosest(times, rowCell, false);
                
                // step 3: append target stop's time to schedule...as long as the time does not exceed loop specified time 
                if(target != null && (rowCell.getTime() - target.getTime()) < loop.getTripLength())
                {
                    target.setProcessed(true);
                    target.setTrip(row.getTrip());
                    row.setCell(columnIndex, target);
                }                
            }
            else if(loop.getRule() == LoopFillIn.LoopFillInRule.RIGHT)
            {
                // step 1: get a stop time for this trip, that's latter than target timepoint
                Cell rowCell = RowImpl.getPrevCell(row, columnIndex - 1);  
                if(rowCell == null  || rowCell.getTime() <= 0) continue;
                
                // step 2: loop through target stop times, finding the one closest (in time) to this timepoint 
                Cell target = CellImpl.findClosest(times, rowCell, true);
                
                // step 3: append target stop's time to schedule...as long as the time does not exceed loop specified time
                if(target != null && (target.getTime() - rowCell.getTime()) < loop.getTripLength())
                {
                    target.setProcessed(true);
                    target.setTrip(row.getTrip());
                    row.setCell(columnIndex, target);
                } 
            }
        }        
    }
    
    
    public static int biggest(int w, int s, int u)
    {
        int retVal = w;
        if(retVal < s) retVal = s;
        if(retVal < u) retVal = u;
        
        return retVal;
    }   
    
    // SASI 
    public static List<Row> getStopSchedule(String stopId, TimesTable tt)
    {
        List<Row> retVal = new ArrayList<Row>();
        if(tt == null) return retVal;
        
        String weekKeys = ActiveServiceKeys.processReturnSQLString(tt, KeyType.Weekday);
        String satKeys  = ActiveServiceKeys.processReturnSQLString(tt, KeyType.Saturday);
        String sunKeys  = ActiveServiceKeys.processReturnSQLString(tt, KeyType.Sunday);

        Object w[] = getTimesByStopId(stopId, weekKeys, tt).toArray();
        Object s[] = getTimesByStopId(stopId, satKeys,  tt).toArray();
        Object u[] = getTimesByStopId(stopId, sunKeys,  tt).toArray();
        
        int size = biggest(w.length, s.length, u.length);
        for(int i = 0; i < size; i++)
        {
            // want to create a row of the table.  
            // note: the Weekday / Saturday / Sunday arrays will be different sized. don't care about
            //       the exceptions thrown, AND there WILL BE exceptions, hence the try/catch hack
            Row rt = new RowImpl(tt.getRouteID(), tt.getDir(), tt.getDate(), 3);
            try{ rt.setCell(0, (Cell)w[i]); } catch(Exception e){} 
            try{ rt.setCell(1, (Cell)s[i]); } catch(Exception e){} 
            try{ rt.setCell(2, (Cell)u[i]); } catch(Exception e){} 

            retVal.add(rt);
        }
      
        return retVal;
    }


    
    
    //
    //
    //  S T O P   &    T I M E P O I N T    R O U T I N E S
    //
    //

    public static Hashtable<String,String> getAmenityDescriptions(boolean externalDescription)
    {
        Hashtable<String,String> retVal = new Hashtable<String,String>();

        try
        {
            // STEP A: CONNECT TO THE DATABASE & START A SESSION
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query start: " + new Date().getTime());
            
            Session session = TransHibernateUtil.getSession();

            // STEP B: QUERY THE DATABASE FOR ALL STOPS
            Query nameQ = session.createQuery("from AmenityCodeDescription");

            // STEP C: CALL DB QUERY to get the NAME
            Iterator it = nameQ.iterate();
            while(it.hasNext())
            {
                AmenityCodeDescription a = (AmenityCodeDescription) it.next();
                if(externalDescription)
                    retVal.put(a.getAmenityCode(), a.getTripPlanningDescription());
                else
                    retVal.put(a.getAmenityCode(), a.getAmenityCodeDescription());
            }
            if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query start: " + new Date().getTime());
        }
        catch(DataException e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n" + e.getSQL(), e);
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n", e);
        }
        finally
        {
        }

        return retVal;
    }
     
    
    // gets a list of location ID (Collection<Integer>) for a given route / direction / day-of-week
    // 
    // TODO: have to add a date parameter & schedule status (timepoints / all / stops)
    //
    // ex:
    // route = 108 = 8-Jackson Park:
    // direction = 1 = To Portland (INBOUND)
    // serviceKey = U = Sunday
    //
    // scheduleStatus == [0-2] : all stops
    // scheduleStatus == [3] : all timepoints
    // scheduleStatus == [4] : all timepoints that are stops ???
    // scheduleStatus == [5] : narrow list of timepoints (like 2-3)
    // scheduleStatus == [6] : one stop
    // scheduleStatus >= [7] : zero stops
    //
    private static Iterator<RouteStop> doLocationQuery(Session session, int route, int dir, String date, int status, String keySQLString)
    {
        if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "route locations query start: " + new Date().getTime());
        Query timesQ = session.createQuery(
                "select rs                                                " +
                "from RouteStop rs, Route r                               " +
                "where " +
                "     TO_DATE(:date, 'MM-DD-YYYY') between rs.id.routeStopBeginDate and rs.routeStopEndDate " + 
                " and TO_DATE(:date, 'MM-DD-YYYY') between r.id.routeBeginDate and r.routeEndDate           " +                                 
                " and  rs.id.dir         = :direction                     " +
                " and  rs.id.routeNumber = :route                         " +
                " and  rs.id.routeNumber = r.id.routeNumber               " +
/*              " and rs.location.passengerAccessCode <> 'N'              " + */ 
                " and rs.location.locationId in                           " +
                " (                                                       " +
                "  select distinct sst.locationId                         " +
                "  from ScheduledStopTime sst, Trip t                     " +
                "  where                                                  " +
                "    sst.id.routeNumber   = t.id.routeNumber      and     " +
                "    sst.id.dir           = t.id.dir              and     " +
                "    sst.id.serviceKey    = t.id.serviceKey       and     " +
                "    sst.id.tripNumber    = t.id.tripNumber       and     " +
                "    sst.id.tripBeginDate = t.id.tripBeginDate    and     " +
                "    t.id.routeNumber     = :route                and     " +
                "    t.id.dir             = :direction            and     " +
                "    t.id.serviceKey in ( " + keySQLString + " )  and     " +
                "    sst.scheduleStatus >= :status                and     " +
                "    TO_DATE(:date, 'MM-DD-YYYY') between t.id.tripBeginDate and t.tripEndDate   " +
                " )                                                       " +
                " order by rs.id.colSeq "                   
        );             
        timesQ.setInteger("route",     route);
        timesQ.setInteger("direction", dir);
        timesQ.setString("date",       date);
        timesQ.setInteger("status",    status);

        Iterator<RouteStop> retVal = timesQ.iterate();
        if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "route locations query end: " +  new Date().getTime());
        return retVal;
    }

    private static Iterator<RouteStop> doLocationQuery(Collection<Integer> stopIds, int route, int direction, String date, Session session)
    {       
        if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "(with stops) location query start: " +  new Date().getTime());        
        Query timesQ = session.createQuery(
                "select rs                                                 " +
                "from RouteStop rs, Route r                                " +
                "where rs.id.dir         = :direction                      " +
                " and  rs.id.routeNumber = :route                          " +
                " and  rs.id.routeNumber = r.id.routeNumber                " +
                " and TO_DATE(:date, 'MM-DD-YYYY') between rs.id.routeStopBeginDate and rs.routeStopEndDate " + 
                " and TO_DATE(:date, 'MM-DD-YYYY') between r.id.routeBeginDate and r.routeEndDate           " +                                 
                " and rs.location.locationId in (:stopIdList)              " +
                "order by rs.id.colSeq                                     "                   
        );             
        timesQ.setInteger("route",     route);
        timesQ.setInteger("direction", direction);
        timesQ.setString("date",       date);
        timesQ.setParameterList("stopIdList",  stopIds);
        
        Iterator<RouteStop> retVal = timesQ.iterate();
        if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "(with stops) query end: " +  new Date().getTime());
        return retVal;        
    }

    // use this routine when schedule status does not matter
    private static Iterator<RouteStop> doLocationQuery(int route, int direction, String date, Session session)
    {       
        if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query start: " + new Date().getTime());
        
        Query timesQ = session.createQuery(
                " select rs                                               " +
                " from RouteStop rs, Route r                              " +
                " where rs.id.dir         = :direction                     " +
                " and   rs.id.routeNumber = :route                        " +
                " and   TO_DATE(:date, 'MM-DD-YYYY') between rs.id.routeStopBeginDate and rs.routeStopEndDate " + 
                " and   TO_DATE(:date, 'MM-DD-YYYY') between r.id.routeBeginDate and r.routeEndDate           " +                                 
                " and   rs.id.routeNumber = r.id.routeNumber              " +
                " order by rs.id.colSeq                                   "                   
        );             
        timesQ.setInteger("route",     route);
        timesQ.setInteger("direction", direction);
        timesQ.setString("date",       date);

        Iterator<RouteStop> retVal = timesQ.iterate();
        if(LOGGER.isLoggable(DEBUG)) LOGGER.log(DEBUG, "query start: " + new Date().getTime());

        return retVal;
    }


    public static List<Location> getLocations(int route, int direction, String serviceKey, String date)
    {
        return getLocations(null, route, direction, serviceKey, date, 0);
    }
    public static List<Location> getLocations(int route, int direction, String serviceKey, String date, int status)
    {
        return getLocations(null, route, direction, serviceKey, date, status);
    }
    public static List<Location> getLocations(Collection<Integer> stopList, int route, int direction, String serviceKey, String date)
    {
        // note: send down status == 4, just incase stopList is null...that will return scheduling timepoints
        return getLocations(stopList, route, direction, serviceKey, date, 4);
    }
    public static List<Location> getLocations(Collection<Integer> stopList, int route, int direction, String key, String date, int status)
    {           
        List<Location> retVal = new ArrayList<Location>();   

        try
        {
            // STEP A: CONNECT TO THE DATABASE & START A SESSION
            Session session = TransHibernateUtil.getSession();

            // STEP B: QUERY THE DATABASE FOR LOCATIONS
            Iterator<RouteStop> iter = null;
            if(stopList != null && stopList.size() > 0)
                iter = doLocationQuery(stopList, route, direction, date, session);
            else if(status == 0)
                iter = doLocationQuery(route, direction, date, session);
            else 
            {
                String sqlKeyStr = ActiveServiceKeys.processReturnSQLString(route, direction, key);
                iter = doLocationQuery(session, route, direction, date, status, sqlKeyStr);
            }


            // STEP C: CALL DB QUERY to get LIST of RESULTS
            while(iter.hasNext())
            {
                RouteStop rs = iter.next();
                if(rs == null) continue;
                
                Location l = rs.getLocation();
                //Location l = getLocation(rs.getLocationId());
                
                // we're returning a list of UNIQUE Locations, so assign them to our Location list
                // the Hibernate.initialize() makes the object available outside of the Hibernate session
                // NOTE: the last compare logic is a 'unique' clause -- the RS query sometimes retuns
                //       the same timepoint multiple times (at least that's been observed).
                if(!retVal.contains(l))
                {
                    Hibernate.initialize(l);
                    // TODO: is any of this needed???  need to fix this for efficiency's sake
                    //Hibernate.initialize(l.getLocationAmenities());
                    String tmp = l.getPublicLocationDescription();
                    retVal.add(l);
                }
            }
        }
        catch(DataException e)
        {
            LOGGER.log(SEVERE, "Couldn't get any locations from DB: " + e.getLocalizedMessage() + "\n" + e.getSQL(), e);
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, e.toString() + "\n", e);
        }
        finally
        {
        }

        return retVal;
    }

    public static Location getLocation(String stopID)
    {
        Integer id = IntUtils.getIntegerFromString(stopID);        
        return getLocation(id);
    }
    public static Location getLocation(Integer stopID)
    {
        if(stopID == null) return null;

        Location retVal = null;
        try
        {
            // STEP A: CONNECT TO THE DATABASE & START A SESSION
            Session session = TransHibernateUtil.getSession();

            // STEP B: QUERY THE DATABASE FOR ALL STOPS
            Query nameQ = session.createQuery("from Location where locationId = (:id)");
            nameQ.setParameter("id", stopID);

            // STEP C: CALL DB QUERY to get the NAME
            retVal = (Location) nameQ.uniqueResult();
            if(retVal != null)
            {
                Hibernate.initialize(retVal);
/*  TODO: not working - throwing an exception, so commented out
          (not sure amenities are really needed / used at this point, so...

                if(retVal.getLocationAmenities() != null && retVal.getLocationAmenities().size() > 0)
                    Hibernate.initialize(retVal.getLocationAmenities());
*/
            }
        }
        catch (Exception e)
        {
            LOGGER.log(SEVERE, e.toString(), e);
        }                
        finally
        {
        }

        return retVal;
    }

    public static String getLocationDescription(String stop)
    {
        String retVal = null;

        Location l = getLocation(IntUtils.getIntegerFromString(stop));
        if(l != null) {
            retVal = l.getPublicLocationDescription();
        }
            
        return retVal;
    }
    
    public static Stop makeStop(String stop, Integer sequence)
    {
        String desc = null;
        Double lat  = null;
        Double lng  = null;
        char   code = 'Y';
        
        Location l = getLocation(IntUtils.getIntegerFromString(stop));
        if(l != null) 
        {
            desc = l.getPublicLocationDescription();
            lat  = l.getLatitude().doubleValue();
            lng  = l.getLongitude().doubleValue();
            code = l.getPassengerAccessCode();
        }
        StopImpl retVal = new StopImpl(stop, sequence, desc);
        if(retVal != null)
        {
            retVal.setLatitude(lat);
            retVal.setLongitude(lng);
            retVal.setPublic(code);
        }
        return retVal;
    }
    

    public static List<Stop> getTimePoints(String routeID, DirType dir, KeyType key, String date)
    {
        try
        {
            return TransQueryUtils.getTimePoints(IntUtils.getIntegerFromString(routeID), dir.value(), key.value(), date);
        }
        catch(Exception e)
        {
            LOGGER.log(DEBUG, "Couldn't GET stops for this route: " + routeID + " dir: " + dir + " key " + key + " date: " + date, e);
            return null;
        }
    }
    public static List<Stop> getTimePoints(int route, int dir, String key, String date)
    {
        return getTimePointsDefinedByScheduling(route, dir, key, date);
    }    

    public static List<Stop> getTimePointsDefinedByScheduling(int route, int dir, String key, String date)
    {
        List<Location> locations = getLocations(route, dir, key, date, 4);
        return locationsToStops(locations);
    }
    public static List<Stop> getAllStops(int route, int dir, String key, String date)
    {
        List<Location> locations = getLocations(route, dir, key, date);
        return locationsToStops(locations);
    }
    
    public static List<Stop> locationsToStops(List<Location> locations)
    {
        List<Stop> retVal = new ArrayList<Stop>();
        if(locations == null) return retVal;
        
        int i = 1;
        for(Location l : locations)
        {
            StopImpl c = new StopImpl(l.getLocationId(), l.getLocationId().toString(), i, 
                                      l.getPublicLocationDescription(), 
                                      l.getPassengerAccessCode(), 
                                      l.getLatitude(), l.getLongitude());
            retVal.add(c);
        }
        
        return retVal;
    }
}