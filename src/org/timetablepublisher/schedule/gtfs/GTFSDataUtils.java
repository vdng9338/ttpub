/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import org.timetablepublisher.configure.ActiveServiceKeys;
import org.timetablepublisher.configure.Configure;
import org.timetablepublisher.configure.TimePoints;
import org.timetablepublisher.schedule.ScheduleDataQuery.PositionType;
import org.timetablepublisher.schedule.gtfs.loader.GTFSDataLoader;
import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.CellImpl;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.RouteDescription;
import org.timetablepublisher.table.RouteDescriptionImpl;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.RowImpl;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.IntUtils;


/**
 * The purpose of GDataUtils is to provide query utilities for reading and navigating the Google Feed Spec 
 * and associated data structures.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 8, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class GTFSDataUtils
{
    protected static final Logger LOGGER = Logger.getLogger(GTFSDataUtils.class.getCanonicalName());
    
    
    public static DirType getDefaultDirection(TimesTable tt, DirType def)
    {
        DirType retVal = def;
        Configure c = Configure.getDefaultConfigure(tt.getConfiguration(), tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString());
        if(c != null && c.getDirType() != null && c.getDirType().getOpposite() != def)
            retVal = c.getDirType();
        
        return retVal;
    }
    
    /**
     * load all trip stops into memory
     */
    public static Hashtable<String,List<String>> getTripStops(GTFSDataLoader loader, Agency agency)
    {
        return getTripStops(loader, agency, false);
    }
    public static Hashtable<String,List<String>> getTripStops(GTFSDataLoader loader, Agency agency, boolean allStops)
    {
        Hashtable<String,List<String>> retVal = new Hashtable<String,List<String>>();

        List stopList = loader.getData(agency, StopTimes.class);            
        if(stopList != null)
        {
            Collections.sort((List<StopTimes>) stopList, new Comparator<StopTimes>() {
                @Override
                public int compare(StopTimes s1, StopTimes s2) {
                    if(s1.getTrip() == null) {
                        if(s2.getTrip() != null)
                            return -1;
                        else
                            return s1.getStopSequence() - s2.getStopSequence();
                    }
                    else if(!s1.getTrip().equals(s2.getTrip()))
                        return s1.getTrip().compareTo(s2.getTrip());
                    else
                        return s1.getStopSequence() - s2.getStopSequence();
                }
            });
            String       thisTrip = "XXX";
            List<String> thisTPs  = null;
            boolean      hasTimePointInfo = false;

            // TODO: is this INEFFICIENT????  LOOK AT GETTING STOP TIMES LIST VIA TRIP OBJECT
            
            for(StopTimes s : (List<StopTimes>)stopList)
            {        
                if(s.getTrip() == null) continue;
                if(s.isTimepoint()) hasTimePointInfo = true;
                
                if(!allStops)
                {
                    // since we don't want ALL stops, then apply some criteria to including stops
                    // criteria #1: if the data has timepoint info, and it's not a timepoint, then continue
                    // criteria #2: if the data lacks timepoint info, make sure there are times for this stop
                    if(hasTimePointInfo) 
                    {
                        if(!s.isTimepoint()) continue;
                    }
                    else
                    {
                        if(s.getValidTime() == null) continue;                        
                    }
                }
                
                if(!s.getTrip().equals(thisTrip))                   
                {
                    thisTrip = s.getTrip();
                    thisTPs  = retVal.get(thisTrip);
                    if(thisTPs == null)
                    {
                        thisTPs  = new ArrayList<String>();
                        retVal.put(thisTrip, thisTPs);
                    }                    
                }
                
                thisTPs.add(s.getStopID());
            }                
        }

        return retVal;
    }   

    
    /**
     * Will evaluate a set of trips, and the stops made on each of those trips, to build the list of timepoints.
     * NOTE: this works best if the first trip in the input trips List has the most number of stops. 
     * 
     * @param trips
     * @param tripTimePoints
     * @return
     */
    public static List<String> getRouteStops(String[] trips, Hashtable<String,List<String>> tripTimePoints)
    {
        List<String> retVal = new ArrayList<String>();
        
        // step 1: loop through all of the trips
        for(String t : trips)
        {
            // step 2: and loop through all of the stops -- to match to the above trip
            for(String st : tripTimePoints.keySet())
            {
                if(st == null || !st.equals(t)) continue;     // match stops to this trip 
                List<String> stops = tripTimePoints.get(st);
                if(stops == null) continue;

                // step 3: seed a blank list with the first row of stops
                if(retVal.size() == 0)
                {
                    retVal.addAll(stops);                    
                    continue;
                }
                
                // step 4: insert the stops N+1 (and beyond) trips into the stop list
                for(int i = 0; i < stops.size(); i++)
                {
                    // step 4a: find a matching stop in the return list
                    String foundStop  = null;
                    int j;
                    for(j = i; j <  stops.size(); j++)
                    {
                        String tp = stops.get(j);
                        if(retVal.contains(tp))
                        {
                            foundStop  = tp;
                            break;
                        }
                    }

                    // step 4a: hmmm... we never found a matching stop...we'll add these stops, but also warn that this trip might not belong to this route 
                    if(i == 0 && foundStop == null)
                    {
                        // step 4e: we didn't find a stop
                        LOGGER.warning("trip " + t + " has zero stops in common with the other trips.  Is this trip really in the same route?");
                        retVal.addAll(stops);
                        break;
                    }
                    // step 4b: ok, we've found a stop (that doesn't need to be inserted)...are there any previous stops not in the list?                    
                    else if(i < j)
                    {   
                        // step 4c: since i < j, we did have un-matched stops before we found our match
                        //          so if we didn't have a foundStop, then we'll assume that these stops belong on the end of the list
                        if(foundStop == null)
                        {
                            // step 4d: append new stops onto end of list
                            while(i < stops.size() && i < j)
                            {
                                retVal.add(stops.get(i));
                                i++;
                            }
                        }
                        else
                        {
                            // step 4d: insert new stops into middle of the list
                            int thisIndex = retVal.indexOf(foundStop);
                            for(int k = thisIndex; k < retVal.size() && i < stops.size() && i < j; k++, i++)
                            {
                                retVal.add(k, stops.get(i));
                            }
                        }                         
                    }
                }                
            }
        }
        
        return retVal;
    }    
    public static List<String> getRouteStops(List<String> trips, Hashtable<String,List<String>> tripTimePoints)
    {
        if(trips == null || trips.size() < 1) 
            return null;
        
        return getRouteStops(trips.toArray(new String[trips.size()]), tripTimePoints);
    }

    
    public static Hashtable<String, List<String>> colapseRoutes(GTFSDataLoader loader, Agency agency)
    {
        Hashtable<String,List<String>> retVal = new Hashtable<String,List<String>>();
        
        List routeList = loader.getData(agency, Routes.class);
        for(Routes r : (List<Routes>) routeList)
        {
            if(r == null || r.getRouteID() == null) continue;
            
            String key = r.getDescription();
            if(key == null) key = r.getLongName();
            List<String> rtList = retVal.get(key);
            if(rtList == null)
            {
                rtList = new ArrayList();
                retVal.put(key, rtList);
            }           
            
            rtList.add(r.getRouteID());
        }
        
        return retVal;
    }

    public static List<Trips> findTrips(GTFSDataLoader loader, TimesTable tt)    
    {
        return findTrips(loader, tt.getAgencyName(), tt.getRouteID(), tt.getDir(), tt.getKey(), tt.getActiveServiceKeys(), tt.bypassConfig(), tt.getDate());
    }
    
    public static List<Trips> findTrips(GTFSDataLoader loader, String agencyName, String routeID, DirType dir, KeyType key, List<String> activeServiceKeys, boolean bypassConfig, String date)
    {
        if(loader == null) return null;
        
        List<Trips> retVal = new ArrayList<Trips>();
        List tripList = loader.getData(agencyName, Trips.class);

        boolean compareSvcKeys = false;
        if(!bypassConfig && activeServiceKeys != null && activeServiceKeys.size() > 0)
        {
            compareSvcKeys = true;
        }
        
        if(tripList != null && routeID != null && dir != null)
        {
            for(Trips t : (List<Trips>)tripList) 
            {
                // filter null trips ... and also those trips with service keys that don't match our list of ActiveServiceKeys
                if(t == null) continue;
                if(compareSvcKeys && ActiveServiceKeys.filterKey(activeServiceKeys, t.getKey()))  continue;

                if(routeID.equals(t.getRoute()) && dir.isSemanticallySame(t.getDir(), true))
                {
                    retVal.add(t);
                }
            }
            
            // TODO: maybe add caching here...no need to do this query more than once (except for memory)f
            // TODO: put cache inside of loader -- trip cache / stop cache / etc...
        }        
        return retVal;
    }
    
    
    
    public static List<String> findTripIDs(GTFSDataLoader loader, String agencyName, String routeID, DirType dir, KeyType key, List<String> activeServiceKeys, boolean bypassConfig, String date)
    {
        List<String> retVal = new ArrayList<String>();
        
        List<Trips> tripList = findTrips(loader, agencyName, routeID, dir, key, activeServiceKeys, bypassConfig, date);
        if(tripList != null)
        {
            for(Trips t : tripList)
            {
                if(t != null && t.getTripID() != null)
                    retVal.add(t.getTripID());
            }
        }
        
        return retVal;
    }

    
    /**
     * get the tripIDs directly out of the Google defined Trips.txt, based on the routeID that is
     * defined by  
     * 
     * @param  loader
     * @param  agency
     * @param  routeID
     * @return List<String>
     * 
     */
    public static List<String> getTripIDsByGRoute(GTFSDataLoader loader, Agency agency, String routeID)
    {
        if(loader == null || agency == null || routeID == null) return null;

        List<String> retVal = new ArrayList<String>();
        
        List tList = loader.getData(agency, Trips.class);
        if(tList != null)
        {
            for(Trips t : (List<Trips>)tList)
            {
                if(t == null || t.getTripID() == null || t.getRoute() == null) continue;                
                if(routeID.equals(t.getRoute()))
                {
                    retVal.add(t.getTripID());
                }
            }
        }
        
        return retVal;
    }
    
    
    /**
     * Input a route, and get back a list of trips active for that route
     *   
     * @param routeId
     * @param agency
     * @param loader
     * @return
     */    
    public static List<String> findTripIDsByStopID(GTFSDataLoader loader, Agency agency, List<Trips>tripList, String stopID, PositionType stopPosition)
    {
        List<String> retVal = new ArrayList<String>();

        if(tripList != null)
        {
            for(Trips t : tripList)
            {
                if(t == null || t.getTripID() == null || t.getRoute() == null) continue;
                
                List<StopTimes> tripTimes = t.getStopTimes(loader, agency);
                if(tripTimes == null || tripTimes.size() <= 1) continue;
                
                for(StopTimes st : tripTimes)
                {
                    // optional StopID filter 
                    if(stopID != null)
                    { 
                        // only continue processing when stopID matches the stop
                        if(!stopID.equals(st.getStopID())) continue;
                        
                        // another filter -- only continue when this stop is at the end of trip
                        int pos  = tripTimes.lastIndexOf(st);                        
                        if(stopPosition == PositionType.LAST)
                        {                            
                            // filter if this stop is not the last stop on trip
                            int size = tripTimes.size() - 1;
                            if(pos < size) continue;
                        }
                        else if(stopPosition == PositionType.FIRST)
                        {
                            // filter if this stop is not the first stop on trip
                            if(pos > 0) continue;                            
                        }
                    }
                    
                    if(st.getTrip().equals(t.getTripID()))
                    {
                        retVal.add(t.getTripID());
                        break;
                    }
                }
            }
        }
        
        return retVal;
    }

    
    /** NOTE: these queries are specific to rt/dir/key */
    public static List<String> findTripIDsByTT(GTFSDataLoader loader, TimesTable tt, String stopID, PositionType position)
    {
        if(tt == null || loader == null) return null;
        Agency agency = loader.getAgency(tt.getAgencyName());

        List<Trips> tripList = findTrips(loader, tt);
        if(tripList == null) return null;
        
        return findTripIDsByStopID(loader, agency, tripList, stopID, position);
    }
    
    
    /**
     * come up with a set of timepoints per route, be evaluating the trip stops
     * 
     * @param loader
     * @param trip
     * @return
     */
    public static void calculateRouteStops(GTFSDataLoader loader, Agency agency, Hashtable<String,List<String>> tripTimePoints)
    {
        List routeList = loader.getData(agency, Routes.class);        
        List tripList  = loader.getData(agency, Trips.class);
        if(routeList != null && tripList != null)
        {
            for(Routes r : (List<Routes>)routeList)
            {
                if(r == null || r.getRouteID() == null) continue;
                
                List<TimePoints> tpDir1 = new ArrayList<TimePoints>();
                List<TimePoints> tpDir2 = new ArrayList<TimePoints>();
                
                for(Trips t : (List<Trips>)tripList)
                {
                    if(t == null || t.getTripID() == null || t.getRoute() == null) continue;
                    if(!t.getRoute().equals(r.getRouteID())) continue;
                    
                    String thisTrip = t.getTripID();
                    for(String st : tripTimePoints.keySet())
                    {
                        if(st == null || !st.equals(thisTrip)) continue;
                        List<String> stops = tripTimePoints.get(st);
                        if(stops == null) continue;
                        
                        System.out.print(r.getRouteID() + " " + t.getTripID() + " ");
                        for(String tp : stops)
                        {
                            System.out.print(tp + " ");
                        }
                        System.out.println();
                    }
                }
            }
        }        
        
        //return retVal;        
    }

       
    public static Stop makeColumn(GTFSDataLoader loader, Agency agency, String stopID, Integer sequence)
    {
        return Stops.makeColumn(loader, agency, stopID, sequence);
    }

    public static Stop makeColumn(GTFSDataLoader loader, String agencyName, String stopID, Integer sequence)
    {
        Agency agency = loader.getAgency(agencyName);
        return makeColumn(loader, agency, stopID, sequence);
    }
    
    public static List<Stop> getRouteStops(GTFSDataLoader loader, TimesTable tt, boolean allStops)
    {
        return getRouteStops(loader, tt.getAgencyName(), tt.getRouteID(), tt.getDir(), tt.getKey(), tt.getActiveServiceKeys(), tt.bypassConfig(), tt.getDate(), allStops);
    }
    public static List<Stop> getRouteStops(GTFSDataLoader loader, String agencyName, String routeID, DirType dir, KeyType key, List<String> activeServiceKeys, boolean bypassConfig, String date, boolean allStops)
    {
        List<Stop> retVal = new ArrayList<Stop>();

        Agency agency = loader.getAgency(agencyName);
        
        // TODO: geeze, this getTripStops call looks inefficient...maybe it's only called once at startup?
        Hashtable<String,List<String>> tripStops = getTripStops(loader, agency, allStops);
        if(tripStops != null)
        {
            List<String> tripIDList = findTripIDs(loader, agencyName, routeID, dir, key, activeServiceKeys, bypassConfig, date);
            List<String> stopIdList = getRouteStops(tripIDList, tripStops); 
            if(stopIdList != null)
            {
                int i = 1;
                for(String s : stopIdList)
                {
                    Stop kol = Stops.makeColumn(loader, agency, s, 10 * i++);
                    retVal.add(kol);
                }                
            }
        }
        
        return retVal;
    }    
    
    
    
    /**
     * Make Cells
     * 
     * @param tripTimes
     * @param stopID
     * @param block
     * @param trip
     * @param tripType
     * @param dir
     * @param key
     * @return Cell -- one trip time
     */
    public static Cell makeTripTimeByStop(List<StopTimes> tripTimes, String stopID, String block, String trip, String tripType, DirType dir, String key, KeyType defKey)
    {
        Cell retVal = null;
        
        // step 1: find a time matching this stop
        StopTimes st = StopTimes.find(tripTimes, stopID);
        if(st != null)
        {
            // step 2: found a target route direction, so capture it as a time
            String time = st.getValidTime();
            int seconds = IntUtils.secPastMid(time);
            
            // step 3: make the cell to return
            retVal = new CellImpl(st.getStopID(), seconds, block, trip, dir, tripType, key, defKey);
        }
        
        return retVal;
    }

    
    public static List<Row> getSchedule(GTFSDataLoader loader, Agency agency, String route, DirType dir, KeyType key, String date, List<Stop> stops, List<Trips> tripList)
    {
        if(tripList == null) return null;  
        
        List<Row> retVal = new ArrayList<Row>();        
        try
        {
            // step 1: loop through the trips 
            for(Trips t : tripList)
            {
                // step 2: find the trip times for this trip -- make sure this trip has 2 or more times
                List<StopTimes> tripTimes = t.getStopTimes(loader, agency);
                if(tripTimes == null || tripTimes.size() <= 1) continue;
                StopTimes.clearProcessed(tripTimes);
                
                // step 3: create a new row
                Row r = new RowImpl(route, dir, date, stops.size());
                
                // step 4: fit the times (cells) into this new row, based on the selected time points
                int numStops = 0;
                for(int i = 0; i < stops.size(); i++)
                {                    
                    Stop c = stops.get(i);
                    
                    // step 5: find a stop time that matches this stop id                   
                    Cell cell = makeTripTimeByStop(tripTimes, c.getStopId(), t.getBlock(), t.getTripID(), t.getTripType(), t.getDir(), t.getKey(), key);
                    if(cell != null)
                    {
                        r.setCell(i, cell);
                        numStops++;
                    }
                }
                
                // step 8: add the trip (as long as it has two or more stop times) to the return value 
                if(numStops >= 2)
                {
                    retVal.add(r);
                }
                else
                {
                    LOGGER.warning("trip " + r.getTrip() + " had only " + numStops + " stop times.");
                }
            
            }

            // step 9: sort trips (table rows) based on time
            Collections.sort(retVal, new Row.Compare(stops.size()));                
        }
        catch(Exception e)
        {
            LOGGER.log(Constants.SEVERE, "couldn't get schedule -- probably a cast problem", e);
        }
        
        return retVal;
    }

    public static List<Row> getSchedule(GTFSDataLoader loader, String agencyName, String route, DirType dir, KeyType key, String date, List<Stop> stops, List<String> actSvcKeys, boolean bypassConfig)
    {
        List<Row> retVal = null;
        try
        {
            List<Trips> tripList = GTFSDataUtils.findTrips(loader, agencyName, route, dir, key, actSvcKeys, bypassConfig, date);
            Agency      agency   = loader.getAgency(agencyName);            
            retVal = getSchedule(loader, agency, route, dir, key, date, stops, tripList);
        }
        catch(Exception _)
        {
        }
        
        return retVal;
    }    

    
    public static List<Row> getSchedule(GTFSTimesTable tt)
    {
        if(tt == null) 
            return null;
                
        return getSchedule(tt.getData(), tt.getAgencyName(), tt.getRouteID(), tt.getDir(), tt.getKey(), tt.getDate(), tt.getTimePoints(), tt.getActiveServiceKeys(), tt.bypassConfig()); 
    }
    
    /**
     * getTimesByStopID
     * 
     * @param m_tt
     * @param stopID
     * @param fromRight 
     * @return List<Cell> list of stop times for a multiple trips in a given service day
     */
    public static List<Cell> getTimesByStopID(GTFSDataLoader loader, TimesTable tt, String stopID, boolean fromRight)
    {
        if(tt == null) return null;
        return getTimesByStopID(loader, tt, tt.getRouteID(), tt.getDir(), stopID, fromRight);
    }
    public static List<Cell> getTimesByStopID(GTFSDataLoader loader, TimesTable tt, String route, DirType dir, String stopID, boolean fromRight)
    {
        List<Cell> retVal = new ArrayList<Cell>();
        if(tt == null || route == null || stopID == null) return retVal;

        Agency agency        = loader.getAgency(tt.getAgencyName());

        // get list of trips for this rt/dir/key
        List<Trips> tripList = findTrips(loader, tt.getAgencyName(), route, dir, tt.getKey(), tt.getActiveServiceKeys(), tt.bypassConfig(), tt.getDate());
        if(tripList == null) return retVal;

        // iterate trips to build a column of times (cells) for this stop 
        for(Trips trip : tripList)
        {
            if(trip == null) continue;

            List<StopTimes> tripTimes = trip.getStopTimes(loader, agency);
            if(tripTimes == null || tripTimes.size() <= 1) continue;
            StopTimes.clearProcessed(tripTimes);
            if(fromRight)
            {
                // if we're looking from the right, we can reverse the list of times
                Collections.reverse(tripTimes);
            }

            // make our new cell 
            Cell cell = makeTripTimeByStop(tripTimes, stopID, trip.getBlock(), trip.getTripID(), trip.getTripType(), trip.getDir(), trip.getKey(), tt.getKey());
            if(cell != null) 
            {
                retVal.add(cell);
            }
        }
        return retVal;
    }



    public static List<StopTimes> getTripTimes(List<StopTimes> stopTimeList, String trip)
    {
        List<StopTimes> retVal = new ArrayList<StopTimes>();
        if(trip != null && trip.length() > 0 && stopTimeList != null)
        {
            for(StopTimes s : stopTimeList)
            {        
                if(s == null || s.getTrip() == null) continue;
                
                if(trip.equals(s.getTrip()))
                {
                    retVal.add(s);
                }
            }
        }
        return retVal;
    }

    
    public static String getRouteIDForList(String agencyName, String routeID)
    {
        return agencyName + Constants.AGENCY_ROUTE_SEP + routeID;
    }
    
    public static List<RouteDescription> getRouteNames(GTFSDataLoader loader, String date)
    {
        if(loader == null || loader.getAgencies() == null) return null;
        
        List<RouteDescription> retVal = new ArrayList<RouteDescription>();
        List<String> cache = new ArrayList<String>();
        for(Agency a : loader.getAgencies())
        {
            Integer i = 1;
            
            // get Route Def data, if exists -- if not, continue to next Agency
            List rList = loader.getData(a, Routes.class);
            if(rList == null) continue;
            
            for(Routes r : (List<Routes>)rList)
            {                
                if(r == null) continue;

                String routeID = getRouteIDForList(a.getName(), r.getRouteID());
                if(cache.contains(routeID)) continue;
                
                String name = "???";
                String[] nmList = {r.getShortName(), r.getLongName(), r.getDescription()};
                for(String n : nmList)
                {
                    if(n != null && n.length() > 5)
                    {
                        int j = n.length(); 
                        if(j > 30) j = 30;
                        name = n.substring(0, j);
                        break;
                    }                    
                }
                
                String routeName = a.getName() + " -- " + name;                
                cache.add(routeID);

                // create the route desc
                RouteDescriptionImpl rd = new RouteDescriptionImpl(
                        a.getName(),
                        routeName,
                        routeID,
                        r.getRouteID(),
                        true);
                rd.setUrl(r.getUrl());                
                
                retVal.add(rd);
                i++;
            }
        }

        return retVal;
    }
    
    public static Routes findRoute(GTFSDataLoader loader, TimesTable tt)
    {
        if(tt == null || tt.getRouteID() == null || loader == null) return null;
        
        Routes retVal = null;        
        
        String routeID = tt.getRouteID();
        List   rList   = loader.getData(tt.getAgencyName(), Routes.class);
        if(rList != null)
        for(Routes r : (List<Routes>)rList)
        {                
            if(r == null || r.getRouteID() == null) continue;
            if(routeID.equals(r.getRouteID()))
            {
                retVal = r;
                break;
            }
        }

        return retVal;
    }

    private static String findDestinationString(GTFSDataLoader loader, TimesTable tt)
    {
        String retVal = null;
        
        List<Trips> tList = findTrips(loader, tt);
        if(tList != null)
        for(Trips t : tList)
        {
            if(t == null || t.getHeadSign() == null) continue;
            retVal = t.getHeadSign();
            break;
        }
        return retVal;
    }
    
    public static RouteDescription getRouteDirectionName(GTFSDataLoader loader, TimesTable tt)
    {
        if(tt == null) return null;        
        RouteDescriptionImpl retVal = null;
        
        Routes ref  = findRoute(loader, tt);
        String dest = findDestinationString(loader, tt);
        if(ref != null)
        {
            tt.setRouteShortName(ref.getShortName());
            tt.setRouteLongName(ref.getLongName());
            tt.setRouteDescription(ref.getDescription());
            tt.setRouteType(ref.getType());
            tt.setDestination(dest);
            retVal = new RouteDescriptionImpl(tt);
            retVal.setUrl(ref.getUrl());
        }
        
        return retVal;
    }
}
