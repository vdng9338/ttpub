/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.trimet.ttpub.schedule.trans;

import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import org.timetablepublisher.configure.ComboRoutes;
import org.timetablepublisher.configure.Configure;
import org.timetablepublisher.configure.RenameTimePoint;
import org.timetablepublisher.configure.RouteNames;
import org.timetablepublisher.configure.TimePoints;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.RouteDescription;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTableImpl;
import org.timetablepublisher.table.TimeTableFactory.TableType;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.IntUtils;


/**
 * The purpose of TrimetTimesTable is to provide an interface into the Stop Level schedule data
 * inside of TriMet's TRANS database.   
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 21, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class TransTimesTable extends TimesTableImpl implements TimesTable, Constants
{    
    private final Hashtable<String, List<Row>> m_stopTable = new Hashtable<String, List<Row>>();
    public static final String TRANS_AGENCY = "TriMetTRANS";
    protected final ScheduleDataQuery m_query = new TransDataQueryImpl(this);
    
    /**
     * This constructor will create a timetable based on an abitrary set of stops.
     * 
     * The stop list is either defined by scheduling, overridden by via config.TimePoints 
     * (eg: CSV config files), or these stops are a list requested by a customer via the web. 
     * 
     * Formatting rules (and thus footnotes) are applied in the last step.
     * 
     * @param route   --  route number (eg: 4, 104, 6, etc...)
     * @param dir     --  direction (eg: DirType enum)
     * @param key     --  service key (eg: KeyType enum)
     * @param date    --  effective date of this TrimetTimesTable
     */
    public TransTimesTable(List<Stop> timePoints, String route, DirType dir, KeyType key, String date, Locale locale, String configDirectory, boolean bypassConfig)
    {
        super(TRIMET, route, dir, key, date, locale, configDirectory, bypassConfig);
        m_timePoints = timePoints;
        process();
    }
    
    public void process()
    {
        RouteNames.setRouteDirectionName(this, m_query);
        
        if(m_timePoints == null || m_timePoints.size() < 1)
        {
            m_timePoints = TimePoints.getTimePoints(this, m_query);
            
        }
        m_timePoints = StopImpl.cullRepeatColumns(m_timePoints);
        
        if(ComboRoutes.isComboRoute(this))
        {
            ComboRoutes.process(this, m_query);
            RenameTimePoint.process(this, m_query);
        }
        else
        {
            m_timeTable = TransQueryUtils.getSchedule(this);        
            m_footnotes = Configure.processAllConfigurations(this, m_query);            
        }
    }  
    
    
    public TableType getTableType()
    {
        return TableType.TRANS;
    }        
    
    public List<Row> getStopTimeTable(String stopID)
    {
        if(m_stopTable.get(stopID) == null) 
        {
            m_stopTable.put(stopID, TransQueryUtils.getStopSchedule(stopID, this));
        }
            
        return m_stopTable.get(stopID);
    }
    
    public List<RouteDescription> getRouteNames()
    {
        return RouteNames.getRouteNames(this, m_query);
    }    
        
    public List<Stop> getRouteStops()    
    {
        return TransQueryUtils.getAllStops(IntUtils.getIntFromString(m_routeID), m_dir.value(), m_key.value(), m_date);
    }
    public List<Stop> getSchedulingTimePoints()    
    {
        return TransQueryUtils.getTimePointsDefinedByScheduling(IntUtils.getIntFromString(m_routeID), m_dir.value(), m_key.value(), m_date);
    }
}
