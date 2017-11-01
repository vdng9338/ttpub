/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import org.timetablepublisher.configure.RouteNames;
import org.timetablepublisher.table.TimesTable.DirType;


/**
 * The purpose of RouteDescriptionImpl is to collect the strings that make up a table, like the Route and Destination name.
 * The reason for multiple constructors is for Hibernate, where this class is created by some of the HQL queries directly
 * from route name data in the TriMet database.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 14, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */

// TODO: the construction of this object looks very hacky...clean me
public class RouteDescriptionImpl implements RouteDescription
{
    public final String  m_agencyName;
    public final String  m_routeID;
    public String  m_routeName      = null;
    public String  m_routeRawID     = null;
    public String  m_destination    = null;
    public String  m_serviceKeyName = null;
    public boolean m_showStopID     = true;
    public DirType m_direction      = DirType.Inbound;
    public String  m_url            = null;
    
    public RouteDescriptionImpl(String routeName, short routeID)
    {
        this(routeName, Short.toString(routeID));
    }
    public RouteDescriptionImpl(String routeName, String destination, short routeID)
    {        
        this(routeName, routeID);
        m_destination = destination;
    }
    public RouteDescriptionImpl(String routeName, String routeID)
    {
        this(null, routeName, routeID, null, null, true);
    }
        
    public RouteDescriptionImpl(String agencyName, String routeName, String routeID, String destination, String keyName, boolean showStopID)
    {
        m_agencyName     = agencyName;
        m_routeID        = routeID;
        setValues(routeName, destination, keyName, showStopID);
    }
    
    public RouteDescriptionImpl(String agencyName, String routeName, String routeID, String  rawID, boolean showStopID)
    {
        this(agencyName, routeName, routeID, null, null, showStopID);
        m_routeRawID = rawID;
    }

    public RouteDescriptionImpl(RouteNames rn)
    {
        this(rn, rn.getRouteID());
    }

    public RouteDescriptionImpl(RouteNames rn, String routeID)
    {
        m_agencyName = rn.getAgency();
        m_routeID    = routeID;
        setValues(rn);
    }
    
    public RouteDescriptionImpl(TimesTable tt)
    {
        this(tt.getAgencyName(), tt.getRouteName(), tt.getRouteID(), tt.getDestination(), tt.getKeyName(), tt.showStopIDs());
    }

    public void setValues(RouteNames rn)
    {
        setValues(rn.getRouteName(), rn.getDestination(), rn.getKeyName(), rn.showStopID());
    }
    public void setValues(String routeName, String destination, String keyName, Boolean showStopID)
    {
        m_routeName      = (routeName   != null && routeName.length()   > 0) ? routeName   : m_routeName; 
        m_destination    = (destination != null && destination.length() > 0) ? destination : m_destination; 
        m_serviceKeyName = (keyName     != null && keyName.length()     > 0) ? keyName     : m_serviceKeyName;
        m_showStopID     = (showStopID  != null)                             ? showStopID  : m_showStopID;
    }
    
    public String getAgencyName()
    {
        return m_agencyName;
    }    
    public String getRouteID()
    {
        return m_routeID;
    }
    public String getRawRouteID()
    {
        if(m_routeRawID == null)
            return m_routeID;

        return m_routeRawID;
    }
    public void setRawRouteID(String id)
    {
        m_routeRawID = id;
    }
    
    public String getRouteName()
    {
        return m_routeName;
    }    
    public void setRouteName(String routeName)
    {
        if(routeName == null || routeName.length() < 1) return;        
        m_routeName = routeName;
    }

    
    public String getDestination()
    {
        return m_destination;
    }
    public void setDestination(String destination)
    {
        if(destination == null || destination.length() < 1) return;
        m_destination = destination;
    }

    
    public String getServiceKeyName()
    {
        return m_serviceKeyName;
    }
    public void setServiceKeyName(String serviceKeyName)
    {
        if(serviceKeyName == null || serviceKeyName.length() < 1) return;        
        m_serviceKeyName = serviceKeyName;
    }
    
    public boolean showStopIDs()
    {
        return m_showStopID;
    }
    public void setShowStopID(boolean showStopID)
    {
        m_showStopID = showStopID;
    }

    public void append(RouteDescription tmp)
    {        
        if(m_routeName == null || m_routeName.length()     < 1) 
            m_routeName = tmp.getRouteName();
        
        if(m_destination == null || m_destination.length() < 1) 
            m_destination = tmp.getDestination();
        
        if(m_serviceKeyName == null || m_serviceKeyName.length() < 1)
            m_serviceKeyName = tmp.getServiceKeyName();
    }    

    public boolean hasRouteAndDir()
    {
        if(m_routeName   == null || m_routeName.length()   < 1) return false;
        if(m_destination == null || m_destination.length() < 1) return false;
        
        return true;
    }    
    public boolean isComplete()
    {
        if(m_serviceKeyName == null || m_serviceKeyName.length() < 1) return false;
        if(!hasRouteAndDir()) 
            return false;
                
        return true;
    }
    public DirType getDir1()
    {
        return m_direction;
    }    
    public DirType getDir2()
    {
        if(m_direction == null) return null;
        return m_direction.getOpposite();
    }
    public void setDirection(DirType d)
    {
        m_direction = d;
    }        
    public void setDirection(int d)
    {
        m_direction = DirType.construct(d);
    }
 
    public String getUrl() {
        return m_url;
    }
    public void setUrl(String routeUrl) {
        m_url = routeUrl;
    }        
}
