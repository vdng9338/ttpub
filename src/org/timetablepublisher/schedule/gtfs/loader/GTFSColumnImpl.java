/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs.loader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.timetablepublisher.schedule.gtfs.Agency;
import org.timetablepublisher.schedule.gtfs.GTFSData;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.ReflectionUtils;

/**
 * The purpose of GTFSColumnImpl is to provide Annotation / Reflectio support methods
 * to those classes that use the GTFSColumn anntation.  The routines allow the reading
 * of the annotation, their associated fields, and an objects' value of that field. 
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 * @see     org.timetablepublisher.view.web.CSVEditorServlet
 * @see     org.timetablepublisher.configure.Configure
 */
public class GTFSColumnImpl // NOTE: do not implement Constants on this class...it will fudge up the loader
{
    private static final Logger LOGGER = Logger.getLogger(GTFSColumnImpl.class.getCanonicalName());    

    //
    // Annotation Getters/Setters/Handlers
    //
    public String[] getCsvHeader()
    {
        List<String> s = getFieldNames();
        if(s == null || s.size() < 1) return null;

        String[] retVal = new String[s.size()];
        if(s != null)
        {
            s.toArray(retVal);
        }
        
        return retVal; 
    }

    
    public String[] getCsvValues()
    {
        List<String> s = getFieldValues(getCsvFields());
        if(s == null || s.size() < 1) return null;
        
        String[] retVal = new String[s.size()];
        if(s != null)
        {
            s.toArray(retVal);
        }
        
        return retVal; 
    }
    
    public List getFieldValues()
    {
        return getFieldValues(getCsvFields());
    }
    public List<String> getFieldValues(List<GTFSColumn> cols)
    {
        if(cols == null) return null;
        
        List<String> retVal = new ArrayList<String>();
        for(GTFSColumn c : cols)
        {
            if(c == null) continue;
            
            Object obj = getFieldValue(c);
            if(obj != null) {
                retVal.add(obj.toString());
            }
            else {
                retVal.add(null);
            }
        }
        
        return retVal;
    }
    
    
    public Object getFieldValue(GTFSColumn col)
    {
        if(col == null) return null;
        
        Object retVal = "";
        try
        {
            Field[] fList = this.getClass().getFields();
            for(Field f : fList)
            {
                if(f == null) continue;
                GTFSColumn a = f.getAnnotation(GTFSColumn.class);
                if(a == null) continue;
                
                if(a.equals(col))
                {
                    retVal = f.get(this); 
                    break;
                }
            }
        } 
        catch (Exception e)
        {
            LOGGER.log(Constants.SEVERE, "error getting field for GTFSColumn " + col.name(), e);
        }
        
        return retVal;
    }

    private static String getCsvValue(Map<String, Integer> header, String[] csv, String name)
    {
        String retVal = null;
        
        Integer i = null;
        try
        {
            i = header.get(name);
            if(i == null)
            {
                LOGGER.log(Constants.DEBUG, "NOTE: this fead does not support the csv element " + name);
            }
            else
            {
                retVal = csv[i].trim();
            }
        }
        catch(Exception e)
        {
            LOGGER.log(Constants.DEBUG, "Access csv field " + name + " (csv[" + i + "]) is out of bounds from the file data, which only has " + csv.length + " elements.  This might be nothing...null values are allowed in the config files.");
            retVal = null;
        }
        
        return retVal;
    }
    
    
    // TODO: to improve speed...stop creating Field[] fList...do that a layer above...acutally, make header == Map<Field, Integer>
    public boolean setFieldValues(Map<Object, Integer> header, String[] csv)
    {
        if(header == null || header.size() < 1) return false;
        if(csv == null    || csv.length < 1)    return false;
        
        boolean retVal   = false; 
        String csvValue  = null;        
        try
        {
            Set rSet = header.keySet();
            for(Object ref : rSet)
            {
                Integer i = header.get(ref);
                if(i == null || i >= csv.length) continue;
                
                csvValue  = csv[i].trim();                
                if(csvValue == null || csvValue.length() < 1) continue;
                
                if(ref instanceof Field)
                {
                    Field f = (Field) ref;
                    f.set(this, csvValue);
                    retVal = true;
                }
                else if (ref instanceof Method)
                {
                    Method r = (Method) ref;
                    r.invoke(this, new Object[]{csvValue});
                    retVal = true;
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.log(Constants.SEVERE, "generic reflection error", e);
        }
        
        return retVal;
    }  
    
/*  TO DO if you ever write an HTML editor for GTFS
 * 

    public boolean setFieldValues(HttpServletRequest req, String prefix)
    {
        boolean retVal = false;
        if(req == null) return retVal;
        
        String reqParam  = "";
        String fieldName = "";
        try
        {
            Field[] fList = this.getClass().getFields();
            for(Field f : fList)
            {                
                if(f == null) continue;
                GTFSColumn a = f.getAnnotation(GTFSColumn.class);
                if(a == null) continue;
                reqParam = req.getParameter(prefix + a.index());
                if(reqParam != null)
                {
                    fieldName   = f.getName();                        // get the field name
                    Method meth = ReflectionUtils.getMethod(this, "set", fieldName);  // get the setter method                    
                    meth.invoke(this, reqParam);                      // set the field with http param
                    retVal = true;
                }
            }
        }
        catch (NoSuchMethodException nsm)
        {
            LOGGER.log(SEVERE, "looks like this class lacks a setter for element " + fieldName, nsm);   
        }
        catch (Exception e)
        {
            LOGGER.log(SEVERE, "error setting field with variable " + reqParam, e);
        }

        return retVal;
    }
*/
    
    public static List<GTFSColumn> getCsvFields(String cName)
    {
        Class c = ReflectionUtils.getClassFromString(Agency.class.getPackage().getName(), cName);
        return getCsvFields(c);
    }
    
    public List<String> getFieldNames()
    {
        return getFieldNames(this.getClass());
    }
    
    public List<GTFSColumn> getCsvFields()
    {
        return getCsvFields(this.getClass());
    }
    
    public static List<String> getFieldNames(Class c)
    {
        if(c == null) return null;
        
        List<String> retVal = new ArrayList<String>();        
        for(GTFSColumn csv : getCsvFields(c))
        {
            retVal.add(csv.name());
        }
        
        return retVal;
    }
    
    public static List<String> getFieldNames(String cName)
    {
        Class c = ReflectionUtils.getClassFromString(GTFSData.class.getPackage().getName(), cName);
        return getFieldNames(c);        
    }

    public static List<GTFSColumn> getCsvFields(Class c)
    {
        if(c == null) return null;
        
        List<GTFSColumn> retVal = new ArrayList<GTFSColumn>();
        try
        {
            Field[] fList = c.getFields();
            for(Field f : fList)
            {
                if(f == null) continue;
                GTFSColumn a = f.getAnnotation(GTFSColumn.class);
                if(a == null) continue;
                retVal.add(a);
            }
        } 
        catch (Exception e)
        {
            LOGGER.log(Constants.SEVERE, "error getting fields for class " + c.getCanonicalName(), e);
        }
        
        return retVal;
    }   
}
