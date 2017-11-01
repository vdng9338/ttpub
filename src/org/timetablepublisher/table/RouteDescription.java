/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import org.timetablepublisher.configure.RouteNames;
import org.timetablepublisher.table.TimesTable.DirType;

public interface RouteDescription
{
    public String  getUrl();    
    public void    append(RouteDescription tmp);
    public boolean hasRouteAndDir();
    public boolean isComplete();
    public boolean showStopIDs();
    public String  getAgencyName();    
    public String  getRouteID();
    public String  getRawRouteID();
    public String  getRouteName();
    public String  getDestination();
    public String  getServiceKeyName();
    public DirType getDir1();
    public DirType getDir2();
    public void    setDirection(int i);
    public void    setDirection(DirType d);    
    public void    setServiceKeyName(String dow);
    public void    setRouteName(String name);
    public void    setDestination(String dest);
    public void    setValues(RouteNames rn);
    public void    setValues(String routeName, String destination, String keyName, Boolean showStopID);
}