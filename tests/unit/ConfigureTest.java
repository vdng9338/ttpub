package tests.unit;

import java.util.Date;
import java.util.List;

import junit.framework.JUnit4TestAdapter;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import org.junit.Before;
import org.junit.Test;

import org.timetablepublisher.utils.Constants;


/**
 * JUnit Test for RouteNames 
 *
 *
 */
public class ConfigureTest implements Constants
{
    protected String date;

    @Before public void setUp()
    {
        try
        {
            date = dateSDF.format(new Date());
        }
        catch(Exception e)
        {
            System.out.println("Error: " + e);
        }
    }
    
    @Test public void testRouteNames() 
    {
        String route = "31";
        
/*
        TrimetTimesTable tt   = new TrimetTimesTable(route, DirType.Inbound, KeyType.Weekday,  date);
        RouteDescription week = CsvTableUtils.getRouteDirectionName(tt);
        RouteDescription sat  = CsvTableUtils.getRouteDirectionName(tt);
        RouteDescription sun  = CsvTableUtils.getRouteDirectionName(tt);
        
        assertNotNull(week);
        assertNotNull(sat);
        assertNotNull(sun);
        
        assertNotSame(week.getDestination(), sat.getDestination());
        assertEquals (sat.getDestination(),  sun.getDestination());
  */      
    }

    @Test public void testComboRoutes() 
    {
        String route = "280";

/*        
        TrimetTimesTable tt = new TrimetTimesTable(route, DirType.Inbound, KeyType.Weekday,  date);
        List<String> combo = CsvTableUtils.getComboRoutes(tt);
        assertNotNull(combo);        
        assertEquals(combo.get(0), "180");
        //assertEquals(tt.getRouteName() "80 / 180 blah"...
 */
    }
    
    public static junit.framework.Test suite() 
    {
         return new JUnit4TestAdapter(ConfigureTest.class);
    }
    
    public static void main(String args[]) 
    {
        org.junit.runner.JUnitCore.main("tests.unit.ConfigureTest");
    }    
}
