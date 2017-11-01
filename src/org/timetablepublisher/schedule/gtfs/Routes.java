/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.gtfs;

import java.util.List;

import org.timetablepublisher.schedule.gtfs.loader.GTFSColumn;
import org.timetablepublisher.schedule.gtfs.loader.GTFSDataLoader;
import org.timetablepublisher.table.TimesTable.RouteType;

/**
 * The purpose of Routes is to represent the Routes data object in the GData format.  This class, and the member
 * variables within, map directly to the Routes.txt CSV file specified in the Google Feed Spec. 
 * TODO
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 8, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class Routes extends GTFSData
{
    @GTFSColumn(name="route_id", description="")
    public String    m_routeID;
    
    @GTFSColumn(name="agency_id", description="")
    public String    m_agencyID;
    
    @GTFSColumn(name="route_short_name", description="")
    public String    m_shortName;
    
    @GTFSColumn(name="route_long_name", description="")
    public String    m_longName;
    
    @GTFSColumn(name="route_desc", description="")
    public String    m_description;
    
    @GTFSColumn(name="route_url", description="")
    public String    m_url;
    
    @GTFSColumn(name="route_color", description="")
    public String    m_color;
    
    @GTFSColumn(name="route_text_color", description="")
    public String    m_textColor;
    
    @GTFSColumn(name="route_type", description="")
    public RouteType m_type;

    public void setType(String type)
    {
        m_type = RouteType.construct(type);
    }

    public static String getFileName()
    {
        return "routes.txt";
    }
    
    public String getDescription()
    {
        return m_description;
    }

    public String getRouteID()
    {
        return m_routeID;
    }
    
    public String getLongName()
    {
        return m_longName;
    }

    public String getShortName()
    {
        return m_shortName;
    }

    public RouteType getType()
    {
        return m_type;
    }
    
    public String getAgencyID()
    {
        return m_agencyID;
    }

    public String getColor()
    {
        return m_color;
    }

    public String getTextColor()
    {
        return m_textColor;
    }

    public String getUrl()
    {
        return m_url;
    }

    public static Routes findRoute(String route, Agency agency, GTFSDataLoader loader)
    {
        if(route == null || agency == null || loader == null) return null;
        
        Routes retVal = null;
        
        List routeList = loader.getData(agency, Routes.class);
        for(Routes r :(List<Routes>)routeList)
        {
            if(r == null || r.getRouteID() == null) continue;
            
            if(route.equals(r.getRouteID()))
            {
                retVal = r;
                break;
            }
        }
        
        return retVal;
    }    
}
