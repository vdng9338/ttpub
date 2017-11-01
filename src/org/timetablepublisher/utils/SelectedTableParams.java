/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import org.timetablepublisher.table.RouteDescription;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;

/**
 * The purpose of SelectedTableParams is to maintain a list of route/dir/keys
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 10, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class SelectedTableParams implements Constants
{    
    String  m_agency;
    String  m_route;
    KeyType m_key;
    DirType m_dir;
    

    public SelectedTableParams(String a, String r, KeyType k, DirType d)
    {
        m_agency = a;
        m_route  = r;
        m_key    = k;
        m_dir    = d;
        seperateAgencyRoute();
    }

    public SelectedTableParams(String r, KeyType k, DirType d)
    {
        m_route = r;
        m_key   = k;
        m_dir   = d;
        seperateAgencyRoute();
    }

    public void seperateAgencyRoute()
    {        
        if(m_route != null && m_route.contains(Constants.AGENCY_ROUTE_SEP))
        {
            String[] n = m_route.split(Constants.AGENCY_ROUTE_SEP);
            if(n.length == 1)
            {
                m_route = n[0];
            }
            else if(n.length >= 2)
            {
                m_agency = n[0];
                m_route  = n[1];
            }
        }
    }    
    
    
    public String getAgency()
    {
        return m_agency;
    }

    public DirType getDir()
    {
        return m_dir;
    }

    public KeyType getKey()
    {
        return m_key;
    }

    public String getRoute()
    {
        return m_route;
    }
    
    public static List<SelectedTableParams> getCombos(List<RouteDescription> rdList, List<DirType> dirList, List<KeyType> keyList)    
    {
        if(rdList == null || dirList == null || keyList == null) return null;

        List<SelectedTableParams> retVal = new ArrayList<SelectedTableParams>();
        for(RouteDescription r : rdList)
        {   
            // step 2: loop through all of the keys for each route
            for(KeyType k : keyList)
            {            
                // step 3: loop through all of the directions for the route / key 
                for(DirType d : dirList)
                {
                    retVal.add(new SelectedTableParams(r.getAgencyName(), r.getRawRouteID(), k, d));
                }
            }
        }

        return retVal;
    }
    public static List<SelectedTableParams> getCombos(List<RouteDescription> rdList, DirType dir, List<KeyType> keyList)    
    {
        List dl = new ArrayList();
        dl.add(dir);
        return getCombos(rdList, dl, keyList);
    }
    public static List<SelectedTableParams> getCombos(List<RouteDescription> rdList, DirType dir, KeyType[] keys)    
    {
        return getCombos(rdList, dir, Arrays.asList(keys));
    }
    public static List<SelectedTableParams> getCombos(List<RouteDescription> rdList, DirType[] dirs, KeyType[] keys)    
    {
        return getCombos(rdList, Arrays.asList(dirs), Arrays.asList(keys));
    }
    public static List<SelectedTableParams> getCombos(List<RouteDescription> rdList)
    {
        return getCombos(rdList, Arrays.asList(DirType.getInboundOutbound()), Arrays.asList(KeyType.getWkSaSu()));
    }
    
    
    public String description()
    {
        return "agency: " + getAgency() + " route: " + getRoute() + "-" + getDir() + "-" + getKey();
    }
    
    static public class Compare implements Comparator<SelectedTableParams>
    {
        //  compare
        //
        //  public int compare(java.lang.Object o1, java.lang.Object o2)
        //
        //      Return an integer that is negative, zero or positive depending on whether the first argument 
        //      is less than, equal to or greater than the second according to this ordering. This method 
        //      should obey the following contract:
        //          * if compare(a, b) < 0 then compare(b, a) > 0
        //          * if compare(a, b) throws an exception, so does compare(b, a)
        //          * if compare(a, b) < 0 and compare(b, c) < 0 then compare(a, c) < 0
        //          * if compare(a, b) == 0 then compare(a, c) and compare(b, c) must have the same sign
        //
        //      To be consistent with equals, the following additional constraint is in place:
        //          * if a.equals(b) or both a and b are null, then compare(a, b) == 0.
        //
        //      Although it is permissible for a comparator to provide an order inconsistent with equals, 
        //      that should be documented. 
        //
        public int compare(SelectedTableParams c1, SelectedTableParams c2)
        {
            if(c1 == null || c2 == null)                                     return  0;
            if(c1.getRoute() == null || c2.getRoute() == null)               return  0;
            
            if(c1.getRoute().matches(c2.getRoute()))
            {
                if(c1.getDir() == null || c1.getKey() == null)               return 0;
                if(c2.getDir() == null || c2.getKey() == null)               return 0;                
                if(c1.getDir() == c2.getDir() && c1.getKey() == c2.getKey()) return 0;
                
                if      (c1.getDir().value() > c2.getDir().value())          return  1;
                else if (c1.getDir().value() < c2.getDir().value())          return -1;
                
                if(c1.getKey() == KeyType.Weekday)                           return -1;
                if(c2.getKey() == KeyType.Weekday)                           return  1;
                
                if(c1.getKey() == KeyType.Saturday)                          return -1;
                if(c2.getKey() == KeyType.Saturday)                          return  1;

                return  0;
            }
            
            return c1.getRoute().compareToIgnoreCase(c2.getRoute());
        }
    }       
}
