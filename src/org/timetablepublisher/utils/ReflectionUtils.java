/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.timetablepublisher.schedule.gtfs.loader.GTFSColumn;

public class ReflectionUtils implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(ReflectionUtils.class.getCanonicalName());    
    
    public static Method getMethod(Object obj, String prefix, Field f) throws SecurityException, NoSuchMethodException     
    {
        String fieldName = f.getName();
        return getMethod(obj, prefix, fieldName);
    }
    
    public static Method getMethod(Object obj, String prefix, String fieldName) throws SecurityException, NoSuchMethodException 
    {
        final Class[] setterParam = {String.class};
        String methodName = getBeanMethodName(prefix, fieldName);
        return obj.getClass().getMethod(methodName, setterParam);        
    }


    public static Object findReflectObject(Object that, String target)
    {
        Object retVal = null;
        String columnName = null;
        try
        {            
            Field[] fList = that.getClass().getFields();
            for(Field f : fList)
            {                
                if(f == null) continue;
                GTFSColumn a = f.getAnnotation(GTFSColumn.class);
                if(a == null) continue;

                columnName = a.name();
                if(columnName != null && target.equals(columnName))
                {
                    if(!a.useSetter() && String.class == f.getType())
                    {
                        f.setAccessible(true);
                        retVal = f;
                        break;
                    }
                    else
                    {
                        retVal = ReflectionUtils.getMethod(that, "set", f.getName());
                        if(retVal != null)
                            break;
                    }
                }
            }
        }
        catch (NoSuchMethodException nsm)
        {
            LOGGER.log(Constants.SEVERE, "looks like this class lacks a setter for element " + columnName, nsm);
            retVal = null;
        }
        catch (Exception e)
        {
            LOGGER.log(Constants.SEVERE, "generic reflection error", e);
            retVal = null;
        }
        
        return retVal;
    }
    
    public static String getBeanMethodName(String prefix, String name)
    {
        String tmp = name;
        if(name.startsWith("m_"))
        {
            tmp = name.substring(2);
        }
        else if(name.startsWith("_"))
        {
            tmp = name.substring(1);
        }
        
        if(prefix == null) prefix = "";
        
        return prefix + capitolize(tmp);
    }
    
    public static String capitolize(String name)
    {
        if(name == null || name.length() < 1) return "";
        
        String firstLetter = name.substring(0,1);   // get first letter
        String remainder   = name.substring(1);     // get remainder of word
        return firstLetter.toUpperCase() + remainder;
    }

    
    public static Class getClassFromString(String packageName, String cName)
    {
        Class retVal = null;
        try
        {
            String path = cName;
            
            // make sure the class name has a fully qualified path...if not, add one
            if(cName.indexOf(".") < 3) {
                path = packageName + "." + cName;
            }
            retVal = Class.forName(path);
        } 
        catch (ClassNotFoundException e)
        {
            LOGGER.log(DEBUG, "couldn't find Class for string " + cName, e);
        }
        
        return retVal;
    }    
}
