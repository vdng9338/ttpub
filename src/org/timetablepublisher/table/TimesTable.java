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

import org.timetablepublisher.configure.CullTrips;
import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.table.TimeTableFactory.TableType;
import org.timetablepublisher.utils.IntUtils;

/**
 * The purpose of TimesTable is to provide an interface for a TimesTable.  The TimesTable is an abstract type within 
 * the TimeTablePublisher.  The goal of the system is to build a TimesTable object out of schedule data (comming form
 * a given data source -- whether it be a database or .csv file or xml file or ...), and then send that TimesTable object
 * down to a view to be rendered.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 8, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public interface TimesTable
{    
    // getters
    public String       getAgencyName();
    public String       getAgencyURL();
    public String       getRouteURL();
    public String       getRouteID();
    public String       getRouteName();
    public String       getRouteShortName();
    public String       getRouteLongName();
    public String       getRouteDescription();
    public String       getDestination();
    public String       getDate();
    public String       getServiceStartDate();
    public String       getServiceEndDate();
    public void         setServiceDates(Date s, Date e);
    public void         setServiceDates(String s, String e);    
    public DirType      getDir();
    public String       getDirName();
    public KeyType      getKey();
    public String       getKeyName();
    public List<String> getActiveServiceKeys();
    public TableType    getTableType();
    public RouteType    getRouteType();    
    public boolean      showStopIDs();

    public List<Row>    getTimeTable();
    public List<Row>    getStopTimeTable(String stopId);
    public List<Cell>   getStopTimes(String stopId);
    public List<Cell>   getStopTimes(String stopId, boolean fromRight);
    public List<Cell>   getStopTimesPropagateFootnotes(String stopId);
    public List<Cell>   getStopTimesPropagateFootnotes(String stopId, String footnoteSymbolFilter);
    public List<Cell>   getStopTimesPropagateFootnotes(String stopId, Integer columnPosition, String footnoteSymbolFilter);    
    public Collection<Footnote> getFootnotes();
    
    public Stop       getTimePoint(int i);    
    public List<Stop> getTimePoints();
    public List<Stop> getSchedulingTimePoints();
    public List<Stop> getRouteStops();   
    public List<RouteDescription> getRouteNames();

    
    // setters
    public void setBypassConfig(boolean bypass);
    public void setTimepoints(Collection<String> timepoints);
    public void setTimepoints(List<Stop>         timepoints);
    public void setTimeTable(List<Row> row);
    public void setShowStopIDs(boolean show);

    public void setRouteName(String routeName);    
    public void setRouteShortName(String str);
    public void setRouteLongName(String str);
    public void setRouteDescription(String str);
    public void setDestination(String dest);    
    public void setDirName(String dirName);
    public void setKeyName(String keyName);
    public void setRouteType(RouteType routeType);

    
    // finders
    public Stop findTimePoint(String stopId);
    public Row  findTrip(String trip);

    public ConfigurationLoader getConfiguration();
    public void setLoader(ConfigurationLoader conf);
    public void reloadConfig();
    
    // special processors
    public boolean bypassConfig();
    public void    process();
    public void    cull(CullTrips cull);    
    
    
    // localization 
    public String getLanguage();
    public Locale getLocale();
    public void   setLocale(Locale locale);
    public Date   getEffectiveDate();


    // Key & Direction enums
    
    
    /**
     * The purpose of KeyType is to represent a Service Key Type (eg: Weekday, Saturday, Sunday, Holiday, etc...)
     * 
     * @author  Frank Purcell (purcellf@trimet.org)
     * @date    Nov 8, 2006
     * @project http://timetablepublisher.org
     * @version Revision: 1.0
     * @since   1.0
     */
    public static enum KeyType
    {
        Weekday("W", 0), Saturday("S", 1), Sunday("U", 2),
        Holiday("H", 3), Special_Service("V", 4),
        Monday_Thursday("f", 5), Friday("F", 6),
        Monday("m", 7), Tuesday("t", 8), Wednesday("w", 9), Thursday("t", 10),
        AllDays("A", 111), Weekend("D", 222)
        ;
        
        private final String m_value;
        private final int    m_iValue;
        KeyType(String value, int iValue) { this.m_value = value; this.m_iValue = iValue; }
        public String value()  { return m_value;  }
        public int    ivalue() { return m_iValue; }

        public static KeyType construct(String value) { return construct(value, Weekday); }
        public static KeyType construct(KeyType k)    { return k != null ? k :  Weekday;  }
        public static KeyType construct(String value, KeyType defaultKey)
        {
            KeyType retVal = null;
            
            if(value != null)
            {
                value = value.trim();
                
                for(KeyType k : KeyType.values())
                {
                    if(value.equals(k.value()))
                    {
                        retVal = k;
                        break;
                    }
                }
            }
            
            if(retVal == null)
            {
                try
                {
                    retVal = KeyType.valueOf(value);
                }
                catch(Exception e)
                {
                    retVal = defaultKey;
                }
            }
            return retVal;
        }
        public static KeyType[] getWkSaSu() { return new KeyType[] {Weekday, Saturday, Sunday}; }
        public static KeyType[] toKeyArray(KeyType k)
        { 
            KeyType[] keys;
            if(k == null) 
            {
                keys = KeyType.getWkSaSu();
            }
            else
            {
                keys = new KeyType[1];
                keys[0] = k;
            }
            return keys;
        }
        
        public String[] getValueArray()
        {
            final String[] WEEKDAY  = {"W", "F", "f", "A"};
            final String[] SATURDAY = {"S", "D"};
            final String[] SUNDAY   = {"U", "D"};
            
            switch(this)
            {
                case Saturday: return SATURDAY;
                case Sunday:   return SUNDAY;
                default:
                case Weekday:  return WEEKDAY;
            }
        }
        
        public List<String> getValueList()
        {
            List<String> retVal = new ArrayList<String>();
            for(String s : getValueArray())
            {
                retVal.add(s);
            }
            
            return retVal;
        }

    
        public boolean isWeekday()
        {
            switch(this)
            {
            case Weekday:
            case Holiday:
            case Monday_Thursday:
            case Monday: case Tuesday: case Wednesday: case Thursday: case Friday:
            case Special_Service:
            case AllDays:
                 return true;
            }
            return false;
        }

        public boolean isSaturday()
        {
            switch(this)
            {
            case Saturday:
            case Weekend:
            case Holiday:
            case Special_Service:
            case AllDays:                
                 return true;
            }
            return false;
        }
        
        public boolean isSunday()
        {
            switch(this)
            {
            case Sunday:
            case Weekend:
            case Holiday:
            case Special_Service:
            case AllDays:
                 return true;
            }
            return false;
        }
    }
    
    
    
    /**
     * The purpose of DirType is to provide an enum that represents a direction of a route.  The system is really designed
     * for bi-directional routes.  That's not to say that the system won't work with a uni-directional / multi-directional
     * route.  Although when rendering an interactive web view on a uni-directional / multi-directional, you will probably
     * have the option of the inverse of the route (if you use the current html views). 
     * 
     * @author  Frank Purcell (purcellf@trimet.org)
     * @date    Nov 8, 2006
     * @project http://timetablepublisher.org
     * @version Revision: 1.0
     * @since   1.0
     */
    public static enum DirType 
    {
        Inbound(1),      Outbound(0),
        North(1),        South(0), 
        West(1),         East(0), 
        Northbound(1),   Southbound(0), 
        Westbound(1),    Eastbound(0), 
        Morning_Loop(1), Evening_Loop(0);

        private final int m_value;
        DirType(int value)             { this.m_value = value;     }
        public int value()             { return m_value;           }
        public boolean isSame(String s){ return this.toString().equals(s); }

        public boolean isSemanticallySame(DirType dir)
        {
            return isSemanticallySame(dir, false);
        }
        public boolean isSemanticallySame(DirType dir, boolean defVal)
        {
            if(dir == null) return defVal;
            return this.value() == dir.value(); 
        }
        public boolean isSemanticallySame(String dirStr)
        {
            DirType dir = construct(dirStr, null);
            return isSemanticallySame(dir);
        }

        
        public DirType getOpposite()   { return getOpposite(this);  }
        public static DirType getOpposite(DirType dir)
        {
            switch(dir) 
            {
                case Inbound:      return Outbound;
                case Outbound:     return Inbound;
                case Morning_Loop: return Evening_Loop;
                case Evening_Loop: return Morning_Loop;
                case North:        return South; 
                case South:        return North; 
                case West:         return East;
                case East:         return West;                
                case Northbound:   return Southbound; 
                case Southbound:   return Northbound; 
                case Westbound:    return Eastbound;
                case Eastbound:    return Westbound;                
            }

            return Inbound;             
        }
        public static DirType construct(int i)    { return i == DirType.Inbound.value() ? DirType.Inbound : DirType.Outbound; } 
        public static DirType construct(DirType d){ return d != null ? d : Inbound; }
        public static DirType construct(String s) { return construct(s, Inbound);   }
        public static DirType construct(String s, DirType defaultDir)
        {
            if(s == null) return   defaultDir;
            if(defaultDir == null) defaultDir = Inbound;
            
            // step 1: compare to see if we got text input (eg: Inbound, Oubound, etc...)
            DirType retVal = null;            
            try
            {
                s = s.trim();
                retVal = DirType.valueOf(s);
            }
            catch(Exception e)
            {
                retVal = null;
            }

            // step 1b: case insenstive compare 
            if(retVal == null)
            {
                String d = s.toUpperCase().trim();
                if(d != null)
                {
                    for(DirType k : DirType.values())
                    {
                        if(d.equals(k.name().toUpperCase()))
                        {
                            retVal = k;
                            break;
                        }
                    }
                }
            }

            // step 2: compare to see if we got a numeric input (1 or 0, etc...)
            if(retVal == null)
            {
                Integer d = IntUtils.getIntegerFromString(s);
                if(d != null)
                {
                    if(d == 1)
                        retVal = defaultDir;
                    else
                        retVal = defaultDir.getOpposite();
                }
            }

            // step 3: still null?  then assign default
            if(retVal == null)
            {
                retVal = defaultDir;
            }            
            
            return retVal;
        }
        public static DirType[] toDirArray(DirType d, DirType def)
        {
            DirType[] dirs;
            
            if(def == null) {
                def = Inbound;
            }

            if(d == null)
            {
                dirs = new DirType[2];
                dirs[0] = def;
                dirs[1] = def.getOpposite();
            }
            else
            {
                dirs = new DirType[1];
                dirs[0] = d;            
            }
            
            return dirs;
        }
        public static DirType[] getInboundOutbound() {return toDirArray(null, Inbound);}
        public static DirType[] getNorthSouth()      {return toDirArray(null, North);}
        public static DirType[] getEastWest()        {return toDirArray(null, East);}
        public static DirType[] getMorningEvening()  {return toDirArray(null, Morning_Loop);}
    }
    
    
    /**
     * The purpose of RouteType is to mirror the RouteType from the Google Transit Feed Spec.
     * 
     * @author  Frank Purcell (purcellf@trimet.org)
     * @date    Nov 8, 2006
     * @project http://timetablepublisher.org
     * @version Revision: 1.0
     * @since   1.0
     * @see     http://code.google.com/transit/spec/transit_feed_specification.htm
     */
    public static enum RouteType
    {
        Tram("0"), Subway("1"), Rail("2"), LightRail("L"), Bus("3"), Ferry("4"), CableCar("5"), Gondola("6"), Funicular("7");  
        
        private final String m_value;
        RouteType(String value)  { this.m_value = value; }
        public String value()    { return m_value;       }

        public static RouteType construct(String value) { return construct(value, Bus); }
        public static RouteType construct(String value, RouteType defaultKey)
        {
            RouteType retVal = null;
            
            if(value != null)
            {
                for(RouteType k : RouteType.values())
                {
                    if(value.equals(k.value()))
                    {
                        retVal = k;
                    }
                }
            }
            
            if(retVal == null)
            {
                try
                {
                    retVal = RouteType.valueOf(value);
                }
                catch(Exception e)
                {
                    retVal = defaultKey;
                }
            }
            return retVal;
        }
    }
}