package org.timetablepublisher.table;

import java.util.List;
import java.util.Locale;

import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Params;
import org.timetablepublisher.schedule.gtfs.GTFSTimesTable;
import org.timetablepublisher.schedule.mock.MockTimesTable;


/**
 * The purpose of TimeTableFactory is to provide a single point of contact for building TimesTable objects.  The 
 * construction of the TimesTable can have multiple instances (eg: there's a TimesTable that is specific to the 
 * Google Feed Spec, there are a couple of different TriMet TimeTable objects, depending upon the data source, etc...).  
 * 
 * NOTE: the problem with the Factory pattern is that in order to add new TimesTable types, one has to make a code change.
 * I'd thought about adding the Inversion of Control (IOC) pattern to solved this problem, but I decided that for simplicity,
 * I would stick to the Factory.  That said, I have no objection to IOC -- and think that PicoContainer might be a good solution.
 * Then again, I also think think there might be a better solution out there.  Here's another tought I had on this a few months
 * back:
 *          thinking that we could have an CSV file, and instantiate random TimeTable instances
 *          based off contents in the CSV -- a bit of reflection, a bit of CSV, etc...
 *          don't want to be changing code every time a new Table is created
 *        
 * 
 *    
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Nov 21, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class TimeTableFactory
{    
    public static enum TableType
    {
        TRANS("T"), MOCK("M"), GTFS("G");
        public static final TableType getDefault() { return TRANS; }
        
        private final String m_value;
        private TableType(String value) { this.m_value = value; }
        public String value()           { return m_value;       }
        
        public static TableType construct(String s) 
        {      
            if(s == null) return getDefault();
            
            TableType retVal = null;            
            for(TableType k : TableType.values())
            {
                if(s.equals(k.value()))
                {
                    retVal = k;
                }
            }
            
            if(retVal == null)
            {
                try
                {
                    retVal = TableType.valueOf(s);   
                }
                catch(Exception e)
                {
                    retVal = getDefault();
                }            
            }
            return retVal;
        }
    }    

    public static TimesTable create(Params p)
    {
        return create(p, p.getDate());
    }
    public static TimesTable create(Params p, String date)
    {
        return create(p.getTableType(), null, p.getAgency(), p.getRouteID(), p.getDir(), p.getKey(), date, p.getLocale(), p.getConfigureDataDir(), p.isBypassConfig(), p.getScheduleDataDir());
    }    
    public static TimesTable create(TableType type, List<Stop> timePoints, String agency, String route, DirType dir, KeyType key, String date, Locale lang, String configDirectory, boolean bypassConfig, String scheduleDataDirectory)
    {
        switch(type)
        {
        case MOCK:
            return new MockTimesTable(route, dir, key, date);            
        default:
            return new GTFSTimesTable(timePoints, agency, route, dir, key, date, lang, configDirectory, bypassConfig, scheduleDataDirectory);
        }
    }
    public static void cleanup()
    {
    }
}
