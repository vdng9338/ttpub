/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.timetablepublisher.configure.ActiveServiceKeys;
import org.timetablepublisher.configure.CullTrips;
import org.timetablepublisher.configure.EffectiveDate;
import org.timetablepublisher.configure.loader.ConfigurationCacheSingleton;
import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.IntUtils;

/**
 * This implements the abstract TimeTable class.  It contains a lot of the boiler plate  
 * plumbing code (getter/setter), and should be inherited by specific implementations.
 * 
 * TimeTable is a higher level class atop the scheduling data.  From a TimeTable, you have
 * an interface which exposes a lot of information.  Thus, it is the object that is usually
 * passed around in the view.  
 * 
 * @author  Frank Purcell
 * @version $Revision: 1.0 $
 * @since 1.0
 */
abstract public class TimesTableImpl implements TimesTable, Constants
{
    private static final Logger LOGGER = Logger.getLogger(TimesTableImpl.class.getCanonicalName());
    
    protected final String   m_agencyName;
    protected final String   m_routeID;
    protected final String   m_date;
    protected       String   m_serviceStartDate = null;
    protected       String   m_serviceEndDate   = null;
    
    protected KeyType        m_key;
    protected List<String>   m_activeServiceKeys = null;
    protected DirType        m_dir;
    protected RouteType      m_routeType         = null;
    
    protected String         m_routeName        = null;
    protected String         m_routeShortName   = null;
    protected String         m_routeLongName    = null;
    protected String         m_routeDescription = null;
    protected String         m_keyName          = null;
    protected String         m_dirName          = null;
    protected String         m_destination      = null;
    protected boolean        m_showStopID       = true;

    protected List<Row>      m_timeTable   = new ArrayList<Row>();
    protected List<Stop>     m_timePoints  = new ArrayList<Stop>();
    protected List<Footnote> m_footnotes   = new ArrayList<Footnote>();
    
    protected ConfigurationLoader m_config = null;
    protected boolean             m_bypassConfig;
    protected Locale              m_locale;

    public TimesTableImpl(TimesTable tt)
    {
        this(tt.getAgencyName(), tt.getRouteID(), tt.getDir(), tt.getKey(), tt.getDate(), tt.getLocale(), null, false);
    }
    public TimesTableImpl(String agencyName, String routeID, DirType dir, KeyType key, String date, Locale locale, String configDir, boolean bypass)
    {
        m_agencyName   = agencyName;
        m_routeID      = routeID;  
        m_dir          = dir;
        m_key          = key;
        m_locale       = locale;
        m_bypassConfig = bypass;

        if(configDir != null)
            m_config = ConfigurationCacheSingleton.getLoader(configDir);
        
        if(m_config == null)
            m_config = ConfigurationCacheSingleton.getDefaultLoader();
            
        
        // assign the date...knowing that we may get a null date, which we'll then pull from 
        if(date == null && m_config != null)
        {
            EffectiveDate ef = (EffectiveDate)m_config.findData(new EffectiveDate(this));            
            if(ef != null)
                m_date = ef.getStart();
            else
                m_date = "1-1-2007";
        }
        else
        {
            m_date = date;
        }
    }

    
    public ConfigurationLoader getConfiguration() 
    {
        return m_config;
    }
    public void setLoader(ConfigurationLoader conf)
    {
        m_config = conf;
    }
    public void reloadConfig()
    {
        ConfigurationCacheSingleton.reLoader(m_config);
    }

    
    public String getAgencyName()
    {
        return m_agencyName;
    }
    public String getAgencyURL()
    {
        return null;
    }
    public String getRouteURL()
    {
        return null;
    }
    public String getRouteID()
    {
        return m_routeID;
    }
    public String getDate()
    {
        return m_date;
    }
    public Date getEffectiveDate()
    {
        return IntUtils.getDate(m_date);    
    }
    public final String getServiceStartDate()
    {
        if(m_serviceStartDate == null)
            return getDate();
        
        return m_serviceStartDate;
    }
    public final String getServiceEndDate()
    {
        if(m_serviceEndDate == null)
            return getDate();
        
        return m_serviceEndDate;
    }
    public final void setServiceDates(Date s, Date e)
    {
        m_serviceStartDate = IntUtils.toDate(s, getDate());
        m_serviceEndDate   = IntUtils.toDate(e, getDate());
    }
    public final void setServiceDates(String s, String e)
    {
        m_serviceStartDate = s;
        m_serviceEndDate   = e;
    }


    public void process()
    {
    }
    
    public List<Cell> getStopTimes(String stopId)
    {
        return getStopTimes(stopId, false);
    }
    public List<Cell> getStopTimes(String stopId, boolean fromRight)
    {
        Integer i = StopImpl.findColumnIndex(getTimePoints(), stopId, fromRight);
        if(i == null) return null;
        
        List<Cell> retVal = new ArrayList<Cell>();
        for(Row r : getTimeTable())
        {
            retVal.add(r.getCell(i));
        }            
        
        return retVal;
    }
    
    
    /**
     *  Will propagate any footnote symbols appearing in the row (eg: first encountered symbol in row of cells, that is not already part of the cell)
     */
    public List<Cell> getStopTimesPropagateFootnotes(String stopId)
    {
        return getStopTimesPropagateFootnotes(stopId, null);
    }

    /**
     *  Will propagate any footnote symbols appearing in the row (eg: first encountered symbol in row of cells, that is not already part of the cell)
     *  The footnote filter is a comma-separated list of potential footnote symbols to match on -- any other footnote symbols in the row will be filtered.
     */
    public List<Cell> getStopTimesPropagateFootnotes(String stopId, String footnoteSymbolFilter)
    {
        return getStopTimesPropagateFootnotes(stopId, null, footnoteSymbolFilter);
    }
    public List<Cell> getStopTimesPropagateFootnotes(String stopId, Integer columnPosition, String footnoteSymbolFilter)
    {
        if(stopId == null || getTimeTable() == null) return null;
        
        // step A: check/find row index 
        Integer i = columnPosition;
        if(i == null || i >= getTimeTable().size()) 
        {
            boolean fromLeftOrRight = i != null && i >= getTimeTable().size();
            i = StopImpl.findColumnIndex(getTimePoints(), stopId, fromLeftOrRight);
            if(i == null)
                return null;
        }

        // step B: setup any footnote filter we might have specified
        String[] filters = null;
        if(footnoteSymbolFilter != null)
        {
            filters = footnoteSymbolFilter.split(",[ \t]*");
        }
       
        // step C: do the work
        List<Cell> retVal = new ArrayList<Cell>();
        for(Row r : getTimeTable())
        {
            // step 1: clone the cell (if not null)
            Cell c = r.getCell(i);
            if(c != null)
                c = new CellImpl(c);
            
            // step 2: add cloned cell to return list
            retVal.add(c);            
            
            // step 3: WARNING CONTINUES -- might add a NULL row to the list, so no further processing
            if(c == null) continue;
            
            if(LOGGER.isLoggable(DEBUG))
            {
                if(c.getStopId() == null || stopId == null)
                {
                    LOGGER.log(DEBUG, "either supplied and/or cell stopID is null...this is bad");
                }
                else if(stopId.equals(c.getStopId()))
                {
                    LOGGER.log(DEBUG, "supplied stopId " + stopId + " and target cell stopId " + c.getStopId() + " don't match; expect weird results");
                }                
            }
            
            
            // step 4: find & add any footnotes from the row to this cell
            String rSym = r.getFootnoteSymbol();
            if(rSym != null && rSym.length() > 0)
            {
                String cSym = c.getFootnoteSymbol();
                if(cSym == null || !rSym.equals(cSym))
                {
                    boolean passesFilter = true;

                    // step 2c: filter test -- make sure symbol is in any filter list
                    if(filters != null && filters.length > 0 && filters[0].length() > 0)                    
                    {
                        passesFilter = false;
                        for(String f : filters)
                        {
                            if(f == null) continue;
                            if(rSym.equals(f))
                            {
                                passesFilter = true;
                                break;
                            }
                        }
                    }
                    
                    if(passesFilter)
                    {
                        c.setFootnoteSymbol(rSym);
                    }
                }
            }
        }            
        
        return retVal;
    }

    
    public List<Row> getTimeTable()
    {
        return m_timeTable;
    }    
    public void setTimeTable(List<Row> row)
    {
        m_timeTable = row;
    }    
    
    
    public Stop getTimePoint(int i)
    {
        if(m_timePoints == null || i < 0 || i > m_timePoints.size()) 
            return null;
        
        return m_timePoints.get(i);
    }
    public List<Stop> getTimePoints()
    {
        return m_timePoints;
    }
    public void setTimepoints(Collection<String> timepoints)
    {
        // TODO: do a string to Column query here... 
        LOGGER.warning("Feature Not Implemented");
    }
    public void setTimepoints(List<Stop> timepoints)
    {
        m_timePoints = timepoints;
    }
    
    
    
    public List<Footnote> getFootnotes()
    {
        return m_footnotes;
    }
    

    public List<Stop> getRouteStops()
    {
        return m_timePoints;        
    }    
    public List<Stop> getSchedulingTimePoints()
    {
        return  getRouteStops();
    }
    
    
    public boolean bypassConfig()
    {
        return m_bypassConfig;
    }
    public void setBypassConfig(boolean ignoreConfig)
    {
        m_bypassConfig = ignoreConfig;
    }


    //
    // names / titles / directions / destination / other string-like data of the table
    //
    public String getDestination()    
    {
        if(m_destination == null && m_dir != null)
            return m_dir.toString();
        
        return m_destination;
    }
    public void setDestination(String dest)
    {
        m_destination = dest;
    }

    
    public DirType getDir()
    {
        return m_dir;
    }
    public String getDirName()
    {
        if(m_dirName == null)
        {
            m_dirName = m_dir.toString();
        }        
        return m_dirName;
    }
    public void setDirName(String name)
    {
        m_dirName = name;
    }


    public KeyType getKey()
    {
        return m_key;
    }
    public String getKeyName()
    {
        if(m_keyName == null)
        {
            m_keyName = m_key.toString();
        }        
        return m_keyName;
    }
    public void setKeyName(String keyName)
    {
        m_keyName = keyName;
    }
    
    public List<String> getActiveServiceKeys()
    {        
        if(m_activeServiceKeys == null)
        {
            m_activeServiceKeys = ActiveServiceKeys.process(this);
        }
        
        return m_activeServiceKeys;
    }

    
    
    public RouteType getRouteType()
    {
        return m_routeType;
    }
    public void setRouteType(RouteType routeType)
    {
        m_routeType = routeType;
    }
    
    
    public Locale getLocale()
    {
        return m_locale;
    }
    public void setLocale(Locale locale)
    {
        m_locale = locale;
    }
    public String getLanguage()
    {
        return m_locale.getLanguage();
    }
    

    
    /**
     * Get Route Name Routines 
     * 
     * The following routines contain a bit of logic to either get/set the field, or else build
     * out some string that makes sense.  
     */
    public String getRouteName()
    {
        if(m_routeName == null) 
        {
            if(m_routeShortName != null && m_routeLongName != null) 
            {
                m_routeName = m_routeShortName + "-" + m_routeLongName;
            }
            else
            {
                m_routeName = getRouteShortName();
                if(m_routeLongName != null) 
                {
                    m_routeName = m_routeName + "-" + m_routeLongName;    
                }
            }
        }
                
        return m_routeName;
    }
    public void setRouteName(String routeName)
    {
        m_routeName = routeName;
    }
    
    public String getRouteLongName()
    {
        if(m_routeLongName == null)
        {
            if(m_routeName != null) 
                m_routeLongName = getRouteName();
            else                    
                m_routeLongName = getRouteShortName();
        }

        return m_routeLongName;
    }
    public void setRouteLongName(String routeLongName)
    {
        m_routeLongName = routeLongName;
    }

    public String getRouteShortName()
    {
        if(m_routeShortName == null)
        {
            m_routeShortName = m_routeID;
        }
        return m_routeShortName;
    }
    public void setRouteShortName(String routeShortName)
    {
        if(m_routeShortName == null)
        {
            m_routeShortName = getRouteID();
        }

        m_routeShortName = routeShortName;
    }


    public String getRouteDescription()
    {
        if(m_routeDescription == null)
        {
            m_routeDescription = getRouteName() + " (" + getKeyName() + " - " + getDestination() + ")";
        }
        return m_routeDescription;
    }
    public void setRouteDescription(String routeDescription)
    {
        m_routeDescription = routeDescription;
    }

    public boolean isShowStopID()
    {
        return m_showStopID;
    }
    public boolean showStopIDs()
    {
        return m_showStopID;
    }
    public void setShowStopIDs(boolean showStopID)
    {
        m_showStopID = showStopID;
    }
    
    
    
    /**
     * This routine will use a configuration that defines the hours of operations in effect
     * for a given route (and trim off those trips that fall outside that range)
     *
     * NOTE: you probably want to do this before making footnotes, as some notes may fall
     *       away when trips are cut from the table.
     * 
     * @param table
     */
    public void cull(CullTrips cull)
    {
        List<Row> newRow = RowImpl.cull(m_timeTable, cull);
        if(newRow != null) {
            m_timeTable = newRow;
        }
    }
    
    public Row findTrip(String trip)
    {       
        return findTrip(m_timeTable, trip);
    }    
    static public Row findTrip(List<Row> rows, String trip)
    {
        if(rows == null) return null;
        
        Row retVal = null;
        for(Row r : rows)
        {
            if(r == null) continue;
            if(trip.equals(r.getTrip()))
            {
                retVal = r;
                break;
            }
        }
        return retVal;
    }
    
    
    public Stop findTimePoint(String stopID)
    {
        return StopImpl.findTimePointStatic(m_timePoints, stopID);
    }    
}
