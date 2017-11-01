/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.timetablepublisher.configure.RenameTimePoint;
import org.timetablepublisher.schedule.ScheduleDataQuery;
import org.timetablepublisher.utils.IntUtils;


/**
 * The purpose of FootnoteImpl is to provide the default implementation of the Footnote Interface.  It is in
 * this class where we store the footnote data (eg: note, symbol -- which has a link to the Cell's Footnote Symbol),
 * etc.   There are also routines within this call to process and shape the footnote data.  Finally, it should be noted
 * that FootnoteImpl implements a lot of functionality and logic in terms of the little Token Replacement Langage 
 * implemented for footnotes.  See the Footnote interface, and the public final strings for the variety of tokens.   
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 19, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class FootnoteImpl implements Footnote
{            
    private String  m_symbol        = null;
    private String  m_note          = null;
    private String  m_formattedNote = null;
    private Integer m_sequence      = null;
    private final String m_id;
    
    public FootnoteImpl(String id, String symbol, String note, Integer sequence)
    {
        m_id            = id;
        m_symbol        = symbol;
        m_note          = note;
        m_sequence      = sequence;
        m_formattedNote = m_note;
    }
    
    
    // TODO: move from Integer to String for stop ids
    
    // this constructor will
    public FootnoteImpl(String id, String symbol, String note, Integer sequence, List<Stop> timePoints, String srcStopID, String destStopID)
    {
        this(id, symbol, note, sequence);
        processTokens(timePoints, srcStopID, destStopID);
    }
    public void processTokens(List<Stop> timePoints, String srcStopID, String destStopID)
    {        
        // token replacment in footnote data 
        if(srcStopID != null)
        {
            m_formattedNote = tokenReplace(m_formattedNote, SOURCE_ID,   srcStopID);
            m_formattedNote = tokenReplace(m_formattedNote, SOURCE_NAME, StopImpl.getDescriptionStatic(timePoints, srcStopID));
        }        
        if(destStopID != null)
        {
            m_formattedNote = tokenReplace(m_formattedNote, PRINTED_ID,   destStopID);
            m_formattedNote = tokenReplace(m_formattedNote, PRINTED_NAME, StopImpl.getDescriptionStatic(timePoints, destStopID));
        }
    }
    
    
    public void processTokens(String place, TimesTable tt, ScheduleDataQuery query)
    {
        // step 1: get a copy of our timepoints into new List
        List<Stop> timePoints = new ArrayList<Stop>();
        timePoints.addAll(tt.getTimePoints());
        
        // step 2: then rename these timepoints (something we don't do to the actual table at this point
        //         so we do it outside of the table)
        RenameTimePoint.process(tt, timePoints, null);
        String stopID = RenameTimePoint.findRenamedStopID(tt, place);

        int end = timePoints.size() - 1;

        // step 3: do the easy string token replacments
        m_formattedNote = tokenReplace(m_formattedNote, START_ID,   timePoints.get(0).getStopId());    
        m_formattedNote = tokenReplace(m_formattedNote, START_NAME, timePoints.get(0).getDescription());
        m_formattedNote = tokenReplace(m_formattedNote, END_ID,     timePoints.get(end).getStopId());    
        m_formattedNote = tokenReplace(m_formattedNote, END_NAME,   timePoints.get(end).getDescription());
        m_formattedNote = tokenReplace(m_formattedNote, STOP_ID,    stopID);
        if(m_formattedNote.contains(STOP_NAME))
        {
            String name = RenameTimePoint.findStopReName(stopID, tt);
            
            // step 4: OK, we didn't find a rename config for this stop, let's look  within the current set of TPs
            if(name != null)
            {
                name = StopImpl.getDescriptionStatic(timePoints, stopID);   
            }

            // step 5: STILL didn't find a name...let's query the database directly
            if(name == null || name.length() < 1)
            {
                name = query.getLocationDescription(tt.getAgencyName(), stopID);
            }
            
            m_formattedNote = tokenReplace(m_formattedNote, STOP_NAME, name);
        }        
    }
    
    public void processTokens(String stop, String symbol, TimesTable tt, List<Cell> stopTimes, ScheduleDataQuery query)
    {
        processTokens(stop, tt, query);

        // to do times, we have to compare stop times and time-point times
        String regexp = "XXXXX";
        if(stopTimes != null && m_note.contains(STOP_TIMES))
        {
            Integer adder = null; 
            if(m_note.contains(PLUS_)) 
            {
                adder = IntUtils.getIntegerFromSubString(m_note, PLUS_, " ");
                // convert from minutes to seconds
                if(adder != null)
                {                    
                    adder *= 60;
                }
                regexp = PLUS_ + "[0-9]+";
            }
            else if(m_note.contains(MINUS_)) 
            {
                adder = IntUtils.getIntegerFromSubString(m_note, MINUS_, " ");
                // convert from minutes to seconds
                if(adder != null)
                {                    
                    adder *= -60;
                }
                regexp = MINUS_ + "[0-9]+";
            }            

            
            String timeStr = "";
            String plusStr = "";
            for(Cell c : stopTimes)
            {
                if(c == null) continue;
                if(timeStr.length() > 1)
                {
                    timeStr += ", ";
                }
                timeStr = timeStr + c.getTimeAsStr() + " " + CellImpl.getAmPm(c.getTime());

                if(adder != null)
                {
                    int time = c.getTime() + adder;
                    if(plusStr.length() > 1)
                    {
                        plusStr += ", ";
                    }
                    plusStr += ((CellImpl)c).secondsToString(time) + " " + CellImpl.getAmPm(time);
                }                
            }
            
            if(timeStr != null && timeStr.length() > 1) {
                m_formattedNote = m_formattedNote.replace(STOP_TIMES, timeStr);
            }
            if(plusStr != null && plusStr.length() > 1) {            
                m_formattedNote = m_formattedNote.replaceFirst(regexp, plusStr);
            }            
        }
        
        if(stopTimes != null && m_note.contains(START_TIMES))
        {
            String timeStr = getTimePointTimeForMatchingTrips(symbol, tt.getTimeTable(), stopTimes, false);
            if(timeStr != null) {
                m_formattedNote = m_formattedNote.replace(START_TIMES, timeStr);
            }
        }
        
        if(stopTimes != null && m_note.contains(END_TIMES))
        {
            String timeStr = getTimePointTimeForMatchingTrips(null, tt.getTimeTable(), stopTimes, true); 
            if(timeStr != null) {
                m_formattedNote = m_formattedNote.replace(START_TIMES, timeStr);
            }
        }
    }

    //   
    // NOTE: THIS WORKS...but FOR TRIPS WHERE THE TARGET STOP is not part of the schedule (eg: 53 / Mercer), 
    // we won't have times to plug in here, since those trips a culled from the table prior to getting here
    //
    public static String getTimePointTimeForMatchingTrips(String symbol, List<Row> timePointTimes, List<Cell> trips, boolean fromRight)
    {
        String retVal = null;
        for(Cell c : trips)
        {                
            if(c == null) continue;
            String targetTrip = c.getTrip();
            if(targetTrip == null) continue;
            
            for(Row r : timePointTimes)
            {
                if(r == null) continue;
                    
                Cell tripCell;
                if(fromRight) 
                {
                    tripCell = ((RowImpl)r).getLastNonNullCell();
                }
                else 
                {
                    tripCell = ((RowImpl)r).getFirstNonNullCell();
                }
                    
                if(tripCell != null && tripCell.getTrip().equals(targetTrip))
                {
                    if(retVal == null) 
                    {
                        retVal = tripCell.getTimeAsStr() + " " + CellImpl.getAmPm(c.getTime());
                    }
                    else
                    {
                        retVal += ", " + tripCell.getTimeAsStr() + " " + CellImpl.getAmPm(c.getTime());
                    }
                    
                    if(symbol != null) 
                    {
                        tripCell.setFootnoteSymbol(symbol);
                    }
                }
            }
        }

        return retVal;
    }
    
    public static String tokenReplace(String target, String token, String replacement)
    {
        String retVal = target;
        if(target != null && token != null && replacement != null)
        {
            if(target.contains(token))
            {
                retVal = target.replace(token, replacement);
            }
        }
        return retVal;
    }
        
    

    /* (non-Javadoc)
     * @see org.timetablepublisher.Footnote#getNote()
     */
    public String getNote()
    {
        return m_note;
    }

    /* (non-Javadoc)
     * @see org.timetablepublisher.Footnote#setNote(java.lang.String)
     */
    public void setNote(String note)
    {
        m_note = note;
    }

    /* (non-Javadoc)
     * @see org.timetablepublisher.Footnote#getSymbol()
     */
    public String getSymbol()
    {
        return m_symbol;
    }

    /* (non-Javadoc)
     * @see org.timetablepublisher.Footnote#setSymbol(java.lang.String)
     */
    public void setSymbol(String symbol)
    {
        m_symbol = symbol;
    }

    /* (non-Javadoc)
     * @see org.timetablepublisher.Footnote#getFormattedNote()
     */
    public String getFormattedNote()
    {
        return m_formattedNote;
    }

    /* (non-Javadoc)
     * @see org.timetablepublisher.Footnote#setFormattedNote(java.lang.String)
     */
    public void setFormattedNote(String formattedNote)
    {
        m_formattedNote = formattedNote;
    }

    public String getId()
    {
        return m_id;
    }

    public Integer getSequence()
    {
        return m_sequence;
    }

    public void setSequence(Integer sequence)
    {
        m_sequence = sequence;        
    }


    public static boolean fnWithinList(Footnote target, List<Footnote> notes)
    {
        for(Footnote fn : notes)
        {
            if(target.getNote().equals(fn.getNote())
               && target.getSymbol().equals(fn.getSymbol()))
            {
                return true;
            }
        }
        
        return false;
    }


    public static boolean exists(Collection<Footnote> footnotes, String note, String sym)
    {
        Collection<Footnote> notes = find(footnotes, note, sym);
        return notes != null && notes.size() > 0;
    }
    public static Collection<Footnote> find(Collection<Footnote> footnotes, String note, String sym)
    {
        Collection<Footnote> retVal = new ArrayList<Footnote>();
        
        // add all notes from the list that match the note & sym parameters
        for(Footnote fn : footnotes)
        {
            if(fn == null || fn.getNote() == null) continue;

            // STEP 1: have to make sure the note matches 
            if(note.equals(fn.getNote()))
            {
                // STEP 2: and IF A SYMBOL EXISTS, make sure it matches also
                if(sym != null && sym.length() > 0 && fn.getSymbol() != null)
                {
                    // STEP 2b: notes are the same, but symbol's do not match, so continue without adding to the find list
                    if(!sym.equals(fn.getSymbol()))
                        continue;
                }
                
                // STEP 3: once both conditions are met, add to list
                retVal.add(fn);
            }
        }        
        return retVal;
    }
    
    public static String requiresNewSymbol(Collection<Footnote> noteList, String sym)
    {
        String retVal = sym;
        
        // need to change the symbol if it's already in the list
        if(FootnoteImpl.sameSymbolExists(noteList, sym))
        {
            if(sym.length() == 1)                            
            {
                char c = sym.charAt(0);
                if(Character.isLetter(c))
                {
                    String sym2 = sym.toLowerCase();
                    if(sym2.equals(sym)) 
                        sym2 = sym.toUpperCase();
                    
                    retVal = sym2;
                }                            
            }
            else
            {
                // TODO: HOW TO RENAME A NON CHAR SYMBOL -- RANDOM ????                
            }
        }
        
        return retVal;
    }
    
    public static boolean sameSymbolExists(Collection<Footnote> footnotes, String sym)
    {
        Collection<Footnote> notes = findSameSymbol(footnotes, sym);
        return notes != null && notes.size() > 0;
    }
    public static Collection<Footnote> findSameSymbol(Collection<Footnote> footnotes, String sym)
    {
        Collection<Footnote> retVal = new ArrayList<Footnote>();
        if(sym == null || sym.length() < 1) return retVal;
        
        // add all notes from the list that match the note & sym parameters
        for(Footnote fn : footnotes)
        {
            // STEP 1: ONLY IF A SYMBOL EXISTS, and it MATCHES the target sym
            if(fn.getSymbol() == null || fn.getSymbol().length() < 1) continue;
            if(!sym.equals(fn.getSymbol()))                           continue;
            
            // STEP 2: add it to the list
            retVal.add(fn);
        }        
        return retVal;
    }  
    
}
