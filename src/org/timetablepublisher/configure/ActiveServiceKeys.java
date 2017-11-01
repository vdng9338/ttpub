/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.ArrayList;
import java.util.List;

import org.timetablepublisher.configure.loader.ConfigurationCacheSingleton;
import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.IntUtils;

/**
 * The purpose of ActiveServiceKeys is define what Service Keys from the scheduling data
 * map to what Service Key in the TimeTablePublisher.  This is a per-route configuration,
 * with most routes using the defaults:
 *    Weekday  = "W, F, f"
 *    Saturday = "S"
 *    Sunday   = "U"
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 15, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class ActiveServiceKeys extends Configure
{
    List<String> m_askList = null;
    
    @CsvColumn(name="Active Service Keys", details="List of Keys that pertain to this route / direction (eg: 1, 2, 3)", index=6)
    public String m_askString = "";

    public ActiveServiceKeys()
    {           
    }

    public ActiveServiceKeys(String agency)
    {
        m_agency = agency;
    }
    
    public ActiveServiceKeys(String agency, String route, String dir, String key)
    {
        super(agency, route, dir, key);
    }
    
    public ActiveServiceKeys(String agency, String route, String dir, KeyType key, String rawKeys)
    {
        super(agency, route, dir, key != null ? key.name() : KeyType.Weekday.name());
        setAskString(rawKeys);
    }

    public void setAskString(String val)
    {
        if(val == null) return;
        m_askString = val.trim();
        m_askList   = split(m_askString);
    }
    
    public String getAskString()
    {
        return m_askString;
    }
        
    public List<String> getAskList()
    {
        return m_askList;
    }

    
    synchronized public static List<String> process(TimesTable tt, KeyType key)
    {
        if(tt == null && key == null) return null;
        
        KeyType thisKey = key;
        if(thisKey == null) thisKey = tt.getKey();       
        if(thisKey == null) return null;
        
        List<String> retVal = null;
        if(tt != null) 
        {
            retVal = process(tt.getConfiguration(), tt.getAgencyName(), tt.getRouteID(), tt.getDir(), thisKey);
        }        
        if(retVal == null && thisKey != null)
        {
            retVal = thisKey.getValueList();
        }

        return retVal;
    }
    synchronized public static List<String> process(TimesTable tt)
    {
        if(tt == null || tt.getKey() == null) return null;
        if(tt.getConfiguration() == null || tt.bypassConfig()) return tt.getKey().getValueList();
        
        return process(tt.getConfiguration(), tt.getAgencyName(), tt.getRouteID(), tt.getDir(), tt.getKey());
    }

    synchronized public static List<String> process(ConfigurationLoader loader, String agency, String route, DirType dir, KeyType key)
    {    
        if(loader == null) return null;

        String dirStr = dir != null ? dir.toString() : null;
        String keyStr = key != null ? key.toString() : null;
        
        ActiveServiceKeys findME = new ActiveServiceKeys(agency, route, dirStr, keyStr);
        List askConfigs = loader.findAllData(findME);
        if(askConfigs == null || askConfigs.size() < 1) return null;

        List<String> retVal = new ArrayList<String>();
        for(ActiveServiceKeys ask : (List<ActiveServiceKeys>)askConfigs)
        {
            if(ask == null) continue;
            List<String> tmpList = ask.getAskList();
            if(tmpList != null && tmpList.size() > 0)
            {
                for(String s : tmpList)
                {
                    if(s == null) continue;
                    
                    String tmp = s.trim();
                    if(tmp.length() > 0 && !retVal.contains(tmp))
                    {
                        retVal.add(tmp);
                    }
                }
            }
        }
/*
        if(retVal == null || retVal.isEmpty())
        {
            if(key != null) {
                retVal = key.getValueList();
            }
        }
*/      
        return retVal;
    }
    
    synchronized public static String processReturnSQLString(int route, int dir, String key)
    {
        List<String> keys = process(ConfigurationCacheSingleton.getDefaultLoader(), "*", Integer.toString(route), DirType.construct(dir), KeyType.construct(key));
        return IntUtils.toSQLString(keys);
    }
    synchronized public static String processReturnSQLString(TimesTable tt, KeyType key)
    {
        List<String> keys = process(tt, key);
        return IntUtils.toSQLString(keys);
    }
    synchronized public static String processReturnSQLString(TimesTable tt)
    {
        return processReturnSQLString(tt, tt != null ? tt.getKey() : null);
    }

    
    public static boolean filterKey(List<String> actSvcKeys, String key)
    {
        if(key == null || key.length() < 1 || actSvcKeys == null || actSvcKeys.isEmpty()) return false;        
        return !actSvcKeys.contains(key);
    }
}