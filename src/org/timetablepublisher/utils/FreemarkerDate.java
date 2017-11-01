/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

public class FreemarkerDate implements Constants
{
    public FreemarkerDate(HttpServletRequest req, String date)
    {
        setDates(req, date);
    }
    synchronized static public void setDates(HttpServletRequest req, String date)
    {        
        // set the schedule effective date
        Date effective;        
        try
        {
            effective = dateSDF.parse(date);
        }
        catch(Exception e) 
        {
            effective = new Date();
        }
        
        // set the dates
        req.setAttribute(TODAY,          new Date());
        req.setAttribute(EFFECTIVE_DATE, effective);
    }

    synchronized static public Date getDate(String o)
    {
        try
        {
            return dateSDF.parse(o);
        }
        catch(Exception e) 
        {
            return new Date();
        }                
    }
    static public Date getDate(long milli)
    {
        try
        {
            return new Date(milli);
        }
        catch(Exception e)
        {
            return new Date();
        }
    }    
}

