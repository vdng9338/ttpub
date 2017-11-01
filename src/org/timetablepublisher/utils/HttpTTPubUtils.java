/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * The purpose of HttpUtils is a set of utilities for http
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Dec 12, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class HttpTTPubUtils
{
    /**
     * addAttributeToSession will open a session and add some params to it...
     * 
     * @param attribName
     * @param re
     * @see isAttributeInSession
     */
    public static void addAttributeToSession(String attribName, HttpServletRequest req)
    {
        try
        {
            HttpSession ses = req.getSession();
            String selected[] = req.getParameterValues(attribName);
     
            if(ses != null)
            {
                if(selected != null) {
                    ses.setAttribute(attribName, selected);
                } else {
                    ses.removeAttribute(attribName);
                }
            }
        }
        catch(Exception e)
        {            
        }
    }
    
    /**
     * isAttributeInSession will compare a request attribute with something in an http session object.
     * if they are the same, then it returns true (if used properly, you can assume this condition is the 
     * result of a browser 'refresh').
     * 
     * @param attribName
     * @param req
     * @return
     * @see  addAttributeToSession
     */
    public static boolean isAttributeInSession(String attribName, HttpServletRequest req)
    {
        boolean retVal = false;
        
        try
        {
            HttpSession ses = req.getSession();
            if(ses != null)
            {
                String oldVars[]  = (String[])ses.getAttribute(attribName);            
                String selected[] = req.getParameterValues(attribName);
         
                if(selected != null && oldVars != null) {
                    retVal = Arrays.equals(oldVars, selected);
                }                
            }
        }
        catch(Exception e)
        {            
        }
        
        return retVal;
    }
}
