package org.timetablepublisher.configure;

import java.util.ArrayList;
import java.util.List;

import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.CellImpl;
import org.timetablepublisher.table.TimesTable;


/**
 * The purpose of SvcKeyNormalizer is to remap multiple trips that may have keys of X & x (which would
 * represent parts of a week...when taken together are the whole week) to a base service key.
 * 
 * For example, the TriMet schedule has keys three weekday keys of 'W', 'f' and 'F', where
 * 'W' = Weekday, 'F' = Friday only, 'f' = Mon-Thur.  Our schedule, in places, contains two trips 
 * that start at the same time, with an 'F' and 'f' key.  Combined, these two trips are really a 
 * Weekday service key.  In other places, there's only one trip with key of 'F', which distinguishs
 * that as a Fridy only trip, since it lacks it's 'f' companion trip. 
 *
 * NOTE: this utility class will need to be explicity called.  Unlike other configurations, it's not
 * part of the standard call stack.   See TransQueryUtils.java for how it's used.  
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Jul 3, 2007
 * @project ttpub
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class SvcKeyNormalizer
{
    final public List<Keys> m_keys = new ArrayList<Keys>();
        
    public static class Keys
    {
        public String baseKey;
        public String keyX;
        public String keyY;
        
        public Keys(List<String> keys, int i)
        {
            try
            {
                baseKey = keys.get(i);
                keyX    = keys.get(i+1);
                keyY    = keys.get(i+2);                
            }
            catch(Exception e)
            {                
            }
        }
        
        public static void constructor(List<Keys> keyList, List<String> keys)
        {
            if(keyList == null || keys == null) return;            
            for(int i = 0; i+3 <= keys.size(); i+=3)
            {
                keyList.add(new Keys(keys, i));    
            }
        }
        
        public boolean isBase(String k)
        {
            return (k != null && k.equals(baseKey));
        }
        public boolean isSiblingX(String k)
        {
            return (k != null && k.equals(keyX));
        }
        public boolean isSiblingY(String k)
        {
            return (k != null && k.equals(keyY));
        }
        
        public boolean isSibling(String k)
        {
            return (k != null && (k.equals(keyX) || k.equals(keyY)));
        }
        public boolean isRelated(String k)
        {
            return isBase(k) || isSibling(k);
        }
    }
    
    public SvcKeyNormalizer(TimesTable tt)
    {
        ActiveServiceKeys findME = new ActiveServiceKeys(tt.getAgencyName(), tt.getRouteID(), tt.getDir().name(), tt.getKey().name());
        List askConfigs = tt.getConfiguration().findAllData(findME);
        if(askConfigs == null || askConfigs.size() < 1) return;

        for(ActiveServiceKeys ask : (List<ActiveServiceKeys>)askConfigs)
        {
            if(ask == null) continue;
            Keys.constructor(m_keys, ask.getAskList());
        }
    }
    
    public static boolean isEqual(String keyA, String keyB)
    {
        if(keyA == null || keyB == null) return false;
        return keyA.equals(keyB);
    }
    
    public boolean isParentChild(String keyA, String keyB)
    {
        boolean retVal = false;
        
        for(Keys k : m_keys)
        {
            if(k.isRelated(keyA) && k.isRelated(keyB))
            {
                if(!k.isSibling(keyA) || !k.isSibling(keyB))
                    retVal = true;
                
                break;
            }
        }
        
        return retVal;
    }

    public boolean isRelated(String keyA, String keyB)
    {
        boolean retVal = false;
        
        for(Keys k : m_keys)
        {
            if(k.isRelated(keyA) && k.isRelated(keyB))
            {
                retVal = true;
                break;
            }
        }
        
        return retVal;
    }
    
    public boolean isRelatedAndNotEqual(String keyA, String keyB, String keyC)
    {
        if(keyA == null || keyB == null || keyC == null) return false;
        if(isEqual(keyA, keyB) || isEqual(keyA, keyC)) return false;
        return isRelated(keyA, keyB) && isRelated(keyA, keyC);
    }

    public boolean isRelatedAndNotSiblings(String keyA, String keyB)
    {
        if(isRelated(keyA, keyB))
        {
            return (isEqual(keyA, keyB) || isParentChild(keyA, keyB)); 
        }

        return false;
    }

    
    public void normalizeServiceKey(List<Cell> trip)
    {
        if(trip != null && m_keys.size() > 0)
        {
            for(Keys k : m_keys)
            {
                CellImpl.nomalizeServiceKey(trip, k.baseKey, k.keyX, k.keyY);                
            }
        }
    }
}
