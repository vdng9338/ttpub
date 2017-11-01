/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;

/**
 * The purpose of HttpSelectedTableParams is to TODO
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 10, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class HttpSelectedTableParams extends SelectedTableParams implements Constants
{
    public HttpSelectedTableParams(String r, KeyType k, DirType d)
    {
        super(r, k, d);
    }

    static public List<HttpSelectedTableParams> parse(HttpServletRequest req)
    {
        List<HttpSelectedTableParams> selected = new ArrayList<HttpSelectedTableParams>();            

        for(Object o : req.getParameterMap().keySet())
        {
            String param = (String)o;
            String rt = null;
        
            /**
             * NOTE: the web interface is a series of html check boxes.
             *       the parameter naming format for the check boxes is <KEY TYPE><route Number>
             *       where <KEY TYPE> is one of INBOUND, OUTBOUND, WEEK, SAT, SUN.
             *       
             *       Thus, when you want to find whether the user has request to generate 104 Weekday Inbound,
             *       you'd expect two HTTP parameters, with the names 'inbound104' and 'week104'.  The code
             *       below searches for such things, and captures that for latter processing.
             */
            if(param != null && param.startsWith(INBOUND))
            {               
                rt = param.substring(INBOUND.length()).trim();
                
                if(req.getParameter(WEEK+rt) != null) selected.add(new HttpSelectedTableParams(rt, KeyType.Weekday,  DirType.Inbound));
                if(req.getParameter(SAT+rt)  != null) selected.add(new HttpSelectedTableParams(rt, KeyType.Saturday, DirType.Inbound));
                if(req.getParameter(SUN+rt)  != null) selected.add(new HttpSelectedTableParams(rt, KeyType.Sunday,   DirType.Inbound));
            }
            else if(param != null && param.startsWith(OUTBOUND))
            {               
                rt = param.substring(OUTBOUND.length()).trim();
                
                if(req.getParameter(WEEK+rt) != null) selected.add(new HttpSelectedTableParams(rt, KeyType.Weekday,  DirType.Outbound));
                if(req.getParameter(SAT+rt)  != null) selected.add(new HttpSelectedTableParams(rt, KeyType.Saturday, DirType.Outbound));
                if(req.getParameter(SUN+rt)  != null) selected.add(new HttpSelectedTableParams(rt, KeyType.Sunday,   DirType.Outbound));
            }
        }
        
        Collections.sort(selected, new SelectedTableParams.Compare());
        return selected;
    }
}
