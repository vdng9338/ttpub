/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.schedule.ScheduleDataQuery.PositionType;
import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.CellImpl;
import org.timetablepublisher.table.Footnote;
import org.timetablepublisher.table.FootnoteImpl;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.IntUtils;

/**
 * The purpose of PatternNotes is to footnote trips that meet certain criteria, like
 * visitng a stop within a route (EXISTS) or starting/ending the trip at a certain stop
 * (BEGIN_TRIP / END_TRIP).  The footnote can either be marked on the trip, or on the given
 * cell (stop / stop time) where the trip event takes place.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class PatternNotes extends Configure
{    
    public static final String _AFTER_   = "_AFTER_";
    public static final String _BETWEEN_ = "_BETWEEN_";
    public static final String _AND_     = "_AND_";    

    
    public static enum PatternRule 
    {
        EXISTS, BEGIN_TRIP, END_TRIP, BEGIN_TRIP_MARK_STOP, END_TRIP_MARK_STOP;
    
        public static PatternRule construct(String str)
        {
            PatternRule retVal = PatternRule.EXISTS;
            
            try
            {
                if(str != null && str.length() > 1)
                {
                    for(PatternRule p : PatternRule.values())
                    {
                        if(p.name() == null) continue;
                        
                        // do this, since there might be a "BETWEEN" appended to the rule
                        if(str.startsWith(p.name()))
                        {
                            retVal = p;
                        }
                    }
                }
            }
            catch (RuntimeException e)
            {
            }  
            
            return retVal;
        }

        public PositionType toPositionType()
        {
            switch(this)
            {
                case END_TRIP:
                case END_TRIP_MARK_STOP:
                    return PositionType.LAST;
                
                case BEGIN_TRIP:
                case BEGIN_TRIP_MARK_STOP:
                    return PositionType.FIRST;
            }
            return PositionType.EXISTS;
        }
    }
    
    @CsvColumn(index=6, name="Rule: EXISTS, BEGIN_TRIP, END_TRIP", details="position of the stopID that we're using as a guide to determine the trip's pattern")    
    public String m_rule;
    
    @CsvColumn(index=7, name="Stop ID", details="the stopID is used to determine a pattern.  when this stop has valid times, that's the indication of this pattern")
    public String m_stopID;

    @CsvColumn(index=8, name="FN Symbol", details="footnote symbol -- to be added to the time-table")
    public String m_symbol;
    
    @CsvColumn(index=9, name="Footnote (FN)", details="customer friendly information explaining the pattern.  NOTE: you can use the following macros: STOP.NAME, STOP.ID, STOP.TIMES, need more...")
    public String m_footNote;
    
    @CsvColumn(index=10, name="FN Sequence", details="the placement of the footnote, in relation to other footnotes (smaller is higher)")
    public int m_sequence;


    public  PatternRule m_patternRule;    
    private Integer m_startTime = null;
    private Integer m_endTime   = null;    
    
    public PatternNotes()
    {        
    }

    public PatternNotes(String agency, String route, String dir, String key, String lang)
    {
        super.setValues(agency, route, dir, key, lang);
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
    
    public String getStopID()
    {
        return m_stopID;
    }

    public String getRule()
    {
        if(m_rule == null) 
        {
            m_rule = m_patternRule == null ? m_patternRule.name() : PatternRule.EXISTS.name();
        }
        return m_rule;
    }
    
    public PatternRule getPatternRule()
    {
        if(m_patternRule == null) {
            m_patternRule = PatternRule.construct(m_rule);
        }
            
        return m_patternRule;
    }

    
    // setters
    public void setFootNote(String footNote)
    {
        if(footNote == null) return;
        m_footNote = footNote.trim();
    }

    public void setRule(String rule)
    {
        if(rule == null) return;
        
        try
        {
            m_rule = rule;        
            m_patternRule = PatternRule.construct(rule);
            
            if(rule.contains(_BETWEEN_))
            {
                m_startTime = IntUtils.getSecPastMidAfterSubString(rule, _BETWEEN_);
                m_endTime   = IntUtils.getSecPastMidAfterSubString(rule, _AND_);
            }
            else if (rule.contains(_AFTER_))
            {
                m_startTime = IntUtils.getSecPastMidAfterSubString(rule, _AFTER_);
                m_endTime   = Integer.MAX_VALUE;
            }
        }
        catch(Exception e)
        {
        }
    }

    public void setSequence(String sequence)
    {
        m_sequence = IntUtils.getIntegerFromString(sequence, 30);
    }

    public void setStopID(String stopID)
    {
        if(stopID == null) return;
        m_stopID = stopID.trim();
    }

    public void setSymbol(String symbol)
    {
        if(symbol == null) return;
        m_symbol = symbol.trim();
    }
    
    public Integer getEndTime()
    {
        return m_endTime;
    }

    public Integer getStartTime()
    {
        return m_startTime;
    }

    public boolean isAtOrAfterStartTime(Cell cell)
    {
        if(m_startTime == null) return true;
        
        boolean retVal = false;
        if(cell != null)
        {
            retVal = cell.getTime() >= m_startTime; 
        }
        return retVal;
    }

    public boolean isAtOrBeforeEndTime(Cell cell)
    {
        if(m_endTime == null) return true;
        
        boolean retVal = false;
        if(cell != null)
        {
            retVal = cell.getTime() <= m_endTime; 
        }
        return retVal;
    }
    
    public boolean tripFallsWithinTimeBoundries(Row thisTrip)
    {
        // often won't have any time constraint on the config...so allow processing to continue...
        if(m_endTime == null || m_startTime == null) return true;
        
        // but when we do have times, let's check that the start and end cells have times that fall within our time range
        boolean retVal = false;
        if(thisTrip != null)
        {
            retVal = isAtOrAfterStartTime(thisTrip.getFirstNonNullCell());            
            if(retVal) {
                // only when trip start happens 'after' our bracket time do we test the end time
                retVal = isAtOrBeforeEndTime(thisTrip.getLastNonNullCell());
            }            
        }        
        return retVal;
    }

    
    public boolean cellFallsWithinTimeBoundries(Cell cell)
    {
        // often won't have any time constraint on the config...so allow processing to continue...
        if(m_endTime == null || m_startTime == null) return true;
        
        // but when we do have times, let's check that the start and end cells have times that fall within our time range
        boolean retVal = false;
        if(cell != null)
        {
            retVal = isAtOrAfterStartTime(cell);            
            if(retVal) {
                // only when trip start happens 'after' our bracket time do we test the end time
                retVal = isAtOrBeforeEndTime(cell);
            }            
        }        
        return retVal;
    }
    
    
    /**
     * Pattern Notes look at which timepoints the bus stops at to differentiate between pattern A and B.
     * So if a trip hits stop X (and there's a footnote for all trips that hit stop X), a footnote is placed.
     * 
     * processing steps:
     *   1. get the pattern notes config information from the CSV files
     *   2. loop through that list of notes
     *   3. every where there's a time (eg: where the vechicle hits this stop in the pattern), add the footnote
     * 
     * @param m_tt
     * @return
     */
    synchronized public static List<Footnote> process(TimesTable tt, ScheduleDataQuery query)
    {
        if(query == null || !okToProcessHasTimeTable(tt, Configure.class)) return null;

        PatternNotes index = new PatternNotes(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage());
        List pnList = tt.getConfiguration().findAllData(index);
        if(pnList == null || pnList.size() < 1) return null;

        List<Footnote> retVal = new ArrayList<Footnote>();
        for(PatternNotes pn : (List<PatternNotes>)pnList)
        {
            boolean addFootnote = false;
            List<Cell>        column = null;
            Collection<String> trips = null;
            
            // step 1: build a collection of trip numbers that match the configuration rules
            if(pn.getPatternRule() == PatternRule.EXISTS)
            {
                // NOTE: other rules look for the existance of the trip in the list of times
                //       thus, we need the column of times

                // find all times for this stop
                column = tt.getStopTimes(pn.getStopID());
                if(column == null)
                {
                    // have to query this stop.
                    column = query.getTimesByStopID(tt, pn.getStopID(), false);
                    if(column == null || column.size() < 0)
                        continue;
                }
                
                trips = CellImpl.getTrips(column);
            }
            else
            {
                trips = query.findTripsByStopID(tt, pn.getStopID(), pn.getPatternRule().toPositionType());
            }
            
            // step 2: check to see if we found any trips -- and make sure we add a footnote for those trips
            if(trips == null || trips.size() < 1) 
                continue; 
            addFootnote = true;
            
            // step 3: put a symbol on the row / cell every where the vechicle hits the targeted stop in the pattern 
            for(String trip : trips)
            {                
                Row thisTrip = tt.findTrip(trip);                
                if(thisTrip == null) continue;
    
                // step 3a: the MARK_STOP will put the symbol on the cell in the table where the time is...whereas other rules put the symbol on the first cell
                if(pn.getPatternRule() == PatternRule.END_TRIP_MARK_STOP)
                {                    
                    Cell last = thisTrip.getLastNonNullCell();
                    if(last == null) continue;                    

                    // step 3b: when rule is END_TRIP_MARK_STOP_AFTER_18:30 or EXISTS_BETWEEN_14:15_AND_19:45
                    //          we'll filter the cell when the stop's time doesn't fall within the time
                    if(!pn.cellFallsWithinTimeBoundries(last)) continue;                    
                    last.setFootnoteSymbol(pn.getSymbol());
                }
                else if (pn.getPatternRule() == PatternRule.BEGIN_TRIP_MARK_STOP)
                {
                    Cell first = thisTrip.getFirstNonNullCell();                    
                    if(first == null) continue;

                    // step 3b: when rule is END_TRIP_MARK_STOP_AFTER_18:30 or EXISTS_BETWEEN_14:15_AND_19:45
                    //          we'll filter the cell out when the stop time doesn't fall within the time
                    if(!pn.cellFallsWithinTimeBoundries(first)) continue;                    
                    first.setFootnoteSymbol(pn.getSymbol());
                }
                else
                {
                    // step 3b: when rule is END_TRIP_MARK_STOP_AFTER_18:30 or EXISTS_BETWEEN_14:15_AND_19:45
                    //          we'll filter the trip out when it's start / stop times don't fall within the time
                    if(!pn.tripFallsWithinTimeBoundries(thisTrip)) continue;
                    thisTrip.setFootnoteSymbol(pn.getSymbol());
                }
            }
    
            // step 4: add the footnote text to the TimeTable
            if(addFootnote)
            {
                String note = pn.getFootNote();
                FootnoteImpl fn = new FootnoteImpl(pn.getSymbol() + pn.getSequence(), pn.getSymbol(), note, pn.getSequence());                
                fn.processTokens(pn.getStopID(), pn.getSymbol(), tt, column, query);
                retVal.add(fn);
            }            
        }
            
        return retVal;
    }
}

