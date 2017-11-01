/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.ArrayList;
import java.util.List;

import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.CellImpl;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.Footnote;
import org.timetablepublisher.table.FootnoteImpl;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.IntUtils;

/**
 * The purpose of MergeTPNotes is to move the times from one column of stops
 * into another column in the time table.  
 * 
 * This Configuration is used in three primary cases:
 *   1. When multiple stops at a transit center service a route: rahter than
 *      having multiple columns in the timetable, the times are combined into 
 *      a single column for space and readability.
 *   2. When a schedule containts multiple columns showing seperate arrival and
 *      departure times.  Often (at least at TriMet) the departure column is 
 *      sparsely populated.  So to clean things up, the arrival column is used
 *      and the times from the departure column are 'FORCED' into the arrival
 *      colum times.
 *   3. Meet Up & Layover stops:  TriMet has a bus 'Meetup' policy, which involves 
 *      having the buses congregate together at a set of downtown stops late at night.
 *      This is done as a public safety measure, so that riders don't have to hang
 *      around at deserted stops to catch their bus.  This is good public information
 *      which we want to put on the TimeTables.  But since the are only a handful of 
 *      late-night trips with meetup times, we footnote these trips (with adjusted time) 
 *      rather than keep a sparsely populated column in the TimeTable.  Thus, a merge
 *      rule will bring the Meetup times into the schedule, and place an appropriate
 *      footnote on the Meetup cells.
 *   
 * NOTE: as alluded to above (see #2), the MERGE and FORCE rules have certain 
 *       behaviors.  A FORCE will move all times from the source column into the
 *       target column, regardless of the contents in the target.  A MERGE will
 *       only move a time from the source into the target when the target cell
 *       is null.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class MergeTPNotes extends Configure
{
    public enum MergeRule 
    {
        MERGE, FORCE;
        
        public static MergeRule construct(String str)
        {
            MergeRule retVal;
            try
            {
                if(str != null && str.length() > 1)
                {
                     retVal = MergeRule.valueOf(str);
                }
                else
                {
                    retVal = MergeRule.MERGE;
                }
            }
            catch (RuntimeException e)
            {
                e.printStackTrace();
                retVal = MergeRule.MERGE;
            }  
            
            return retVal;
        }
    }

    @CsvColumn(index=6, name="Rule: MERGE, FORCE", details="FORCE overwrites existing table times, whereas MERGE only adds a time when the current cell is null.")
    public MergeRule m_rule;
    
    @CsvColumn(index=7, name="Source Timepoint", details="the timepoint ID that has the times we're merging into the table (this timepoint probably isn't going to appear in the table)")
    public String   m_sourceTimepoint;
    
    @CsvColumn(index=8, name="Printed Timepoint", details="the timepoint that's going to appear in the final table")
    public String   m_destinationTimepoint;
    
    @CsvColumn(index=9, name="FN Symbol", details="footnote symbol -- to be added to the time-table")
    public String m_symbol;
    
    @CsvColumn(index=10, name="Footnote (FN)", details="customer friendly information explaining how / where the stops are merged.  NOTE: you can use the following macros: STOP.NAME, STOP.ID, STOP.TIMES, need more...")
    public String m_footNote;
    
    @CsvColumn(index=11, name="FN Sequence", details="the placement of the footnote, in relation to other footnotes (smaller is higher)")
    public int m_sequence;

    boolean  m_hasFootnote;
    
    public MergeTPNotes()
    {        
    }
    
    public MergeTPNotes(String agency, String route, String dir, String key, String lang)
    {
        super.setValues(agency, route, dir, key, lang);
    }

    public void setValues(String agency, String route, String dir, String key, String lang, String[] csv)
    {
        super.setValues(agency, route, dir, key, lang, csv);
        
        if(m_symbol == null && m_footNote == null)
            m_hasFootnote = false;
        else
            m_hasFootnote = true;
    }

    
    public String getDestinationTimepoint()
    {
        return m_destinationTimepoint;
    }
    public Integer getDestTimepointAsInteger()
    {
        return IntUtils.getIntegerFromString(getDestinationTimepoint());
    }
    
    public String getSourceTimepoint()
    {
        return m_sourceTimepoint;
    }        
    public Integer getSrcTimepointAsInteger()
    {
        return IntUtils.getIntegerFromString(getSourceTimepoint());
    }
    
    public List<String> getStopIDs()
    {        
        List<String> stops = new ArrayList<String>();
        stops.add(getSourceTimepoint());
        stops.add(getDestinationTimepoint());
        
        return stops;
    }
    
    public String getFootNote() 
    {
        return m_footNote;
    }

    public Integer getSequence() 
    {
        return m_sequence;
    }

    public String getSymbol()
    {
         return m_symbol;   
    }

    public MergeRule getRule()
    {
        return m_rule;
    }

    public boolean hasFootnote()
    {
        return m_hasFootnote;
    }
    
    // setters
    public void setDestinationTimepoint(String destinationTimepoint)
    {
        if(destinationTimepoint == null) return;
        m_destinationTimepoint = destinationTimepoint.trim();
    }

    public void setFootNote(String fnNote)
    {
        if(fnNote == null) return;
        m_footNote = fnNote.trim();
    }

    public void setRule(String rule)
    {
        m_rule = MergeRule.construct(rule);
    }

    public void setSequence(String sequence)
    {
        m_sequence = IntUtils.getIntegerFromString(sequence, 30);
    }

    public void setSourceTimepoint(String sourceTimepoint)
    {
        if(sourceTimepoint == null) return;
        m_sourceTimepoint = sourceTimepoint.trim();
    }

    public void setSymbol(String symbol)
    {
        if(symbol == null) return;
        m_symbol = symbol.trim();
    }    

    
    synchronized public static List<Footnote> process(TimesTable tt, ScheduleDataQuery query)
    {
        if(query == null || !okToProcessHasTimeTable(tt, Configure.class)) return null;
                
        MergeTPNotes index = new MergeTPNotes(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage()); 
        List mnList = tt.getConfiguration().findAllData(index);
        if(mnList == null || mnList.size() < 1) return null;

        SvcKeyNormalizer skn = new SvcKeyNormalizer(tt);

        List<Footnote> retVal = new ArrayList<Footnote>();         
        for(MergeTPNotes mn : (List<MergeTPNotes>) mnList)
        {
            if(mn != null && mn.getSourceTimepoint() != null && StopImpl.isStopInList(tt.getTimePoints(), mn.getDestinationTimepoint()))
            {                
                String srcStopID  = mn.getSourceTimepoint();
                String destStopID = mn.getDestinationTimepoint();
                String symbol     = mn.getSymbol();
                MergeRule rule    = mn.getRule();                
                
                Integer pos = StopImpl.findColumnIndex(tt.getTimePoints(), destStopID);
                if(pos == null) {
                    pos = StopImpl.findColumnIndex(tt.getTimePoints(), query.toPlaceId(tt.getConfiguration(), tt.getAgencyName(), destStopID));
                    if(pos == null) continue;
                }
                
                List<Cell> times = query.getTimesByStopID(tt, srcStopID, false, symbol, rule);
                if(times == null || times.size() < 1) continue;
                skn.normalizeServiceKey(times);
                
                boolean alreadySet = false;
                for(Row r : tt.getTimeTable())
                {          
                    Cell c = CellImpl.findSameTrip(times, r.getTrip());
                    if(c == null) continue;
                                        
                    boolean changed = false;
                    if(rule == MergeRule.MERGE && r.getCell(pos) == null) // note: merge does not ovrwrt data (eg: pos must be null to write) 
                    {                        
                        r.setCell(pos, c);
                        changed = true;
                    }
                    else if(rule == MergeRule.FORCE)
                    {    
                        // force -- overwrite existing cell data regardless whether the cell exists (as long as this is new data)
                        Cell tmp = r.getCell(pos);
                        
                        // overwrite as long as different times
                        if(tmp == null || tmp.getTime() != c.getTime())
                        {
                            r.setCell(pos, c);
                            changed = true;
                        }
                    }
                    
                    // set the symbol on the cell
                    if(changed && symbol != null && symbol.length() > 0) 
                    {
                        c.setFootnoteSymbol(symbol);
                    }
                    
                    // set the footnote at bottom of table
                    if(changed && !alreadySet)
                    {
                        setFootnote(tt, query, symbol, mn.getFootNote(), mn.getSequence(), retVal, srcStopID, destStopID);
                        alreadySet = true;
                    }
                }
            }            
        }        
        return retVal;
    }

    protected static void setFootnote(TimesTable tt, ScheduleDataQuery query, String sym, String note, Integer seq, List<Footnote> retVal, String srcStopID, String destStopID)
    {
        if(tt == null || query == null) return;
        if(note != null && note.length() > 1) 
        {
            // step 1: in order to get the name & stop id of the timepoint where these stop times are comming
            //         from, we need to make a new list of timepoints -- with both the route stops, as well as
            //         the timepoint we're pulling times from (which is probably not in our table)           
            List<Stop> tmp = new ArrayList<Stop>();
            tmp.addAll(tt.getTimePoints());
            tmp.add(query.makeStop(tt.getConfiguration(), tt.getAgencyName(), srcStopID, srcStopID, 1));

            // step 2: rename these timepoints
            RenameTimePoint.process(tt, tmp, query);
            
            Footnote fn = new FootnoteImpl(sym + seq, sym, note, seq, tmp, srcStopID, destStopID);
            if(!FootnoteImpl.fnWithinList(fn, retVal))
            {
                retVal.add(fn);
            }
        }        
    }
}

