/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

public class LogUtils
{
    private static final Logger LOGGER = Logger.getLogger(LogUtils.class.getCanonicalName());        
    
    public static void setLevel(HttpServletRequest req)
    {
        String logger = req.getParameter("LOG_NAME");
        String level  = req.getParameter("LOG_LEVEL");        
        setLevel(logger, level);
    }
    
    public static void setLevel(String logger, String level)
    {
        if(logger == null) return;
        
        try        
        {
            if(logger.startsWith("org.timetable") || logger.startsWith("org.trimet"))
            {
                if(level == null)
                    level = "INFO";

                if(level.equals("OFF"))
                    level = "SEVERE";
                
                Level newLevel = Level.parse(level);
                for(String s : LogManager.getLoggingMXBean().getLoggerNames())
                {
                    if(! s.contains(logger)) continue;
                    
                    String curr = LogManager.getLoggingMXBean().getLoggerLevel(s);
                    LOGGER.log(Level.ALL, "logger " + s + ": from " + curr + " to " + newLevel.getName());
                    LogManager.getLoggingMXBean().setLoggerLevel(s, level);
                }
            }                
        }
        catch (RuntimeException e)
        {
            LOGGER.log(Level.ALL, "couldn't set logger", e);
        }
    }
}
