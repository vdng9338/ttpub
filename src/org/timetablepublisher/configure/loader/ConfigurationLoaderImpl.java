/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import org.timetablepublisher.configure.Configure;
import org.timetablepublisher.configure.TimePoints;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.IntUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


/**
 * The purpose of ConfigurationLoaderImpl is to load an instance of TTPUB's Configuration into memory, and
 * manage that data.  A single instance contains multiple files' worth of data.  And by creating an
 * array of ConfigurationLoader objects, you could potentially hold multiple configuration versions.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 20, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class ConfigurationLoaderImpl implements ConfigurationLoader, Constants
{
    private static final Logger LOGGER = Logger.getLogger(ConfigurationLoaderImpl.class.getCanonicalName());
    private static final String[] DEFAULT_FILES = new String[]{"ActiveServiceKeys.csv", "CullTrips.csv", "InterliningNotes.csv", "MergeTPNotes.csv", "PhantomTimePoint.csv", "RouteNames.csv", "TimePoints.csv", "ComboRoutes.csv", "EffectiveDate.csv", "LoopFillIn.csv", "PatternNotes.csv", "RenameTimePoint.csv", "RouteNotes.csv", "TripNotes.csv"};

    protected Hashtable<Class, List<Configure>> m_data   = new Hashtable<Class, List<Configure>>();
    protected Hashtable<Class, Boolean>         m_dirty  = new Hashtable<Class, Boolean>();
    protected Hashtable<Class, Boolean>         m_ignore = new Hashtable<Class, Boolean>();
    protected String m_csvDir = null;

    private static void copyResource(String resourceName, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            //is = new FileInputStream(source);
            is = ConfigurationLoaderImpl.class.getResourceAsStream(resourceName);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public ConfigurationLoaderImpl(String csvDir)    
    {
        setCsvDir(csvDir);
        LOGGER.log(DEBUG, "Loading configuration from " + csvDir);
        
        // Copy defaults if needed
        String defaultPath = "/org/gtfs/blank-config/";
        for(String s : DEFAULT_FILES) {
            File to = new File(csvDir, s);
            if(!to.exists())
                try {
                    copyResource(defaultPath + s, to);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        
        FileReader reader = null;
        try 
        {              
            for(Class c : CSV)
            {
                // step 1: open CSV file
                String file = getAbsoluteCsvPath(c);
                reader = new FileReader(file);

                // step 2: iterate through this data, loading it into a List
                List<Configure> l = new ArrayList<Configure>();
                for(Object o : load(c, reader))
                {
                    l.add((Configure)o);
                }               
                
                // step 3: add that List of csv data to HashTable & close csv file
                m_data.put(c, l);
                if(reader != null) reader.close();
                
                // sort
                if(c == TimePoints.class && l.size() > 1) 
                {
                    Collections.sort(l, new TimePoints());
                }
            }
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, "VERY VERY BAD: couldn't load part of the configuration...so we're punting\n" + e.toString());
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
    

    public boolean isDirty()
    {
        if(m_dirty != null && m_dirty.size() > 0) 
            return true;
        else
            return false;
    }
    
    public boolean isDirty(Class c)
    {
        if(c == null || m_dirty == null) return false;
        
        if(m_dirty.get(c) != null)
            return true;

        return false;        
    }
    

    public void unDirty(Class c)
    {
        try
        {
            m_dirty.remove(c);
        }
        catch(Exception e)
        {            
        }
    }

    
    public boolean isDirty(String className)
    {
        return isDirty(findClass(className));
    }
   
    public String getAbsoluteCsvPath(Class c)
    {
        return m_csvDir + c.getSimpleName() + ".csv";
    }

    public String getCsvDir()
    {
        return m_csvDir;
    }

    public void setCsvDir(String csvDir)
    {
        m_csvDir = csvDir;
        
        if(!(m_csvDir.endsWith("\\") || m_csvDir.endsWith("/")))
        {
            m_csvDir += "/";
        }        
    }    

    
    /**
     * CSV File Saving Routine
     * 
     */
    synchronized public void persist() throws IOException
    {
        if(m_dirty == null || m_dirty.size() <= 0) return;
        
        try
        {           
            for(Class c : CSV)
            {
                if(m_dirty.containsKey(c))
                {
                    List<Configure> rows = m_data.get(c);
                    String[] columnNames = rows.get(0).getCsvHeader();
                    m_dirty.remove(c);
                    writer(this, rows, columnNames, getAbsoluteCsvPath(c));
                }                
            }
            
        }
        catch (IOException e)  // catch is JUST for logging...continue to throw this exception
        {
            LOGGER.warning(e.toString());
            throw e;  
        }
        
    }
    
    
    synchronized public void writer(ConfigurationLoader loader, List<Configure> rows, String[] columnNames, String csvFile) throws IOException     
    {        
        // copy existing file to backup file
        File f = new File(csvFile);
        if(f.exists()) 
        {
            File bkup = new File(csvFile + "_bkup_" + Constants.dateTimeSDF.format(new Date()) + ".csv");
            LOGGER.log(DEBUG, "File Backup: " + csvFile + " to " + bkup.getName()); 
            f.renameTo(bkup);
            bkup = null;
        }
        f = null;
        
        // write out new data to csv file
        FileWriter fw    = new FileWriter(csvFile);
        CSVWriter writer = new CSVWriter(fw);      
        writer.writeNext(columnNames);
        for(Configure t : rows)
        {            
            writer.writeNext(t.getCsvValues());
        }
        
        // close stuff
        writer.close();
        fw.close();
        writer = null;
        fw     = null;
    }
    
    public Integer newConfig(Configure obj)
    {
        Integer hashCode = null;
        
        String name = null;
        try
        {
            Class c   = obj.getClass();
            name      = c.getSimpleName();
            List data = m_data.get(c);
            
            // and now add this object to our list
            data.add(obj);
            setDirty(c);
            hashCode = obj.hashCode();
        } 
        catch(Exception e)
        {
            LOGGER.log(SEVERE, "couldn't add to Configuration List a 'created' type object " + name, e);
        }
        
        return hashCode;
    }
    
    public Integer newConfig(String className, Configure template)
    {
        Integer hashCode = null;

        Class c = findClass(className);
        if(c == null || m_data == null) return null;
       
        // make sure we have a list we can add too
        List data = m_data.get(c);
        if(data == null) return null;
        
        // create a new element of this type (based on className)
        Configure obj = null;
        try
        {
            // create the object via reflection
            obj = (Configure)c.newInstance();
            obj.setValues(template);
            hashCode = obj.hashCode();
        } 
        catch(Exception e)
        {
            LOGGER.log(SEVERE, "couldn't create  object of type " + className, e);
        }

        try
        {
            // and now add this object to our list
            if(obj != null) 
            {
                data.add(obj);
                setDirty(c);
            }
        } 
        catch(Exception e)
        {
            LOGGER.log(SEVERE, "couldn't add to Configuration List a 'created' type object " + className, e);
        }

        return hashCode;
    }

    public boolean deleteConfig(Configure removeMe)
    {
        boolean retVal = false;
        if(removeMe != null)
        {
            Class c = removeMe.getClass();
            List<Configure> list = m_data.get(c);
            if(list != null) 
            {
                retVal = list.remove(removeMe);
                setDirty(c);
            }
        }
        return retVal;
    }

    public void addData(List data)    
    {
        if(data == null || data.size() < 1 || data.get(0) == null) return;
        
        // find the subclass of Index
        Class c = data.get(0).getClass();
        
        // with that class type (our key into the hash), find the list and add that data
        List in = m_data.get(c);
        if(in != null)
        {
            in.addAll(data);
            setDirty(c);
        }
    }
        
    public void removeData(List removeMe)
    {
        if(removeMe == null || removeMe.size() < 1 || removeMe.get(0) == null) return;
        
        // find the subclass of Index
        Class c = removeMe.get(0).getClass();

        // with that class type (our key into the hash), find the list and add that data
        List list = m_data.get(c);
        if(list != null)
        {
            for(Object del : removeMe)
            {
                if(del != null) 
                { 
                    list.remove(del);
                    setDirty(c);
                }
            }
        }        
    }

    public void setDirty(Class c)
    {
        if(m_dirty == null) m_dirty = new Hashtable<Class, Boolean>();
        m_dirty.put(c, Boolean.TRUE);
    }

    public Hashtable<Class, List<Configure>> getData()
    {
        return m_data;
    }
    public List<Configure> getData(Class c)
    {
        if(c == null || m_data == null) return null;
        return m_data.get(c);
    }

    public List<Configure> getData(String className)
    {
        return getData(findClass(className));
    }

    
    
    //
    // FINDER ROUTINES    
    //    
    
    public Class findClass(String className)
    {
        if(className == null) return null;
        
        Class retVal = null;
        
        for(Class c : CSV)
        {            
            if(className.equals(c.getSimpleName()))
            {
                retVal = c;
                break;
            }
        }
        
        return retVal;
    }
    public Class findClass(Object obj)
    {
        if(obj == null) return null;
        return findClass(obj.getClass().getSimpleName());
    }

    
    public List<Configure> findAllData(Configure findMe)
    {
        return findAllData(findMe, false);
    }
    public List<Configure> findAllData(Configure findMe, boolean includeIgnores)
    {
        List<Configure> retVal = new ArrayList<Configure>();
        if(findMe == null || m_data == null) return retVal;
        
        List<Configure> list = m_data.get(findMe.getClass());        
        if(list != null)
        {
            for(Configure in : list)
            {
                if(findMe.equals(in) && (!in.isIgnore() || includeIgnores))
                {
                    retVal.add(in);
                }
            }
        }
        return retVal;
    }


    
    public Configure findData(Configure findMe)
    {
        return findData(findMe, false);
    }
    public Configure findData(Configure findMe, boolean includeIgnores)
    {
        Configure retVal = null;        
        if(findMe == null || m_data == null) return retVal;
        
        List<Configure> list = m_data.get(findMe.getClass());        
        if(list != null)
        {
            for(Configure in : list)
            {
                if(findMe.equals(in) && (!in.isIgnore() || includeIgnores))
                {
                    retVal = in;
                    break;
                }
            }
        }
        return retVal;
    }
    
    public Configure findData(String className, String hashStr)
    {
        int hashCode = IntUtils.getIntFromString(hashStr);        
        return findData(className, hashCode);
    }    
    public Configure findData(String className, int hashCode)
    {
        Class c = findClass(className);
        if(c == null || m_data == null) return null;
        
        Configure retVal = null;
        List<Configure> list = m_data.get(c);
        if(list != null)
        {
            for(Configure in : list)
            {
                if(in.hashCode() == hashCode)
                {
                    retVal = in;
                    break;
                }
            }
        }
        
        return retVal;
    }
       
    /**
     * this method, as opposed to findAllData above, works primarily on the Configure data elements
     * 
     * @param findMe
     * @return List<Configure>...where the list holds multiple types, subtyped off of Configure
     */
    public List<Configure> findAllIndicies(Configure findMe)
    {
        List<Configure> retVal = new ArrayList<Configure>();
        if(findMe == null || m_data == null) return retVal;
        
        Collection<List<Configure>> list = m_data.values();
        if(list != null)
        {
            for(List<Configure> out : list)
            {
                if(out == null) continue;
                for(Configure in : out)
                {
                    if(in == null) continue;
                    if(findMe.equals(in))
                    {
                        retVal.add(in);
                    }                    
                }
            }
        }
        return retVal;
    }
    
    
    
    /**
     * simple method to return names of the CSV files/classes
     * 
     * @return 
     */
    public List<String> getCsvNames()
    {
        List<String> retVal = new ArrayList<String>();
    
        for(Class c : CSV)
        {            
            retVal.add(c.getSimpleName());
        }
        
        return retVal;
    }
       
    
    
    /**
     * This method is the heart of the (csv file based) loader.  It will load the data from a CSV file.  The results of reading
     * the file land into a List of String Arrays (eg: List<String[]>).  From there, we create the object that will store this data, 
     * and we call the routine 'setValues' (part of the Configure interface, which all object implement) to move the data into the object.
     * 
     * The way we know what type to instantiate comes from the Class<T> parameter.  We pass the class (defining the type) to this 
     * routine.  And from that, we use reflection to instantiate, and setValues to populate.  
     * 
     * @param <T>
     * @param c
     * @param csvFile
     * @return List<T>
     * @throws Exception
     * @note   assumes that the caller will close the Reader 
     */
    synchronized public static <T extends Configure> List<T> load(Class<T> c, Reader csvFile) 
        throws Exception
    { 
        List<T> result = new ArrayList<T>();
        
        // note -- assume that the caller will close this reader...do not close the CSVReader
        CSVReader reader = new CSVReader(csvFile, ',', '"', 1);
        List<String[]> allElements = reader.readAll();
                    
        int noteCount = 0;        
        for(String[] csv : allElements)
        {
            if(csv == null || csv.length < 3) continue;
            
            String   agency     = csv[0];
            String[] routes     = csv[1].split(","); 
            String[] directions = csv[2].split(",");  
            String[] keys       = csv[3].split(",");  
            String   lang       = csv[4];

            noteCount++;
            
            // the (multiple) INSERT INTO OS_INDEX and OS_TABLE_RULE
            for(String r : routes)
            {
                for(String d : directions)
                {
                    for(String k : keys)
                    {
                        T item = c.newInstance();
                        item.setValues(agency, r, d, k, lang, csv);                                                
                        result.add(item);
                    }
                }
            }
        }
        
        return result;
    }


    public boolean ignoreConfig(Class configClass)
    {   
        if(configClass == null) return false;
        
        Object o = m_ignore.get(configClass);
        if(o != null && o.equals(Boolean.TRUE)) 
        {
            return true;
        }
            
        return false;
    }

    public void setIgnoreConfig(Class configClass)
    {
        m_ignore.put(configClass, true);
    }

    public void setIgnoreConfig(Class configClass, boolean value)
    {
        m_ignore.put(configClass, value);
    }
}
