/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import java.util.List;


/**
 * the purpose of this class is to TODO
 *
 * @see     
 * @version 1.0 Oct 18, 2006
 * @author  Frank Purcell (purcellf@trimet.org)
 */
public interface Schedule
{   
    // TODO: this is just a stub for future enhancement    
    List<Agency>           getTransitAgencies();
    List<RouteDescription> getRouteNames(String agencyID, String date);
    
    public class Agency
    {
        String   m_description;
        String   m_id;
        String   m_webURL;
        String   m_iconURL;
    }
}