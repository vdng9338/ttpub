/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.configure.loader.ConfigurationLoaderImpl;
import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.configure.loader.CsvColumnImpl;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.table.Footnote;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.IntUtils;
import org.timetablepublisher.utils.Params;

/**
 * The purpose of Configure is to be the base class for all of the Configuration POJOs
 * (eg: CullTrips, LoopFillIn, TripNotes, etc...).  ALL Configuration classes are built
 * upont Configure.  Having such a base allows the loader tool to pull in all 
 * CSV files, with Configure acting as a single interface.
 * 
 * NOTE: why not also provide an interface ?  Not sure...things just evolved this way
 * without an interface, and so there was never a need for such.  Plus, there was
 * a need for a common base class, since core attributes (primary key) like route / dir / service
 * key are common to all Configurations.  And so a common set of methods was needed,
 * which Configure provides.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 21, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class Configure extends CsvColumnImpl implements Comparator<Configure>
{
    public static final Integer DEFAULT_INT = -1;
        
    @CsvColumn(index=1, name="Agency", details="A single string (eg: 'Amtrak Cascades', 'TriMet', '*', etc...")
    public String m_agency;
	       					    
    @CsvColumn(index=2, name="Route", details="One or more RouteIDs (eg: '1', '1, 2', '*').")
    public String m_routeID;  
    	       					    
    @CsvColumn(index=3, name="Direction", details="One or more directions (eg: 'North', 'Inbound', 'North, South', '*'") 
    public String m_dir;    
    	       					    
    @CsvColumn(index=4, name="Service Key", details="One or more keys (eg: 'Sunday', 'Weekday, Saturday', '*').") 
    public String m_key;    
    	       					    
    @CsvColumn(index=5, name="Language", details="Either nothing (for all languages) or the ISO 2-character language (eg: 'en', 'fr', 'es', 'dk', 'jp', etc...)")
    public String m_lang;
    
    protected String m_index;
    protected boolean m_ignore = false;
    
    DirType m_dirType = null;
    KeyType m_keyType = null;
    
    public Configure()
    {        
    }
    
    public Configure(Params p)
    {
        setValues(p);
    }

    public Configure(TimesTable tt)
    {
        setValues(tt);
    }

    public Configure(Configure conf)
    {
        setValues(conf);
    }
    
    public Configure(String agency, String route, String dir, String key)
    {
        setValues(agency, route, dir, key, "", null);
    }

    public Configure(String agency, String route, String dir, String key, String lang)
    {
        setValues(agency, route, dir, key, lang, null);
    }
    
    public static boolean okToProcess(TimesTable tt, Class configClass)
    {
        if(tt == null || tt.getConfiguration() == null || tt.getConfiguration().ignoreConfig(configClass) || tt.bypassConfig()) return false;        
        return true;
    }     

    public static boolean okToProcessHasTimeTable(TimesTable tt, Class configClass)
    {
        if(!okToProcess(tt, configClass)) return false;
        if(tt.getTimeTable() == null || tt.getTimeTable().size() < 1) return false;
        
        return true;
    }     
    
    public static boolean okToProcess(ConfigurationLoader loader, Class configClass)
    {
        if(loader == null || loader.ignoreConfig(configClass)) return false;
        
        return true;
    }     
    
    
    public void setValues(Params p)
    {
        if(p != null) 
        {
            setValues(p.getAgency(), p.getRouteID(), p.getDir().toString(), p.getKey().toString(), p.getLanguage());
        }        
    }

    public void setValues(TimesTable tt)
    {
        if(tt != null) 
        {
            setValues(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage()); 
        }
    }

    public void setValues(Configure conf)
    {
        if(conf != null) 
        {
            setValues(conf.getAgency(), conf.getRouteID(), conf.getDir(), conf.getKey(), conf.getLang()); 
        }
    }
    
    public void setValues(String agency, String route, String dir, String key, String lang)
    {
        setAgency(agency);
        setRouteID(route);
        setDir(dir);
        setKey(key);
        setLang(set(lang, "en"));
        m_index  = m_routeID + m_dir + m_key;
        
        m_dirType = DirType.construct(dir, null);
        m_keyType = KeyType.construct(key, null);
    }

    public void setValues(String agency, String route, String dir, String key, String lang, String[] csv)
    {
        setFieldValues(csv);
        setValues(agency, route, dir, key, lang);
    }

    
    public String getIndex()
    {
        return m_index;
    }

    public String getAgency()
    {
        return m_agency;
    }

    public String getRouteID() 
    {
        return m_routeID;
    }
        
    public String getDir() 
    {
        return m_dir;
    }
    public String getKey() 
    {
        return m_key;
    }

    public String getLang()
    {
        return m_lang;
    }

    // setters
    public void setAgency(String agency)
    {
        if(agency == null) return;
        m_agency = agency.trim();
    }

    public void setRouteID(String route)
    {
        if(route == null) return;
        m_routeID = route.trim();
    }

    public void setDir(String dir)
    {
        if(dir == null) return;
        m_dir = dir.trim();
    }
    public void setKey(String key)
    {
        if(key == null) return;
        m_key = key.trim();
    }
    public void setIndex(String index)
    {
        if(index == null) return;
        m_index = index.trim();
    }

    public void setLang(String lang)
    {
        if(lang == null) return;
        m_lang = lang.trim();
    }

    /**
     * diff routines
     * NOTE: if either value is NULL, then we return false (not diff), since we can't compare a null (default) value
     * NOTE: * is seen as a wildcard, so it' matches all, and thus also returns *false*, meaning that nothing is ever different against a *wildcard*
     * 
     * @param a
     * @param b
     * @return
     */
    public static boolean diff(String a, String b)
    {
        if(a == null       || b == null)        return false;
        if(a.length() < 1  || b.length() < 1)   return false;
        if(a.equals("*")   || b.equals("*"))    return false;  // wild card is a match all, so *not* different
        
        return !a.equals(b);
    }
    public static boolean diff(Integer a, Integer b)
    {
        if(a == null || b == null)  return false;
        if(a.equals(DEFAULT_INT))   return false;
        if(b.equals(DEFAULT_INT))   return false;
        return !a.equals(b);
    }
        
    public boolean equals(String agency, String route, String dir, String key, String lang)
    {               
        if(diff(agency, m_agency))  return false;
        if(diff(route,  m_routeID)) return false;
        if(diff(lang,   m_lang))    return false;        
        if(diff(key,    m_key))     return false;
        if(m_dirType == null || !m_dirType.isSemanticallySame(dir))
        {
            if(diff(dir,    m_dir)) return false;
        }
        
        return true;
    }
    public boolean equals(Configure in)
    {
        return equals(in.m_agency, in.m_routeID, in.m_dir, in.m_key, in.m_lang);
    }

    static public List<String> findNoteStopIDs(Configure in)
    {
        for(Class c : ConfigurationLoaderImpl.CSV)
        {
            System.out.print(c.getSimpleName());
        }
        
        return null;
    }
    
    // various setter utils    
    public String set(String[] csv, int i)
    {
        return set(csv, "", i);
    }
    public String set(String[] csv, String def, int i)
    {
        if(csv == null) return def;
        
        return csv.length > i ? set(csv[i]) : def;
    }        
    public Integer set(String[] csv, int def, int i)
    {
        if(csv == null) return def;
        
        return csv.length > i ? set(csv[i], def) : def;
    }        
    public String set(String str)
    {
        return set(str, "");
    }
    public String set(String str, String def)
    {
        return str != null ? str.trim() : def;
    }
    public Integer set(Integer in)
    {
        return set(in, DEFAULT_INT);
    }
    public Integer set(Integer in, Integer def)
    {
        return in != null ? in : def;
    }
    public Integer set(String in, Integer def)
    {            
        return IntUtils.getIntegerFromString(in, def);
    }       
    static public List<String> split(String str)
    {
        return split(str, ",");
    }
    static public List<String> split(String str, String seperator)
    {
        if(str == null || seperator == null) return null; 

        List<String> retVal = null;
                        
        String[] sss = str.split(seperator);
        if(sss != null)
        {
            retVal = new ArrayList<String>();
            for(String s : sss)
            {
                retVal.add(s.trim());
            }
        }
        
        return retVal;        
    }

    public int compare(Configure inA, Configure inB)
    {
        if(inA == null || inB == null || inA.equals(inB)) return 0;  // equal
        
        if(!inA.getAgency().equals(inB.getAgency()))  
        {
            // sort by agency, as they are unequal
            return inA.getAgency().compareTo(inB.getAgency());
        }
        else if(!inA.getRouteID().equals(inB.getRouteID()))  
        {
            // sort by route, as they are unequal
            return inA.getRouteID().compareTo(inB.getRouteID());
        }
        else if(!inA.getDir().equals(inB.getDir()))
        {
            // sort by direction, as the directions are uneqal
            return inA.getDir().compareTo(inB.getDir());
        }
        else
        {
            // sort by key...making Weekday the first, Saturday the second, and Sunday the third
            if(inA.getKey().equals(KeyType.Weekday.toString()))  return   1;
            if(inB.getKey().equals(KeyType.Weekday.toString()))  return  -1;
            if(inA.getKey().equals(KeyType.Saturday.toString())) return   1;
            
            return -1;
        }
    }    
    
    /**
     * This method is used to execute all of the Configurations on a given TimeTable.
     * NOTE: the execution order of the methods within is important.
     * 
     * @param  m_tt
     * @param  query
     * @return List<Footnote>
     */
    public static List<Footnote> processAllConfigurations(TimesTable tt, ScheduleDataQuery query)
    {
        CullTrips.process(tt);
        LoopFillIn.process(tt, query);
        RenameTimePoint.process(tt, query);
        List<Footnote> footnotes  = makeFootnotes(tt, query);
        PhantomTimePoint.process(tt);
        if(query != null) 
            query.setServiceDates(tt);
        
        return footnotes;
    }

    /**
     * NOTE: the order of these operations is key to things working correctly (eg: Merge will potentially add cells to
     *       the table...and so latter note rules may act on those inserted cells).
     * 
     * @param m_tt
     * @param query
     * @return List<Footnote>
     */
    public static List<Footnote> makeFootnotes(TimesTable tt, ScheduleDataQuery query)
    {
        List<Footnote> retVal = new ArrayList<Footnote>();
        List<Footnote> fn = null;
        fn = InterliningNotes.process(tt, query); if(fn != null) retVal.addAll(fn);
        fn = MergeTPNotes.process(tt, query);     if(fn != null) retVal.addAll(fn);
        fn = PatternNotes.process(tt, query);     if(fn != null) retVal.addAll(fn);
        fn = RouteNotes.process(tt);              if(fn != null) retVal.addAll(fn);
        fn = TripNotes.process(tt);               if(fn != null) retVal.addAll(fn);
        
        Collections.sort(retVal, new Footnote.Compare());
        
        return retVal;
    }

    public DirType getDirType()
    {
        return m_dirType;
    }

    public KeyType getKeyType()
    {
        return m_keyType;
    }

    public boolean isSomewhatComplete()
    {
        if(m_routeID == null)  return false;
        if(m_dirType == null)  return false;
        
        return true;
    }
    public boolean isComplete()
    {
        if(!isSomewhatComplete()) return false;
        if(m_agency  == null) return false;
        if(m_routeID == null) return false;
        if(m_lang    == null) return false;        
        if(m_key     == null) return false;
        if(m_keyType == null) return false;

        return true;
    }
    
    
    public final boolean isIgnore()
    {
        return m_ignore;
    }

    public final void setIgnore(boolean ignore)
    {
        m_ignore = ignore;
    }

    public static final void clearIgnore(List<Configure> ignoreList)
    {
        setIgnore(ignoreList, false);   
    }
    public static final void setIgnore(List<Configure> ignoreList, boolean ignore)
    {
        if(ignoreList != null)
        for(Configure c : ignoreList)
        {
            if(c != null) 
                c.setIgnore(false);
        }
    }

    
    // TODO: WTF does this routine do & why?
    public static Configure getDefaultConfigure(ConfigurationLoader loader, String agency, String route, String dir, String key)
    {
        if(loader == null) return null;
        
        Configure retVal = null;
        Configure index = new Configure(agency, route, dir, key);
        List<Configure> cList = loader.findAllIndicies(index);
        if(cList != null)
        {
            for(Configure c : cList)
            {
                if(c == null) continue;
                if(c.isComplete())
                {
                    retVal = c;
                    break;
                }
                else if(c.isSomewhatComplete())
                {
                    retVal = c;
                    // don't break ... look for a more complete configuration
                }
            }
        }
            
        return retVal;
    }    
}
