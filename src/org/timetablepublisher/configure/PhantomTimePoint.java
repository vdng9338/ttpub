/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.configure;

import java.util.List;
import java.util.logging.Logger;

import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.CellImpl;
import org.timetablepublisher.table.StopImpl;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.IntUtils;


/**
 * The purpose of PhantomTimePoint is to add a timepoint to a table, as well as a colum of 'generated' stop time, which would otherwise not
 * exist in the schedule.  E.G. One route at TriMet (4-Fess) was a lacking in the number of 'real' timepoints in the schedule data, at 
 * least from a customer information perspective.  The solution was to add these phantom timepoints, which are real stops on the route 
 * where we knew the bus visited at an interval of X minutes from a real timepoint.  It's a bit of a hack, and should be used with
 * exteme caution.  Basically, you have to know for certain that the bus hits this stop every time it hits the reference timepoint.  If there
 * are trips and patterns that hit the reference timepoint, but don't hit this stop, then this routine will be giving false information.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 19, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class PhantomTimePoint extends Configure
{
    private static final Logger LOGGER = Logger.getLogger(PhantomTimePoint.class.getCanonicalName());    
    
    public enum PhantomRule
    {
        LEFT, RIGHT;
        
        public static PhantomRule construct(String str)
        {
            PhantomRule retVal;
            try
            {
                if(str != null && str.length() > 1)
                {
                     retVal = PhantomRule.valueOf(str);
                }
                else
                {
                    retVal = PhantomRule.LEFT;
                }
            }
            catch (RuntimeException e)
            {
                LOGGER.log(DEBUG, "error creating rule with string " + str, e);
                retVal = PhantomRule.LEFT;
            }  
            
            return retVal;
        }
    }
    
    
    @CsvColumn(index=6, name="Phantom TP Rule (eg: LEFT / RIGHT)", details="place this new 'phantom' timepoint to the left/right of the specified stopID")
    public PhantomRule m_rule;
    
    @CsvColumn(index=7, name="Reference Stop ID", details="this is a valid stop in the table -- and this stop will be our phantom's neighbor (to either the left or right)")
    public String   m_referenceStopID;
    
    @CsvColumn(index=8, name="Offset Time (in Minutes)", details="using the times in the referece timepoint, we'll add times to the phantom with this offset (in minutes)")
    public int      m_timeOffeset;
    
    @CsvColumn(index=9, name="Printed Stop ID", details="the stop id given to our phantom")
    public String   m_printedStopID;
    
    @CsvColumn(index=10, name="Stop Name", details="the name / description given to this phantom stop")
    public String   m_printedStopDescription;

    @CsvColumn(index=11, name="Move Footnote Symbols To New Cells", details="if the reference stop has any footnotes (with these symbols), move them to the phantom")
    public String   m_moveFootnoteSymbol;
    
    public PhantomTimePoint()
    {        
    }
    public PhantomTimePoint(String agency, String route, String dir, String key)
    {
        super(agency, route, dir, key);
    }
    public PhantomTimePoint(String agency, String route, String dir, String key, String lang)
    {
        super(agency, route, dir, key, lang);
    }
    
    public static PhantomTimePoint instantiate(TimesTable tt)
    {        
        return (PhantomTimePoint)tt.getConfiguration().findData(new PhantomTimePoint(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString(), tt.getLanguage()));
    }

    public String getPrintedStopDescription()
    {
        return m_printedStopDescription;
    }
    public String getPrintedStopID()
    {
        return m_printedStopID;
    }
    public String getReferenceStopID()
    {
        return m_referenceStopID;
    }
    public PhantomRule getRule()
    {
        return m_rule;
    }
    public int getTimeOffeset()
    {
        return m_timeOffeset * 60; // turn minutes (config file) into seconds 
    }
    public String rawMoveFootnoteSymbol()
    {
        return m_moveFootnoteSymbol;
    }    
    
    // setters
    public void setMoveFootnoteSymbol(String moveFootnoteSymbol)
    {
        if(moveFootnoteSymbol == null) return;
        m_moveFootnoteSymbol = moveFootnoteSymbol.trim();
    }
    public void setPrintedStopDescription(String printedStopDescription)
    {
        if(printedStopDescription == null) return;
        m_printedStopDescription = printedStopDescription.trim();
    }
    public void setPrintedStopID(String printedStopID)
    {
        if(printedStopID == null) return;
        m_printedStopID = printedStopID.trim();
    }
    public void setReferenceStopID(String referenceStopID)
    {
        if(referenceStopID == null) return;
        m_referenceStopID = referenceStopID.trim();
    }
    public void setRule(String rule)
    {
        m_rule = PhantomRule.construct(rule);
    }
    public void setTimeOffeset(String offset)
    {
        m_timeOffeset = IntUtils.getIntegerFromString(offset, 0);
    }
    
    
    /**
     * NOTE: you should trim() the returned strings
     * @return array of strings that are supposedly the footnote symbols
     */
    public String[] moveFootnoteSymbol()
    {
        String[] retVal = null;
        if(m_moveFootnoteSymbol != null && m_moveFootnoteSymbol.length() > 0)
        {
            retVal = m_moveFootnoteSymbol.split(",");
        }
        return retVal; 
    }    


    
    /**
     * Will read the configuration, and add time points and estimated times to the TimeTable
     * 
     * @param m_tt
     */
    synchronized public static void process(TimesTable tt)
    {
        if(!okToProcessHasTimeTable(tt, Configure.class)) return;
        
        PhantomTimePoint index = new PhantomTimePoint(tt.getAgencyName(), tt.getRouteID(), tt.getDir().toString(), tt.getKey().toString());
        List ptpList = tt.getConfiguration().findAllData(index);
        if(ptpList == null || ptpList.size() < 1) return;
        
        for(PhantomTimePoint ptp : (List<PhantomTimePoint>)ptpList)
        {
            if(ptp == null || ptp.getReferenceStopID() == null || ptp.getPrintedStopID() == null) continue;
            
            if(StopImpl.isStopInList(tt.getTimePoints(), ptp.getReferenceStopID()))
            {
                List<Row> rows = tt.getTimeTable();
                Integer i = StopImpl.findColumnIndex(tt.getTimePoints(), ptp.getReferenceStopID());
                if(i == null) continue;
                
                int  newCellCount = 0;
                int  newI = (ptp.getRule() == PhantomRule.LEFT) ? i : i+1;
                
                String[] syms = ptp.moveFootnoteSymbol();
                                
                for(Row r : rows)
                {
                    Cell c = r.getCell(i);
                    Cell newC = null;
                    
                    if(c != null) 
                    {
                        newC = new CellImpl(c);
                        newC.setFootnoteSymbol(null);
                        

                        if(ptp.getRule() == PhantomRule.LEFT)
                        {
                            // subtract time (eg: to left, so earlier)
                            if(syms != null && syms.length > 0) {
                                footnoteSymbol(PhantomRule.LEFT, syms, newC, c, ptp, r);
                            }
                            newC.setTime( c.getTime() - ptp.getTimeOffeset() );
                            newCellCount++;
                        }
                        else
                        {
                            // add time (eg: to right, so later)
                            if(syms != null && syms.length > 0) {
                                footnoteSymbol(PhantomRule.RIGHT, syms, newC, c, ptp, r);
                            }
                            newC.setTime( c.getTime() + ptp.getTimeOffeset() );
                            newCellCount++;
                        }
                    }
                    
                    List<Cell> cells = r.getRow();
                    cells.add(newI, newC);                    
                }
                
                if(newCellCount > 0)
                {
                    StopImpl newCol = new StopImpl(ptp.getPrintedStopID(), newI, ptp.getPrintedStopDescription());
                    newCol.setSequence(Constants.PHANTOM_SEQ);
                    tt.getTimePoints().add(newI, newCol);
                }
            }            
        }            
    }
    private static void footnoteSymbol(PhantomRule position, String[] syms, Cell newCell, Cell oldCell, PhantomTimePoint ptp, Row row)
    {
        if(oldCell == null || newCell == null) return;
        
        String oldSym = oldCell.getFootnoteSymbol();
        if(oldSym == null || oldSym.length() < 1) return;
        
        // want to first fix the footnote symbol stuff        
        for(String s : syms)
        {
            if(s == null) continue;
            if(oldSym.equals(s.trim())) 
            {
                // new Phantom cell gets the footnote
                newCell.setFootnoteSymbol( oldCell.getFootnoteSymbol() );
                oldCell.setFootnoteSymbol(null);
            }
        }
    }
}    

