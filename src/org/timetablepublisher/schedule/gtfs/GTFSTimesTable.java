 /**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.RouteDescription;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTableImpl;
import org.timetablepublisher.table.TimeTableFactory.TableType;
import org.timetablepublisher.configure.ActiveServiceKeys;
import org.timetablepublisher.configure.ComboRoutes;
import org.timetablepublisher.configure.Configure;
import org.timetablepublisher.configure.RenameTimePoint;
import org.timetablepublisher.configure.RouteNames;
import org.timetablepublisher.configure.TimePoints;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.schedule.gtfs.loader.GTFSDataLoader;


/**
 * The purpose of GTimesTable is to implement a TimesTable that supports schedule data in the Google Transit Feed Spec format.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 4, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 * @see     http://code.google.com/transit/spec/transit_feed_specification.htm
 */
public class GTFSTimesTable extends TimesTableImpl implements TimesTable
{
    private static final Logger LOGGER = Logger.getLogger(GTFSTimesTable.class.getCanonicalName());    
    
    private final ScheduleDataQuery  m_query;
    protected List<RouteDescription> m_routes     = null;
    protected List<Stop>             m_routeStops = null;
    
    protected     Agency             m_agency;    
    protected     GTFSDataLoader     m_data;
    protected     RouteDescription   m_routeDescription;

    public GTFSTimesTable(List<Stop> timePoints, String agencyName, String routeID, DirType dir, KeyType key, String date, Locale locale, String configDir, boolean bypassConfig, String dataPath)
    {
        super(agencyName, routeID, dir, key, date, locale, configDir, bypassConfig);        
        
        m_data   = GTFSDataLoader.loader();
        m_query  = new GTFSDataQueryImpl(m_data);      
        m_agency = m_data.getAgency(m_agencyName);
        m_dir    = GTFSDataUtils.getDefaultDirection(this, dir);
        m_timePoints = timePoints;
        process();
    }
    
    public void process()
    {
        m_routeDescription = RouteNames.setRouteDirectionName(this, m_query);
        
        if(m_timePoints == null || m_timePoints.size() < 1 )
        {
            m_timePoints = TimePoints.getTimePoints(this, m_query);
        }
        m_timePoints = StopImpl.trimTimePointList(this, m_timePoints, m_query);
        
        if(ComboRoutes.isComboRoute(this))
        {
            ComboRoutes.process(this, m_query);
            RenameTimePoint.process(this, m_query);
        }
        else
        {
            m_timeTable = GTFSDataUtils.getSchedule(this);
            m_footnotes = Configure.processAllConfigurations(this, m_query);
        }        
    }

    public String getAgencyURL()
    {
        if(m_agency == null) 
            return null;

        return m_agency.getUrl();
    }
    public String getRouteURL()
    {
        if(m_routeDescription == null) 
            return null;
        
        return m_routeDescription.getUrl();
    }
    
    public List<String> getActiveServiceKeys()
    {
        if(m_activeServiceKeys == null || m_activeServiceKeys.isEmpty())
        {
            m_activeServiceKeys = ActiveServiceKeys.process(this);
            if(m_activeServiceKeys == null || m_activeServiceKeys.isEmpty())
            {
                GTFSDataLoader.setAgencySvcKeys(m_agencyName, m_config, m_data);
                m_activeServiceKeys = ActiveServiceKeys.process(this);
            }
        }
        
        return m_activeServiceKeys;
    }
    
    public List<RouteDescription> getRouteNames()
    {
        if(m_routes == null || m_routes.size() < 1)
        {
            m_routes = RouteNames.getRouteNames(this, m_query);
        }
        
        return m_routes;
    }
    
    public List<Row> getStopTimeTable(String stopId)
    {
        return getTimeTable();
    }
        
    public List<Stop> getRouteStops()
    {
        if(m_routeStops == null || m_routeStops.size() < 1)
        {
            m_routeStops = GTFSDataUtils.getRouteStops(m_data, this, true);
        }            

        return m_routeStops;
    }
    

    public GTFSDataLoader getData()
    {
        return m_data;
    }

    public TableType getTableType()
    {
        return TableType.GTFS;
    }
}
