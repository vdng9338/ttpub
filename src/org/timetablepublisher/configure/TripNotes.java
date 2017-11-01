/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.ArrayList;
import java.util.List;

import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.table.Footnote;
import org.timetablepublisher.table.FootnoteImpl;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.IntUtils;


/**
 * The purpose of TripNotes is to turn scheduling specific instructions into public 
 * information.  For example, our scheduling system will identify our Limited and
 * Express trips.  They're marked with either an 'L' or a 'E' "Trip Type".  Thus,
 * this Configuration allows for marking these trips with symbols, and adding a 
 * footnote to descirbe the movement of the bus on such special trips.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class TripNotes extends Configure
{
    @CsvColumn(index=6, name="Trip Type", details="Trip Type is defined in the schedule data -- eg: L and E (Limited & Express) trips.  NOTE: there's no definition in the current Google Spec for this.")
    public  String   m_rawTripType;
    private List<String> m_tripTypes = null;
    
    @CsvColumn(index=7, name="Service Key", details="Service Key is another way to mark trips -- this key (eg: f for Fridays) will mark special trips on certain days.")    
    public  String   m_rawServiceKey;
    private List<String> m_serviceKeys = null;
    
    @CsvColumn(index=8, name="FN Symbol", details="footnote symbol -- to be added to the time-table")
    public String m_symbol;
    
    @CsvColumn(index=9, name="Footnote (FN)", details="customer friendly information about the footnoted trips.  NOTE: you can use the following macros: STOP.NAME, STOP.ID, STOP.TIMES, need more...")
    public String m_footNote;
    
    @CsvColumn(index=10, name="FN Sequence", details="the placement of the footnote, in relation to other footnotes (smaller is higher)")
    public int m_sequence;

    
    
    public TripNotes()
    {        
    }
    
    public TripNotes(String agency, String route, String dir, String key, String lang)
    {
        super.setValues(agency, route, dir, key, lang);
    }
    
    public String getFootNote() {
        return m_footNote;
    }

    public String getSymbol() {
        return m_symbol;
    }

    public Integer getSequence() {
        return m_sequence;
    }

    public String getRawTripType() {
        return m_rawTripType;
    }

    public List<String> getTripTypes()
    {
        if(m_tripTypes == null && m_rawTripType != null)
        {
            m_tripTypes = IntUtils.arrayToList(m_rawTripType.split(","));
        }
        
        return m_tripTypes;
    }        

    public void setRawTripType(String tripType)
    {
        if(tripType == null) return;
        m_rawTripType = tripType.trim();
        m_tripTypes   = null;
    }
    
    public List<String> getServiceKeys()
    {
        if(m_serviceKeys == null && m_rawServiceKey != null)
        {
            m_serviceKeys = IntUtils.arrayToList(m_rawServiceKey.split(","));
        }
        
        return m_serviceKeys;
    }        


    public String getRawServiceKey()
    {
        return m_rawServiceKey;
    }
    public void setRawServiceKey(String keyType)
    {
        if(keyType == null) return;
        m_rawServiceKey = keyType.trim();
        m_serviceKeys   = null;
    }

    
    // setters
    public void setFootNote(String footNote)
    {
        if(footNote == null) return;
        m_footNote = footNote.trim();
    }

    public void setSequence(String sequence)
    {
        m_sequence = IntUtils.getIntegerFromString(sequence, 30);
    }

    public void setSymbol(String symbol)
    {
        if(symbol == null) return;
        m_symbol = symbol.trim();
    }
    
    synchronized public static List<Footnote> process(TimesTable tt)
    {
        if(!okToProcessHasTimeTable(tt, Configure.class)) return null;
        
        TripNotes index = new TripNotes(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage());
        List tnList = tt.getConfiguration().findAllData(index);
        if(tnList == null || tnList.size() < 1) return null;

        List<Footnote> retVal = new ArrayList<Footnote>();
        for(TripNotes tn : (List<TripNotes>)tnList)
        {
            if(tn == null) continue;
            if(tn.getSymbol() == null || tn.getFootNote() == null || tn.getFootNote().length() < 1) continue;
            
            for(Row r : tt.getTimeTable())
            {
                if(r == null) continue;
                                
                if(matchesTripType(r.getTripType(), tn))
                {
                    setNote(r, tn, retVal);
                }

                if(matchesServiceKey(r.getRawSvcKey(), tn))
                {
                    setNote(r, tn, retVal);
                }
            }

            for(Row r : tt.getTimeTable())
            {
                if(r == null) continue;
                if(matchesServiceKey(r.getRawSvcKey(), tn))
                {
                    r.getFootnoteSymbol();
                }
            }
        }
            
        
        return retVal;
    }
    
    public static boolean matchesServiceKey(String svcKey, TripNotes tn)
    {
        if(svcKey == null || svcKey.length() < 1 || tn == null || tn.getServiceKeys() == null) return false;
        
        for(String key : tn.getServiceKeys())
        {
            if(svcKey.equals(key))
                return true;
        }
        
        return false;
    }

    public static boolean matchesTripType(String tripType, TripNotes tn)
    {
        if(tripType == null || tripType.length() < 1 || tn == null || tn.getTripTypes() == null) return false;
        
        for(String key : tn.getTripTypes())
        {
            if(tripType.equals(key))
                return true;
        }

        return false;
    }

    public static void setNote(Row r, TripNotes tn, List retVal)
    {
        r.setFootnoteSymbol(tn.getSymbol(), true);
        Footnote fn = new FootnoteImpl(tn.getSymbol() + tn.getSequence(), tn.getSymbol(), tn.getFootNote(), tn.getSequence());
        if(!FootnoteImpl.fnWithinList(fn, retVal))
        {
            retVal.add(fn);
        }        
    }
}
