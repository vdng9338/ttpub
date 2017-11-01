/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KeyValue
{
    final String m_key;
    final String m_value;
    
    public KeyValue(String key, String value)
    {
        m_key = key;
        m_value = value;
    }
    public KeyValue(Integer key, String value)
    {
        m_key   = key.toString();
        m_value = value;
    }
    public KeyValue(Short key, Integer value)
    {
        m_key   = key.toString();
        m_value = value.toString();
    }
    
    public static List<KeyValue> toList(String[] values)
    {
        if(values == null || values.length < 1) return null;
        
        List<KeyValue> retVal = new ArrayList<KeyValue>();
        for(String s : values)
        {
            retVal.add(new KeyValue(s, s));
        }
        
        return retVal;
    }
    
    public String getKey()
    {
        return m_key;
    }
    public Integer getKeyAsInt()
    {
        Integer retVal = null;
        try
        {
            retVal = Integer.parseInt(m_key);
        }
        catch(Exception e)
        {
            retVal = null;
        }
        
        return retVal;
    }
    public String getValue()
    {
        return m_value;
    }
    
    public static KeyValue find(Collection<KeyValue> kvList, String key)
    {
        KeyValue retVal = null;

        if(kvList != null && key != null)
        {
            for(KeyValue kv : kvList)
            {
               if(key.equals(kv.getKey()))
               {
                   retVal = kv;
                   break;
               }
            }            
        }
        
        return retVal;
    }
    
    public static KeyValue find(Collection<KeyValue> kvList, Integer key)
    {
        if(key == null) return null;
        return find(kvList, key.toString());
    }
    
    public static KeyValue findValue(List<KeyValue> kvList, String value)
    {
        KeyValue retVal = null;

        if(kvList != null && value != null)
        {
            for(KeyValue kv : kvList)
            {
               if(value.equals(kv.getValue()))
               {
                   retVal = kv;
                   break;
               }
            }            
        }
        
        return retVal;        
    }
}
