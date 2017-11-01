/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs.loader;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Field;

import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import au.com.bytecode.opencsv.CSVReader;

import org.timetablepublisher.configure.ActiveServiceKeys;
import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.schedule.gtfs.Agency;
import org.timetablepublisher.schedule.gtfs.Calendar;
import org.timetablepublisher.schedule.gtfs.GTFSData;
import org.timetablepublisher.schedule.gtfs.GTFSDataUtils;
import org.timetablepublisher.schedule.gtfs.Routes;
import org.timetablepublisher.schedule.gtfs.StopTimes;
import org.timetablepublisher.schedule.gtfs.Stops;
import org.timetablepublisher.schedule.gtfs.Trips;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.FileUtils;
import org.timetablepublisher.utils.ReflectionUtils;
import org.timetablepublisher.utils.TimeTableProperties;


/**
 * The purpose of GDataFileLoader is to load data from CSV files in the Google Data format into GData data object.  
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 8, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 * @see     http://code.google.com/transit/spec/transit_feed_specification.htm
 */
@SuppressWarnings("unchecked")
public class GTFSDataLoader implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(GTFSDataLoader.class.getCanonicalName());
    private static GTFSDataLoader m_singleton;
    private static String m_csvDir  = null;
    private static String m_baseDir = "";
    
    private final Hashtable<Agency, Hashtable<Class, List<GTFSData>>> m_data;
    private Class[] CSV = {
            Stops.class,  
            Routes.class,  
            Trips.class,
            StopTimes.class,
            Calendar.class
     };

    
    protected GTFSDataLoader()
    {
        m_csvDir = TimeTableProperties.SCHEDULE_DIRECTORY.get(".");
        
        File dataDir = FileUtils.findDir(m_csvDir, m_baseDir);
        m_data = new Hashtable<Agency, Hashtable<Class, List<GTFSData>>>();
        FileReader reader = null;
        try 
        {
            List<File> files = FileUtils.findFiles(dataDir, Agency.getFileName());
            for(File f : files)
            {
                // step 1: load the agency file 
                reader = new FileReader(f);
                List<GTFSData> agencyData = new ArrayList<GTFSData>();
                for(Object o : loadCsv((Class)Agency.class, reader))
                {
                    agencyData.add((GTFSData)o);
                }               
                if(agencyData.size() < 1)
                {
                    LOGGER.log(Constants.DEBUG, "couldn't find an agency record");
                    continue;
                }
                if(reader != null)
                {
                    reader.close();
                    reader = null;
                }
                
                // step 2: create the record store for this agency's schedule data 
                Agency a = (Agency)agencyData.get(0);
                Hashtable<Class, List<GTFSData>> ht = new Hashtable<Class, List<GTFSData>>();
                m_data.put(a, ht);

                // step 3: load the schedule data for this agency
                for(Class c : CSV)
                {                   
                    String csvPath = FileUtils.getFilePath(f, c);
                    try
                    {
                        // step 3a: open one (of the many) CSV file(s)
                        reader = new FileReader(csvPath);
                    } 
                    catch (Exception e)
                    {
                        LOGGER.log(Constants.DEBUG, "agency " + a.getName() + " lacks file " + csvPath, e);
                        continue;
                    }

                    // step 3: iterate through this file, loading the data into a List
                    List<GTFSData> l = new ArrayList<GTFSData>();
                    for(Object o : loadCsv(c, reader))
                    {
                        l.add((GTFSData)o);
                    } 
                    
                    // step 4: put it into the hashtable
                    ht.put(c, l);
                    if(reader != null)
                    {
                        reader.close();
                        reader = null;
                    }
                }                
            }
            
            // commented out, since too slowww on startup...  organize();
        }
        catch(Exception e)
        {
            LOGGER.log(Constants.SEVERE, "very bad error loading schedule data.", e);
        }
        finally
        {
            if(reader != null)
            {
                try { reader.close(); } catch(Exception e) {}
                reader = null;
            }
        }
    }

    public static String getCsvDir()
    {
        return m_csvDir;
    }

    public static void setCsvDir(String dir)
    {
        m_csvDir = dir;
    }
    
    public static String getBaseDir()
    {
        return m_baseDir ;
    }

    public static void setBaseDir(String baseDir)
    {
        m_baseDir = baseDir;
    }   
    
    public synchronized static GTFSDataLoader loader()
    {
        if(m_singleton == null)
        {
            m_singleton = new GTFSDataLoader();
        }
        return m_singleton;
    }
    
    public synchronized static void reloader()
    {
        m_singleton = new GTFSDataLoader();
    }

    public synchronized static void fixDirection(String agency)
    {
        if(m_singleton == null) return;
        
        List tmp = m_singleton.getData(agency, Trips.class);
        List<Trips> tList = tmp;    
        
        if(tList != null)    
        for(Trips t : tList)
        {
            if(t == null) continue;
            if(t.getDir() == null)
                t.setDir("1");
        }
    }
    
    
    /**
     * organize -- rearrange data into more efficient query structures
     * 
     * NOTE: this is currently not called, since it's way inefficient at startup for large datasets.
     * 
     * @date May 9, 2007
     */
    protected void organize()
    {
        // organize stop times into buckets -- much faster query than having to sort a list for every trip in a table 
        Set<Agency> agencyList = getAgencies();
        if(agencyList != null)
        {
            for(Agency a : agencyList)
            {
                if(a == null) continue;
                List tList = getData(a, Trips.class);
                List sList = getData(a, StopTimes.class);
                if(tList != null && sList != null)
                {
                    for(Trips t :(List<Trips>) tList)
                    {
                        if(t != null)
                        {
                            List<StopTimes> stl = GTFSDataUtils.getTripTimes(sList, t.getTripID());
                            t.setStopTimes(stl);
                        }
                    }
                }
            }
        }
    }
    
    public Set<Agency> getAgencies()
    {
        if(m_data == null || m_data.keySet() == null || m_data.keySet().isEmpty()) 
            return null;
        
        return m_data.keySet();
    }
    
    public List<String> getAgencyNames()
    {                
        List<String> retVal = new ArrayList<String>();
        for(Agency a : m_data.keySet())
        {
            if(a == null || a.getName() == null) continue;
            retVal.add(a.getName());
        }
        
        return retVal;
    }

    public Agency getAgency(String name)
    {
        Agency retVal = null;
        for(Agency a : m_data.keySet())
        {
            if(a == null || a.getName() == null) continue;
            if(a.getName().equals(name))
            {
                retVal = a;
                break;
            }
        }
               
        return retVal;
    }

    public Agency getAgency(String name, String def)
    {
        Agency retVal = getAgency(name);
        if(retVal == null)
            retVal = getAgency(def);
        
        return retVal;
    }
    
    
    public Hashtable<Class, List<GTFSData>> getData(Agency a)
    {
        if(a == null) return null;
        return m_data.get(a);
    }

    public Hashtable getData()
    {
        return getData(getAgency(TimeTableProperties.DEFAULT_AGENCY.get())); 
    }
    
    public List<GTFSData> getData(Agency a, Class c)
    {
        List<GTFSData> retVal = null;
        if(a == null || c == null) 
            return null;
        
        Hashtable<Class, List<GTFSData>> agencyData = getData(a);
        if(agencyData != null)
        {
            retVal = agencyData.get(c);
        }
        
        return retVal;
    }

    public List<GTFSData> getData(String agency, Class c)
    {
        if(agency == null || c == null)
            return null;
        
        return getData(getAgency(agency), c);
    }

    public List<GTFSData> getData(Class c)
    {
        return getData(getAgency(TimeTableProperties.DEFAULT_AGENCY.get()), c); 
    }    
    
    /**
     * @param  .class of the type you want to create (must have Index.class a parent)
     * @param  csvFile you want to load
     * @return List of the Type you specify, populated with values from the CSV file
     * @throws Exception
     */
    public static <T extends GTFSData> List<T> loadCsv(Class<T> c, Reader csvFile) 
        throws Exception
    { 
        List<T> result = new ArrayList<T>();
        
        CSVReader reader = new CSVReader(csvFile, ',', '"', 0);
        String[] header = reader.readNext();
        Map<Object, Integer> headMap = new Hashtable<Object, Integer>();
        for(int i = 0; i < header.length; i++)
        {
            Object r = ReflectionUtils.findReflectObject(c.newInstance(), header[i]);
            if(r != null)
                headMap.put(r, i); 
        }

        for(String[] csv = reader.readNext(); csv != null; csv = reader.readNext()) 
        {
            if(csv.length < 3) continue;
            
            T item = c.newInstance();
            boolean ok = item.setFieldValues(headMap, csv); 
            if(ok) 
            {
                result.add(item);
            }
        }
        
        return result;
    }


    /**
     * setDefaultSvcKeys will look at a the current ActiveServiceKey configuration.  If nothing's been configured,
     * then we'll assign these RAW service keys to the ActiveServiceKey table for all svc keys... 
     *
     * @param config
     * @param data void
     * @date May 4, 2007
     */
    public static void setDefaultSvcKeys(ConfigurationLoader config, GTFSDataLoader data)
    {
        if(config == null || data == null || data.getAgencies() == null) return;
        for(Agency a : data.getAgencies())
        {
            if(a == null || a.getName() == null) continue;
            setAgencySvcKeys(a.getName(), config, data);
        }
    }
    
    public static void setAgencySvcKeys(String agency, ConfigurationLoader config, GTFSDataLoader data)
    {
        if(agency == null || config == null || data == null) return;
        
        ActiveServiceKeys findME = new ActiveServiceKeys(agency);
        List<ActiveServiceKeys> askConfigs = config.findAllData(findME);
        if(askConfigs != null && askConfigs.size() > 0)
        {
            LOGGER.log(SEVERE, "There's already an existing ActiveServiceKey config -- I'm not going to overwrite that, so I'm exiting now without loading any data from your GTFS."
                             + "  If you want to get the GTFS keys into memory, delete all rows (except the 1'st / banner row) from ActiveServiceKeys file." );
            return;
        }

        String week = "W";
        String sat  = "S";
        String sun  = "U";
        
        List tmp = data.getData(agency, Calendar.class);
        List<Calendar> calList = tmp; // 2-line cast :-) 
        if(calList != null && calList.size() > 0)
        {
            for(Calendar c : calList)
            {
                if(c == null || c.getServiceKey() == null) continue;
                
                if(c.getServiceKey().isWeekday())
                {
                    week += ", " + c.getRawKey();
                }
                if(c.getServiceKey().isSaturday())
                {
                    sat  += ", " + c.getRawKey();
                }
                if(c.getServiceKey().isSunday())
                {
                    sun  += ", " + c.getRawKey();
                }
            }
        }
        
        // load service key into configuration 
        ActiveServiceKeys wKey = new ActiveServiceKeys(agency, "*", "*", KeyType.Weekday,  week);
        ActiveServiceKeys sKey = new ActiveServiceKeys(agency, "*", "*", KeyType.Saturday, sat);
        ActiveServiceKeys uKey = new ActiveServiceKeys(agency, "*", "*", KeyType.Sunday,   sun);
        config.newConfig(wKey); 
        config.newConfig(sKey); 
        config.newConfig(uKey); 
    }    
}


