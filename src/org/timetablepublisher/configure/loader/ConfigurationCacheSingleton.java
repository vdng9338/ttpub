/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure.loader;

import java.io.File;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.FileUtils;
import org.timetablepublisher.utils.TimeTableProperties;


/**
 * The purpose of ConfigurationCacheSingleton is act as a singleton / store of multiple ConfigurationLoader objects.
 * The cache is indexed via the directory string of the given configuration.  If a requested config is not in the
 * cache, an attempt to load that config is made...and if the config exists, it's contents are inserted into the cache.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 22, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
 @SuppressWarnings("unchecked")
public class ConfigurationCacheSingleton implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(ConfigurationCacheSingleton.class.getCanonicalName());
    private static final ConfigurationCacheSingleton m_singleton = new ConfigurationCacheSingleton(); 

    private final Hashtable<String,ConfigurationLoader> m_data = new Hashtable<String,ConfigurationLoader>();
    private final String m_csvDefaultDir;
    private static String m_baseDir = "";
    
    protected ConfigurationCacheSingleton()
    {
        m_csvDefaultDir = TimeTableProperties.CONFIGURE_DIRECTORY.get();
        ConfigurationLoader cl = getData(m_csvDefaultDir);
        if(cl == null)
        {
            LOGGER.log(SEVERE, "ERROR: Couldn't find CSV directory: " + m_csvDefaultDir);
            LOGGER.log(DEBUG, " via property " + TimeTableProperties.CONFIGURE_DIRECTORY);
            new File(m_csvDefaultDir).mkdirs();
            cl = getData(m_csvDefaultDir);
        }
    }
   
    public static ConfigurationLoader getDefaultLoader()
    {
        return m_singleton.getData();
    }

    public static String cacheKey(String directory)
    {
        if(directory == null) return null;

        String retVal = directory;
        retVal = retVal.replaceAll("^.:", ""); // windows drive/directory crap
        retVal = retVal.replaceAll("\\W", ""); 
        return retVal;
    }
    
    public static ConfigurationLoader getLoader(String directory)
    {
        return m_singleton.getData(directory);
    }

    public static String getDefaultDir()
    {
        return (m_singleton != null) ? m_singleton.m_csvDefaultDir : null;
    }
    
    /** find, then delete a config from the cache, then reload the config from persistance store */
    public static ConfigurationLoader reLoader(ConfigurationLoader loader)
    {
        if(m_singleton == null || m_singleton.m_data == null) return null;
        
        String cDir = getDefaultDir();
        String cKey = cacheKey(getDefaultDir());        
        if(loader != null)
        {
            String tmpDir = loader.getCsvDir();
            String tmpKey = cacheKey(tmpDir);
            if(tmpKey != null && m_singleton.m_data.containsKey(tmpKey))
            {
                cDir = tmpDir;
                cKey = tmpKey;
            }            
        }
        
        if(cKey != null && m_singleton.m_data.containsKey(cKey))
        {
            m_singleton.m_data.remove(cKey);
        }
        
        return m_singleton.getData(cDir);
    }
    
   
    public ConfigurationLoader getData()
    {        
        if(m_csvDefaultDir == null) return null;
        return getData(m_csvDefaultDir);
    }
    
    public ConfigurationLoader getData(String directory)
    {
        if(m_data == null) return null;
        
        ConfigurationLoader retVal = null;
        String key = cacheKey(directory);
        if(key != null) 
        {
           retVal = m_data.get(key);
        }
        
        if(retVal == null)
        {
            File f = FileUtils.findDir(directory, m_baseDir);
            if(f != null)
            {
                String nDir = f.getAbsolutePath();
                retVal = new ConfigurationLoaderImpl(nDir);
                if(retVal != null)
                {
                    // put data into cache using the key the caller came in with...
                    if(key != null)
                        m_data.put(key, retVal);

            /* NOTE 5-14-07: 
                             dual entry f**ks up reload config (at least for the default config)...so I'm commenting out

                    // also put data into cache using the NEW KEY that may have been generated in lookup up the data
                    String nKey = cacheKey(nDir);
                    if(nKey != null && !nKey.equals(key))
                        m_data.put(nKey, retVal);
             */
                }                
            }
        }     
        
        return retVal;
    }

    public static String getBaseDir()
    {
        return m_baseDir;
    }

    public static void setBaseDir(String baseDir)
    {
        m_baseDir = baseDir;
    }    
}
