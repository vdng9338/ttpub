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
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.IntUtils;

/**
 * The purpose of RouteNotes is simply to put a footnote on a Time Table.  There's no
 * logic for placing any footnote symbols within the table itself.  Rather, these 
 * Configurations are simply to put some verbage at the bottom of the table.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class RouteNotes extends Configure
{
    @CsvColumn(index=6, name="Footnote (FN)", details="customer friendly information about the route.")
    public String m_footNote;
    
    @CsvColumn(index=7, name="FN Sequence", details="the placement of the footnote, in relation to other footnotes (smaller is higher)")
    public int m_sequence;

    public RouteNotes()
    {        
    }

    public RouteNotes(String agency, String route, String dir, String key, String lang)
    {
        super.setValues(agency, route, dir, key, lang);
    }


    public String getFootNote() 
    {
        return m_footNote;
    }
    public void setFootNote(String fnNote)
    {
        if(fnNote == null) return;
        m_footNote = fnNote.trim();
    }

    public Integer getSequence() 
    {
        return m_sequence;
    }
    public void setSequence(String sequence)
    {
        m_sequence = IntUtils.getIntegerFromString(sequence, 60);
    }

    
    synchronized public static List<Footnote> process(TimesTable tt)
    {
        if(!okToProcess(tt, Configure.class)) return null;
                        
        RouteNotes index = new RouteNotes(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage());
        List rnList = tt.getConfiguration().findAllData(index);
        if(rnList == null || rnList.size() < 1) return null;
        
        List<Footnote> retVal = new ArrayList<Footnote>();         
        for(RouteNotes rn : (List<RouteNotes>)rnList)
        {
            if(rn == null) continue;
            retVal.add(new FootnoteImpl("rn" + rn.getSequence(), null, rn.getFootNote(), rn.getSequence()));
        }
            
        return retVal;
    }    
}    
