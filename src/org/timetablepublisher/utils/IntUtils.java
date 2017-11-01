/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

public class IntUtils
{
    private static final Logger LOGGER = Logger.getLogger(IntUtils.class.getCanonicalName());    

    static public String toDate(Date d, String def)
    {
        String retVal;
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
            sdf.setLenient(true);
            retVal = sdf.format(d);
        }
        catch(Exception _)
        {
            retVal = def;
        }
        
        return retVal;        
    }
    
    static public Date toDate(String d, Date def)
    {
        Date retVal = null;
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setLenient(true);
            retVal = sdf.parse(d);
        }
        catch(Exception _)
        {
            retVal = def;
        }
        
        return retVal;        
    }
    static public Date toDate(String d)
    {
        return toDate(d, new Date());    
    }
    
    
    //
    // MISC UTILITY routines
    //
    synchronized static public Date getDate(String dateStr)
    {
        Date date = null;
        try
        {
            date = parseDate(Constants.dateSDF, dateStr);
            if(date == null) date = parseDate(new SimpleDateFormat("yyyyMMdd"), dateStr);
        }
        catch (Exception e)
        {
        }
        
        if(date == null)
            date = new Date();
        
        return date;
    }
    
    public synchronized static Date parseDate(SimpleDateFormat sdf, String string) 
    {
        sdf.setLenient(true);
        try 
        {
            return sdf.parse(string);
        }
        catch (Exception e) 
        {
            //log.debug(string + " was not recognized by " + format.toString());
        }
        return null;
    }
    
    synchronized static public String thisOrNextSunday(String dateStr)
    {
        Date date = getDate(dateStr);
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        if(dow > Calendar.SUNDAY)
        {
            cal.add(Calendar.DATE, 8 - dow);
        }
        
        return  Constants.dateSDF.format(cal.getTime());
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

    
    static public List<String> arrayToList(String[] strings)
    {
        if(strings == null || strings.length < 1) return null;

        List<String>retVal = new ArrayList<String>();        
        for(String s : strings)
        {
            if(s == null) continue;
            String tmp = s.trim();
            
            if(tmp != null && tmp.length() >= 1)
                retVal.add(tmp);
        }
            
        return retVal;
    }
    
    static public String toSQLString(List<String> strList, String def)
    {
        String retVal = def;
        try
        {
            if(strList != null && strList.size() > 0)
            {
                StringBuffer k = new StringBuffer("   ");
                for(String key : strList)
                {
                    k.append("'").append(key).append("', ");
                }
                
                // NOTE on substring: get rid of the last ", " characters
                retVal = k.substring(0, k.length() - 2);
            }
        }
        catch(Exception e)
        {
            LOGGER.log(Constants.DEBUG, "uh oh", e);
        }
        
        return retVal;
    }
    static public String toSQLString(List<String> strList)
    {
        return toSQLString(strList, null);
    }
    
    
    public static short getShortFromString(String input)
    {
        return (short)getIntFromString(input);
    }
    public static int getIntFromString(String input)
    {
        int retVal = 0;
        
        Integer n = getIntegerFromString(input);
        if(n != null)
            retVal = n.intValue();
        else if(input != null)
            retVal = input.hashCode();
        
        return retVal;
    }
    public static Integer getIntegerFromString(String input)
    {
        try
        {
            return new Integer(input);
        }
        catch(Exception e)
        {
            // todo: log this...
            return null;
        }        
    }
    public static Integer getIntegerFromString(String input, Integer def)
    {
        Integer retVal = getIntegerFromString(input);
        if(retVal == null)
            retVal = def;
        
        return retVal;
    }
    public static Integer getIntegerFromSubString(String input, int len)
    {
        String tmp = input.substring(len);
        return getIntegerFromString(tmp.trim());
    }

    /**
     * expect an Integer between prefix and suffix
     * 
     * eg: if is this is our string "Hi there #2112, how are you"
     *     then a call of getIntegerFromSubString("Hi there #2112, how are you", "#", ","); will return 2112
     * 
     * note: if " " is specified, and there is no space from prefix to end of line, then the whole line is evaluated
     * 
     * @param target
     * @param prefix
     * @param suffix
     * @return
     */    
    public static Integer getIntegerFromSubString(String target, String prefix, String suffix)
    {
        Integer retVal = null;

        try
        {
            if(target.contains(prefix))
            {
                // get the line from the end of the prefix to end of line
                int sz = prefix.length();
                int in = target.indexOf(prefix);
                String tmp = target.substring(in + sz);
                
                // get suffix endpoint -- and compensate for whitespace / end of line (same thing)
                int suf = tmp.indexOf(suffix);
                if(suf <= 0 && suffix.equals(" "))
                    suf = tmp.length();
                
                tmp = tmp.substring(0, suf);
                retVal = IntUtils.getIntegerFromString(tmp.trim());                
            }
        }
        catch (Exception e)
        {
            // not too big a deal if this dies...just return null, as if we couldn't find an int there
            // TODO: log me
            retVal = null;
        }        
        
        return retVal;
    }

    public static Double getDoubleFromString(String input)
    {        
        try
        {
            return new Double(input);
        }
        catch(Exception e)
        {
            // todo: log this...
            return null;
        }        
    }
    
    static public Boolean toBoolean(String b, Boolean def)
    {
        if(b == null) return def;

        if(b.toLowerCase().equals("true"))
            return true;

        if(b.toLowerCase().equals("false"))
            return false;
        
        if(b.equals("1"))
            return true;

        if(b.equals("0"))
            return false;
        
        return def;
    }
    static public Boolean toBoolean(String b)
    {
        return toBoolean(b, false);
    }
    
    public static boolean isEmpty(String str)
    {
        if(str == null || str.length() < 1) 
            return true;
        
        return false;
    }
    
    public static boolean isEmpty(List list)
    {
        if(list == null || list.size() < 1)
            return true;
        
        return false;
    }    
    
    public static class StringCompare implements Comparator<String>
    {
        public int compare(String str1, String str2)
        {
            return str1.compareToIgnoreCase(str2);
        }
    }
    
    public static List<String> sort(Collection<String> in)
    {
        List<String> retVal = new ArrayList<String>();        
        retVal.addAll(in);
        Collections.sort(retVal, new StringCompare());
        
        return retVal;
    }
    
    
    /**
     * Convert HH:MM or HH:MM:SS to seconds past midnight (note, spm format is chosen for comptability with other data feeds)
     * 
     * @param  Time in HH:MM / HH:MM:SS format
     * @return integer seconds past midnight
     */
    public static int secPastMid(String time)
    {
        int retVal = 0;

        Integer tmp = secondsPastMidnight(time);
        if(tmp != null) 
            retVal = tmp;
        
        return retVal;
    }
    
    public static Integer secondsPastMidnight(String time)
    {
        Integer retVal = null;
        
        int hour = 0, min = 0, sec = 0;
        try
        {
            String[] hms = time.split(":");
            if(hms.length < 2) return 0;
            
            hour = Integer.parseInt(hms[0]); 
            min  = Integer.parseInt(hms[1]); 
            if(hms.length > 2)
                sec  = Integer.parseInt(hms[2]);
            
            retVal = (hour * 60 * 60) + (min * 60) + sec;
        } 
        catch (Exception e)
        {
            LOGGER.log(Constants.DEBUG, time + " didn't parse", e);
            retVal = null;
        }

        return retVal;
    }
    
    
    public static Integer getSecPastMidAfterSubString(String target, String subString)
    {
        Integer retVal = null;

        try
        {
            if(target.contains(subString))
            {
                // get the line from the end of the prefix to end of line
                int sz     = subString.length();
                int in     = target.indexOf(subString);

                // get the 5 character HH:MM string from our substring
                String tmp = target.substring(in + sz, in + sz + 5);                
                retVal = secondsPastMidnight(tmp);
            }
        }
        catch (Exception e)
        {
            // not too big a deal if this dies...just return null, as if we couldn't find an int there
            LOGGER.log(Constants.DEBUG, "couldn't process", e);
            retVal = null;
        }        
        
        return retVal;
    }
}
