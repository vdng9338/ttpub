/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure.loader;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.timetablepublisher.configure.ActiveServiceKeys;
import org.timetablepublisher.configure.ComboRoutes;
import org.timetablepublisher.configure.Configure;
import org.timetablepublisher.configure.CullTrips;
import org.timetablepublisher.configure.EffectiveDate;
import org.timetablepublisher.configure.InterliningNotes;
import org.timetablepublisher.configure.LoopFillIn;
import org.timetablepublisher.configure.MergeTPNotes;
import org.timetablepublisher.configure.PatternNotes;
import org.timetablepublisher.configure.PhantomTimePoint;
import org.timetablepublisher.configure.RenameTimePoint;
import org.timetablepublisher.configure.RouteNames;
import org.timetablepublisher.configure.RouteNotes;
import org.timetablepublisher.configure.TimePoints;
import org.timetablepublisher.configure.TripNotes;


/**
 * The purpose of ConfigurationLoader is to provide a base interface of methods that
 * load Configuration CSV files into memory.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 * @see     org.timetablepublisher.configure.CsvLoaderImpl
 * @see     org.timetablepublisher.configure.loader.ConfigurationLoaderImpl
 */
public interface ConfigurationLoader
{        
    public static final Class[] CSV = 
    {
        EffectiveDate.class,
        RouteNames.class,
        TimePoints.class,  
        RenameTimePoint.class,
        RouteNotes.class,
        PatternNotes.class,  
        InterliningNotes.class,        
        MergeTPNotes.class,
        TripNotes.class,               
        LoopFillIn.class,  
        CullTrips.class,
        PhantomTimePoint.class,
        ComboRoutes.class,
        ActiveServiceKeys.class
    }; 

    // finder routines
    List            findAllData(Configure index);
    Configure       findData(Configure config);
    Configure       findData(String csvFileName, String csvRowID);
    List<Configure> findAllIndicies(Configure index);

    String          getCsvDir();
    List<String>    getCsvNames();
    List<Configure> getData(String csvFileName);
    List<Configure> getData(Class name);
    Hashtable<Class, List<Configure>> getData();
    
    // editor routines    
    void            addData(List added);
    Integer         newConfig(Configure config);
    Integer         newConfig(String csvFileName, Configure config);
    boolean         deleteConfig(Configure rdata);
    void            removeData(List tpList);
    void            persist() throws IOException;
    void            setDirty(Class c);
    void            unDirty(Class c);
    boolean         isDirty();
    boolean         isDirty(Class c);

    // ignore processing a given configuration 
    boolean         ignoreConfig(Class configClass);
    void            setIgnoreConfig(Class configClass);
    void            setIgnoreConfig(Class configClass, boolean value);    
}