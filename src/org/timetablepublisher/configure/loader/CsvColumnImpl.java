/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure.loader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import org.timetablepublisher.configure.Configure;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.ReflectionUtils;

/**
 * The purpose of CsvColumnImpl is to provide Annotation / Reflectio support methods
 * to those classes that use the CsvColumn anntation.  The routines allow the reading
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
public class CsvColumnImpl implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(CsvColumnImpl.class.getCanonicalName());    

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
        List<String> s = getFieldValues(getFields());
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
        return getFieldValues(getFields());
    }
    public List<String> getFieldValues(List<CsvColumn> cols)
    {
        if(cols == null) return null;
        
        List<String> retVal = new ArrayList<String>();
        for(CsvColumn c : cols)
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
    
    
    public Object getFieldValue(CsvColumn col)
    {
        if(col == null) return null;
        
        Object retVal = "";
        try
        {
            Field[] fList = this.getClass().getFields();
            for(Field f : fList)
            {
                if(f == null) continue;
                CsvColumn a = f.getAnnotation(CsvColumn.class);
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
            LOGGER.log(SEVERE, "error getting field for CsvColumn " + col.name(), e);
        }
        
        return retVal;
    }

    private static String getCsvValue(String[] csv, String name, int i)
    {
        try
        {
            return csv[i];
        }
        catch(Exception e)
        {
            LOGGER.log(DEBUG, "Access csv field " + name + " (csv[" + i + "]) is out of bounds from the file data, which only has " + csv.length + " elements.  This might be nothing...null values are allowed in the config files.");
            return null;
        }
    }
    
    
    
    public void setFieldValues(String[] csv)
    {
        if(csv == null || csv.length < 1) return;
        
        String fieldName = null;
        String csvValue  = null;        
        try
        {
            Field[] fList = this.getClass().getFields();
            for(Field f : fList)
            {                
                if(f == null) continue;
                CsvColumn a = f.getAnnotation(CsvColumn.class);
                if(a == null) continue;
                
                csvValue = getCsvValue(csv, a.name(), a.index() - 1);
                if(csvValue != null && csvValue.trim().length() > 0)
                {
                    fieldName   = f.getName();                                        // get the field name
                    Method meth = ReflectionUtils.getMethod(this, "set", fieldName);  // get the setter method                    
                    meth.invoke(this, csvValue.trim());                               // set the field with http param
                }
            }
        }
        catch (NoSuchMethodException nsm)
        {
            LOGGER.log(SEVERE, "looks like this class lacks a setter for element " + fieldName, nsm);   
        }
        catch (Exception e)
        {
            LOGGER.log(SEVERE, "error setting field with variable " + csvValue, e);
        }
        
    }  
    
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
                CsvColumn a = f.getAnnotation(CsvColumn.class);
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
    

    
    public static List<CsvColumn> getFields(String cName)
    {
        Class c = ReflectionUtils.getClassFromString(Configure.class.getPackage().getName(), cName);
        return getFields(c);
    }
    
    public List<String> getFieldNames()
    {
        return getFieldNames(this.getClass());
    }
    
    public List<CsvColumn> getFields()
    {
        return getFields(this.getClass());
    }
    
    public static List<String> getFieldNames(Class c)
    {
        if(c == null) return null;
        
        List<String> retVal = new ArrayList<String>();        
        for(CsvColumn csv : getFields(c))
        {
            retVal.add(csv.name());
        }
        
        return retVal;
    }
    
    public static List<String> getFieldNames(String cName)
    {
        Class c = ReflectionUtils.getClassFromString(Configure.class.getPackage().getName(), cName);
        return getFieldNames(c);        
    }

    public static List<CsvColumn> getFields(Class c)
    {
        if(c == null) return null;
        
        List<CsvColumn> retVal = new ArrayList<CsvColumn>();
        try
        {
            Field[] fList = c.getFields();
            for(Field f : fList)
            {
                if(f == null) continue;
                CsvColumn a = f.getAnnotation(CsvColumn.class);
                if(a == null) continue;
                retVal.add(a);
            }
        } 
        catch (Exception e)
        {
            LOGGER.log(SEVERE, "error getting fields for class " + c.getCanonicalName(), e);
        }
        
        Collections.sort(retVal, new CsvColumn.Compare());        
        return retVal;
    }   
}
