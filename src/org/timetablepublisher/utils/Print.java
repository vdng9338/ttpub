/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * The purpose of Print is simply to provide a utility to print an abitrary object to stdout.  Uses reflection to traverse
 * the members of a Java Bean.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 14, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class Print
{
    // uses REFLECTION to print an object's data via it's getter methods
    public static void print(Object o, boolean ignoreSets)
    {
        if(o == null) return;
        
        try
        {
            System.out.println("\n\nPrinting Object: " + o.getClass().getName());

            Method method[] = o.getClass().getMethods();
            for(int i=0; i < method.length; i++)
            {
                // hibernate -- hib. generated classes have Sets that are lazy loaded
                //              and thus might not be there if the hib. session is dead
                if(ignoreSets && method[i].getReturnType() == Set.class) continue;

                String mStr = method[i].getName();
                if(mStr.startsWith("get")         && 
                   !mStr.equals("getClass")       && 
                   !mStr.equals("getStringValue") &&
                   !mStr.equals("getDomNode")     &&
                   method[i].getParameterTypes().length == 0)
                {
                    Object m = method[i].invoke(o, (Object[])null);
                    if(m != null) {
                        System.out.println(mStr.substring(3) + ": \t" + m.toString());
                    }
                }
            }
        }
        catch(Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void print(Object o)
    {
        print(o, false);
    }

 }
