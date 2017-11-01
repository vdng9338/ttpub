/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;

import java.util.Comparator;

/**
 * The purpose of Footnote is as an interface for a Footnote, within the TimeTable Publisher.  
 * NOTE: FootnoteImpl implements a token replace language...see the public final strings below for more.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 19, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public interface Footnote
{
    // NOTE: these Constants are used in a mini Token Replacement language.  Each of these symbols, when they appear 
    //       within a footnote, in conjunction with the set/getFormattedNote methods, will have the Token string replaced
    //       with some valid schedule data that makes sense for the token.  TODO -- more token documentation needed.
    public final String PRINTED_ID   = "PRINTED.ID";
    public final String SOURCE_ID    = "SOURCE.ID";
    public final String PRINTED_NAME = "PRINTED.NAME";
    public final String SOURCE_NAME  = "SOURCE.NAME";
    
    public final String PLUS_       = "STIME.PLUS_";
    public final String MINUS_      = "STIME.MINUS_";
    public final String STOP_ID     = "STOP.ID";
    public final String STOP_NAME   = "STOP.NAME";
    public final String STOP_TIMES  = "STOP.TIMES";
    public final String START_ID    = "START.ID";
    public final String START_NAME  = "START.NAME";
    public final String START_TIMES = "START.TIMES";
    public final String END_ID      = "END.ID";
    public final String END_NAME    = "END.NAME";
    public final String END_TIMES   = "END.TIMES";

    
    String getId();
    Integer getSequence();
    void setSequence(Integer sequence);
    
    String getNote();
    void setNote(String note);

    String getSymbol();
    void setSymbol(String symbol);

    String getFormattedNote();
    void setFormattedNote(String formattedNote);
    
    /** 
     * Compares its two arguments for sort order.
     * Returns a negative integer, zero, or a positive integer 
     * if the first argument is less than, equal to, or greater than the second. 
     */
    static public class Compare implements Comparator<Footnote>
    {
        public int compare(Footnote fn1, Footnote fn2)
        {
            if(fn1 == null || fn1.getSequence() == null) return 1;
            if(fn2 == null || fn2.getSequence() == null) return -1;
            
            return fn1.getSequence() - fn2.getSequence();
        }
    }    
}
