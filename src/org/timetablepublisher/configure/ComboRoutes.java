/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.Footnote;
import org.timetablepublisher.table.FootnoteImpl;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimeTableFactory;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTable.DirType;


/**
 * The purpose of ComboRoutes is a Configuration, which allows the combination of
 * multiple routes into a single timetable.  NOTE: This is only useful when these multiple
 * routes share a common set of stops. 
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class ComboRoutes extends Configure
{
    List<String> m_routeList;

    @CsvColumn(name="Routes", details="List the routes to combine (eg: 1, 2, 3)", index=6)
    public String m_routesStr = "";

    public ComboRoutes()
    {           
    }
     
    public ComboRoutes(String agency, String route, String dir, String key)
    {
        super(agency, route, dir, key);
    }
    
    public void setRoutesStr(String val)
    {
        if(val == null) return;
        m_routesStr = val.trim();
        m_routeList = split(m_routesStr);        
    }
    
    public String getRoutesStr()
    {
        return m_routesStr;
    }
        
    public List<String> getRouteList()
    {
        return m_routeList;
    }

    
    /**
     * Will process a ComboRoute configuration, given the input 'Master' table.  Basically, it will iterate   
     * through the N tables that make up the combo, and append the resulting data into the Master.
     * 
     * @param m_tt
     * @param type
     */
    synchronized public static void process(TimesTable tt, ScheduleDataQuery query)
    {
        if(tt == null || query == null || tt.getConfiguration() == null) return;
        
        List<String> combo = ComboRoutes.getRouteList(tt);
        boolean hasDirectionInfo = false;
        
        if(combo != null && !tt.bypassConfig())
        {
            // get the master table's three core components  
            List<Stop>         rtStops = tt.getRouteStops();
            List<Row>            times   = tt.getTimeTable();
            Collection<Footnote> notes   = tt.getFootnotes();
            String schedDataDir = query.getScheduleDataDir();
            String configDir    = tt.getConfiguration().getCsvDir();
            
            for(String route : combo)
            {
                if(route == null) continue;

                DirType dir = tt.getDir();
                
                // step 0: find out whether this has the route:Direction tag or not...                
                String[] s = route.split(":");
                if(s.length > 1 && s[1].length() > 0)
                {
                    route = s[0];
                    dir   = DirType.construct(s[1], dir);
                    hasDirectionInfo = true;
                }
                                
                // step 1: get a specific TimeTable -- one of N potential tables in this combo                
                TimesTable thisTT = createTT(combo, tt, route, dir, configDir, schedDataDir);
                if(thisTT == null || thisTT.getTimeTable() == null) continue;
                
                // step 2: append all of the times from this table into our Master table
                if(times != null)                 
                {
                    times.addAll(thisTT.getTimeTable());
                }
                else
                {
                    tt.setTimeTable(thisTT.getTimeTable());
                    times = thisTT.getTimeTable();
                }

                // step 3: append all of the timepoints from this TT's stops into our routeStops (eg: all stops) column listing
                if(rtStops != null && thisTT.getRouteStops() != null)
                {
                    rtStops.addAll(thisTT.getRouteStops());
                }
                
                // step 4: append all of the footnotes from this table to our Master table (but only if they are unique in the Master)
                if(thisTT.getFootnotes() != null)
                {
                    for(Footnote fn : thisTT.getFootnotes())
                    {
                        String note = fn.getNote();
                        String sym  = fn.getSymbol();
                        
                        // don't need to add the same footnote (not UNIQUE) twice
                        if(FootnoteImpl.exists(tt.getFootnotes(), note, sym)) continue; 
                        
                        // add the foot note to TT's footnote list
                        if(notes != null)
                        {
                            notes.add(fn);
                        }
                    }
                }
                
                // step 5: set service dates
                tt.setServiceDates(thisTT.getServiceStartDate(), thisTT.getServiceEndDate());
            }
            
            // step 5: add in any Combo Route 'Specific' footnotes to the Master table
            List<Footnote> fn = RouteNotes.process(tt); 
            if(fn != null && notes != null)
            {
                notes.addAll(fn);
            }
        }
        
        // step 6: sort this sucker -- both rows and footnotes
        Collections.sort(tt.getTimeTable(), new Row.Compare(tt.getTimePoints().size()));
        Collections.sort((List<Footnote>)tt.getFootnotes(), new Footnote.Compare());
        
        // step 7: when there's direction info, assume thru route, thus merge trips based on block
        if(hasDirectionInfo)
        {
            CullTrips.verticull(tt);
        }
        
        // step 8: finally, remove any duplicates that may have resulted from the multiple table query
        CullTrips.duplicull(tt);
    }
    

    /**
     * the special routine TT creation will pause some configurations prior
     * to creating the tables.  this is because certain MERGE configs represent 
     * duplicate efforts when trying to combine routes 
     * 
      * (eg: don't want to first merge a route, then merge it afterwards...messy). 
     */
    private static TimesTable createTT(List<String> comboRouteList, TimesTable tt, String route, DirType dir, String configDir, String schedDataDir)
    {
        InterliningNotes index = new InterliningNotes(tt.getAgencyName(), route, dir.toString(), tt.getKey().toString(), tt.getLanguage());
        List<Configure> paused = InterliningNotes.pause(index, tt.getConfiguration(), comboRouteList);
        TimesTable retVal = TimeTableFactory.create(tt.getTableType(), tt.getTimePoints(), tt.getAgencyName(), route, dir, tt.getKey(), tt.getDate(), tt.getLocale(), configDir, false, schedDataDir);
        Configure.clearIgnore(paused);
        return retVal;
    }

    public static List<String> getRouteList(TimesTable tt)
    {
        if(tt == null || tt.getConfiguration() == null) return null;
        
        ComboRoutes c = (ComboRoutes)tt.getConfiguration().findData(new ComboRoutes(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString()));
        List<String> routeList = c.getRouteList();
        if(routeList != null)
        {
            // help prevent an infinate loop
            if(routeList.contains(tt.getRouteID()))
            {
                routeList.remove(tt.getRouteID());
            }
        }        
        return routeList;
    }

    public static boolean isComboRoute(TimesTable tt)
    {
        if(tt == null || tt.getConfiguration() == null) return false;
        ComboRoutes c = (ComboRoutes)tt.getConfiguration().findData(new ComboRoutes(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString()));
        return c != null ? true : false;
    }       
}
