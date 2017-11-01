/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.schedule.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.CellImpl;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.FootnoteImpl;
import org.timetablepublisher.table.RouteDescription;
import org.timetablepublisher.table.RouteDescriptionImpl;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.RowImpl;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTableImpl;
import org.timetablepublisher.table.TimeTableFactory.TableType;
import org.timetablepublisher.utils.KeyValue;


/**
 * 
 * 
 * @author purcellf
 *
 */
public class MockTimesTable extends TimesTableImpl implements TimesTable
{
    int m_numRows;      
    List<RouteDescription> m_routes = new ArrayList<RouteDescription>();
    
    public MockTimesTable(String route, DirType dir, KeyType key, String date)
    {
        this(route, dir, key, date, 10, 10, 10, 3);        
    }

    public MockTimesTable(String route, DirType dir, KeyType key, String date,
            int numRoutes, int numTimepoints, int numRows, int numFootnotes)
    {
        super("mock agency", route, dir, key, date, Locale.US, null, true);
        m_destination    = "via First";
        m_routeName      = "100-Somewhere";        
        m_numRows        = numRows;

        String[] routeNames = {"100-Somewhere", "200-Anywhere", "300-Nowhere"};
        String[] tpNames = {"First", "Second", "Third", "Forth", "Fifth", "Sixth", "Seventh", "Eighth", "Nineth", "Tenth"};
        

        for(short i = 1, j = 0, k = 0; i <= numRoutes; i++)
        {            
            if(j == routeNames.length) j = 0;
            if(k == tpNames.length)    k = 0;
            m_routes.add(new RouteDescriptionImpl(routeNames[j++], "via " + tpNames[k++] , i));
        }

        // create N number of timepoints (with different names)
        for(int i = 1, j = 0, k = 0; i <= numTimepoints; i++)
        {            
            if(j == tpNames.length) {j = 0; k++;}
            if(k == tpNames.length) k = 0;
            m_timePoints.add(new StopImpl(new Integer(i + 1000).toString(), i * 10, tpNames[k] + " & " + tpNames[j++]));
        }       

        for(int i = 1, j = 0, k = 0; i <= numFootnotes; i++)
        {            
            if(j == tpNames.length) {j = 0; k++;}
            if(k == tpNames.length) k = 0;
            m_footnotes.add(new FootnoteImpl("A" + i, "" + (char)('A' + i), "Board at " + tpNames[j++] + " and " + tpNames[k] + 
                                                                      " (stop id " + (i+1000) + ")", i));
        }        
    }
    
    public TableType getTableType()
    {
        return TableType.MOCK;
    }
    
    public List<RouteDescription> getRouteNames()
    {
        return m_routes;
    }
    
    public Stop findTimePoint(String id)
    {
        return StopImpl.findTimePointStatic(m_timePoints, id);
    }

    //
    // TIME POINTS
    //   
    public List<Stop> appendTimePoints(List<String> timepoints, Integer route, Integer dir, String key, String date, int startIndex)
    {
        List<Stop> retVal = new ArrayList<Stop>();

        // add original timepoints first        
        retVal.addAll(m_timePoints);
        
        int i = startIndex;
        for(String t : timepoints)
        {
            retVal.add(new StopImpl(t, i * 10, "stop " + t));
        }

        return retVal;
    }

    public List<Stop> getRouteStops()
    {        
        List<Stop> retVal = new ArrayList<Stop>();
        
        retVal.addAll(m_timePoints);
        retVal.addAll(m_timePoints);

        return retVal;
    }

    //
    // SCHEDULE
    //
    private Cell getCell(String stopID, int index, Integer rownum, KeyType key)
    {
        Integer time = ((index+3) * 777) + (rownum * 2211) + (60 * 60 * 4); 
        
        /// Integer stop, Integer time, Short train, Short trip, int dir, char tripType, String key)
        Cell c = new CellImpl(stopID, time, rownum.toString(), rownum.toString(), DirType.Inbound, null, key.value(), key);
        if(rownum % 19 == 0 && index == 1)
        {
            c.setFootnoteSymbol("" + (char)(time.byteValue() + 'A'));
        }        
        
        return c;
    }
    
    List<Cell> getCells(List<Stop> cols, Integer rownum)
    {
        List<Cell> cells = new ArrayList<Cell>();
        
        for(int i = 0; i < cols.size(); i++)
        {            
            cells.add(getCell(cols.get(i).getStopId(), i, rownum, KeyType.Weekday));
        }
        
        return cells;
    }

    public List<Row> getTimeTable()
    {
        // set of stop time query
        List<Row> schedule = new ArrayList<Row>();

        if(m_numRows == 1) 
        {            
            // just a blank row if numRows = 1
            schedule.add(new RowImpl(m_routeID, m_dir, m_date, m_timePoints.size()));
        }
        else
        {
            for(int i = 0; i < m_numRows; i++)
            {
                Row r = new RowImpl(m_routeID, m_dir, m_date, m_timePoints.size());            
                r.setRow(getCells(m_timePoints, i+1));
                schedule.add(r);
            }       
        }
        return schedule;
    }

    // SASI 
    public List<Row> getStopTimeTable(String stopId)
    {
        List<Row> schedule = new ArrayList<Row>();
        
        for(int i = 0; i < schedule.size(); i++)
        {
            // want to create a row of the table.  
            // note: the Weekday / Saturday / Sunday arrays will be different sized. don't care about
            //       the exceptions thrown, AND there WILL BE exceptions, hence the try/catch hack
            Row rt = new RowImpl(m_routeID, m_dir, m_date, 3);
            try{ rt.setCell(0, getCell("33", 1, i, KeyType.Weekday));  } catch(Exception e){} 
            try{ rt.setCell(1, getCell("33", 2, i, KeyType.Saturday)); } catch(Exception e){} 
            try{ rt.setCell(2, getCell("33", 3, i, KeyType.Sunday));   } catch(Exception e){} 

            schedule.add(rt);
        }
      
        return schedule;
    }

    
    public String getStopDescription(Integer stopId)
    {
        return "Your Timepoint Name Here";
    }

	List<KeyValue> m_supportedDirs = null;
	public List<KeyValue> getSupportedDirections() 
	{
		if(m_supportedDirs == null)
		{
			m_supportedDirs = new ArrayList<KeyValue>();

			String m = DirType.Morning_Loop.toString();
			String e = DirType.Evening_Loop.toString();
			
			m_supportedDirs.add(new KeyValue(m, "0"));
			m_supportedDirs.add(new KeyValue(e,  "1"));
		}
		
		return m_supportedDirs;
	}

    public List<Cell> getTimesByRoute(List<Stop> stops, String agency, String route, DirType dir, KeyType key, List<String> actSvcKey, String date)
    {
        return null;
    }
}
