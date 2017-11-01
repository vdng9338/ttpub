/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package fr.vdng9338.gtfs.ttpubmod;

import java.awt.Color;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;

import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.Footnote;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.utils.TimeTableProperties;
import org.timetablepublisher.view.pdf.PdfDesign;
import org.timetablepublisher.view.pdf.PdfDesignImpl;

/**
 * Modified from PdfTrimetDesignImpl.
 * 
 * The purpose of PdfTrimetDesignImpl is to provide routines that are called by the iText code
 * in main TimeTable Publisher code, to render a timetable. 
 * 
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 20, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 * @see     org.timetablepublisher.view.pdf.PdfDesign
 */
@SuppressWarnings("unchecked")
public class FrenchPdfDesignImpl extends PdfDesignImpl implements PdfDesign
{
    
    
    public void makeRow(PdfPTable table, Row row, int numCols, boolean doBorder)
    {
        float padding = 1f;
        float leading = 6f;
        Rectangle border = getRowBorder(doBorder);

        // ROW of CELLs
        for(int i = 0; i < numCols; i++)
        {
            Paragraph p   = new Paragraph();
            
            // print the footnote symbol (if exists)
            Phrase sym = getSymbol(row, i);
            if(sym != null)
                p.add(sym);
                
            // print the stop time
            p.add(getTime(row, i));

            PdfPCell cell = makeCell(p, 
                                     Element.ALIGN_BOTTOM, Element.ALIGN_RIGHT, 
                                     leading, padding, border, true, false);
            cell.setFixedHeight  (doBorder ? 9f : 7f);
            cell.setPaddingBottom(doBorder ? 2f : 1f);
            cell.setPaddingRight (4f);
            p.setLeading(leading);
            table.addCell(cell);
        }        
    }
    
    
    
    public Font getFont(Row row, int index)
    {
        Font retVal = HELV_NORM_6;
        /*if(row != null && row.getCell(index) != null)
        {
            if((row.getTime(index) % 86400) >= 43200)
                retVal = HELV_BOLD_6;
        }*/
                
        return retVal; 
    }
    
    public Phrase getSymbol(Row row, int index)
    {
        Phrase retVal = null;
        if(row != null && row.getCell(index) != null)
        {
            String sym = row.getFootnoteSymbol(index);
            if(sym != null && sym.length() > 0)
            {
                retVal = new Phrase(sym, getFont(row, index));
            }
        }
        
        return retVal;
    }
    
    public Phrase getTime(Row row, int index)
    {
        return getTime(row, index, null, null, true);
    }
    
    /**
     * 
     * 
     * @param row
     * @param index
     * @param am
     * @param pm
     * @param toRight
     * @return Phrase
     */
    public Phrase getTime(Row row, int index, String am, String pm, boolean toRight)
    {
        if(row == null || row.getCell(index) == null) 
            return new Phrase(DASH, HELV_NORM_6);
        
        // determine whether there's an AM / PM mark to add to time string
        String mark = null;
        /*if((row.getTime(index) % 86400) >= 43200)
        {
            if(pm != null)
                mark = pm;
        }
        else
        {
            if(am != null)
                mark = am;            
        }*/
        
        // get time string, and append mark in appropriate place
        int timeInt = row.getTime(index);
        int hours = (timeInt / 3600) % 24;
        int minutes = (timeInt / 60) % 60;
        String time = hours + ":" + minutes;
        if(mark != null)
        {
            if(toRight)
                time = time + mark;
            else
                time = mark + time;
        }

        Phrase retVal = new Phrase(time, getFont(row, index));
        // superscript (since deleted): c.setTextRise(3f);
        
        return retVal;
    }

        
    public void makeFootnotes(PdfPTable table, Collection<Footnote> footnotes)
        throws DocumentException
    {
        float padding = 2f;
        float leading = 8f;
        Rectangle border = new Rectangle(0f, 0f);
        border.setBorderWidthLeft(0f);
        border.setBorderWidthRight(0f);
        border.setBorderWidthBottom(0f);
        border.setBorderWidthTop(3f);
        border.setBorderColor(Color.BLACK);
        
        // table bottom bar & easter egg watermark 
        PdfPCell space = makeCell("TimeTablePublisher.com : Génération de PDF, créé originalement par Frank Purcell.",
                          FontFactory.getFont("Helvetica", 4, Font.BOLD, Color.WHITE),
                          Element.ALIGN_TOP, Element.ALIGN_LEFT, 
                          leading, padding, border, false, false);
        space.setColspan(2);
        space.setFixedHeight(15f);
        table.addCell(space);
        border.setBorderWidthTop(0f);

        
        // makes the two columns different sized - symbol = 5% note = 95%
        float[] widths = {0.05f, 0.95f};
        table.setWidths(widths);
        
        for(Footnote fn : footnotes)
        {         
            PdfPCell symbol = makeCell(fn.getSymbol(),
                                     HELV_BOLD_8,
                                     Element.ALIGN_TOP, Element.ALIGN_RIGHT, 
                                     leading, padding, border, false, false);
            table.addCell(symbol);
            
            Paragraph p   = formatNote(fn.getFormattedNote(), HELV_NORM_8, HELV_BOLD_8);
            p.setLeading(leading);
            PdfPCell note = makeCell(p,
                    Element.ALIGN_TOP, Element.ALIGN_LEFT, 
                    leading, padding, border, false, false);

            table.addCell(note);
        }        
    }

    public List<String> getFooterContent()
    {        
        return null;
    }
    

    /**
     * Builds the document footer
     * 
     * @note uses HTMLWorker.parseToList
     * @see  http://www.mail-archive.com/itext-questions@lists.sourceforge.net/msg20413.html
     */
    public PdfPTable getDocumentFooter(float width)
    {
        // step 0: get content that we're going to use for the footer 
        List<String> notes = getFooterContent();        
        if(notes == null || notes.size() < 1) return null;
        
        // step 1: define the font style for our text
        StyleSheet style = new StyleSheet();        
        style.loadTagStyle("body", "leading",   "9,0"); 
        style.loadTagStyle("body", "size",      "8px");                                
        style.loadTagStyle("body", "font-size", "8px");
        style.loadTagStyle("body", "face",      "ariel"); 

        // step 2: create a table to insert text into footer (min 60% of document width)
        PdfPTable table = new PdfPTable(1);
        if(width < 60.0) width = 60.0f;
        table.setWidthPercentage(width);

        // step 2b: create a boarder for that table
        float padding = 2f;
        float leading = 8f;
        Rectangle border = new Rectangle(0f, 0f);
        border.setBorderWidthLeft(0f);
        border.setBorderWidthRight(0f);
        border.setBorderWidthBottom(0f);
        border.setBorderWidthTop(3f);
        border.setBorderColor(Color.WHITE);

        for(String n : notes)
        {         
            StringReader stringReader = new StringReader(formatNoteUnicodeReplacement(n));
            try
            {
                List<Element> html = HTMLWorker.parseToList(stringReader, style);

                Paragraph para = new Paragraph();
                for (Element e : html)
                    para.add(e);
                            
                PdfPCell note = makeCell(para,
                        Element.ALIGN_TOP, Element.ALIGN_LEFT, 
                        leading, padding, border, false, false);

                table.addCell(note);
            } 
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        return table;
    }

    public void makeStops(PdfPTable table, List<Stop> stops, boolean showStopID)
      throws DocumentException
    {
        float padding = 0f;
        float leading = 0f;
        Rectangle border = new Rectangle(0f, 0f);
        border.setBorderWidthLeft(0f);
        border.setBorderWidthRight(0f);
        border.setBorderWidthBottom(1f);
        border.setBorderWidthTop(1f);
        border.setBorderColor(Color.BLACK);
        
        // ROUTE
        float[] widths = new float[stops.size()];
        int i = 0;
        for(Stop c : stops)
        {         
            String s = c.getDescription();
            widths[i++] = calcColSize(s, stops.size());

            Paragraph p = formatLineBreaks(s, HELV_BOLD_6);
            if(showStopID && !c.hideStopId()) 
            {
                String locStopIdStr = TimeTableProperties.STOP_ID_STRING_NAME.get("Stop ID");
                p.add(new Phrase("\n" + locStopIdStr + " " + c.getStopId(), HELV_NORM_6));
            }
        
            PdfPCell cell = makeCell(p, 
                                     Element.ALIGN_BOTTOM, Element.ALIGN_LEFT, 
                                     leading, padding, border, true, false);
            cell.setFixedHeight(49f);
            cell.setRotation(90);
            p.setLeading(6f);
            cell.addElement(p);
            cell.setPaddingBottom(1f);
            cell.setPaddingRight(3f);
            table.addCell(cell);
        }
        
        setWidths(table, widths);
    }

    public void setWidths(PdfPTable table, float[] widths) throws DocumentException
    {
        table.setWidths(widths);
    }

    public Rectangle getRowBorder(boolean doBorder)
    {        
        Rectangle border = new Rectangle(0f, 0f);
        border.setBorderWidthLeft(0f);
        border.setBorderWidthRight(0f);
        border.setBorderWidthTop(0f);
        border.setBorderWidthBottom(doBorder ? 1f : 0f);
        border.setBorderColor(Color.BLACK);
        return border;
    }
    

    public void makeTitle(PdfPTable table, String routeName, String keyName, String destName)
    {
        float padding = 4f;
        float leading = 12f;
        Rectangle border = new Rectangle(0f, 0f);
        border.setBorderWidthLeft(0f);
        border.setBorderWidthRight(0f);
        border.setBorderWidthBottom(1f);
        border.setBorderWidthTop(0f);
        border.setBorderColor(Color.BLACK);
        
        try
        {
            float[] widths = {0.22f, 0.78f};
            table.setWidths(widths);
        } 
        catch (DocumentException e)
        {
            e.printStackTrace();
        }

    
        // ROUTE
        String formattedRouteName = routeName.replaceAll(", ", "\n");
        PdfPCell route = makeCell(formattedRouteName,
                                  FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.WHITE),
                                  Element.ALIGN_BOTTOM, Element.ALIGN_LEFT, 
                                  leading, padding, border, false, false);
        route.setColspan(2);
        //route.setMinimumHeight(routeName.length() >= 25 ? 40.0f : 20.0f);
        route.setMinimumHeight(20.0f);
        route.setGrayFill(0.0f);
        table.addCell(route);
    
        // KEY
        leading = 8f;
        PdfPCell key = makeCell(keyName,
                                HELV_BOLD_8,
                                Element.ALIGN_BOTTOM, Element.ALIGN_LEFT, 
                                leading, padding, border, false, false);
        key.setPaddingLeft(2f);
        key.setPaddingRight(0f);
        key.setMinimumHeight(12f);        
        table.addCell(key);
    
    
        // DEST
        PdfPCell dest = makeCell(destName,
                                 HELV_BOLD_8,
                                 Element.ALIGN_BOTTOM, Element.ALIGN_RIGHT, 
                                 leading, padding, border, false, false);        
        dest.setPaddingLeft(0f);
        dest.setPaddingRight(2f);
        dest.setFixedHeight(15f);
        table.addCell(dest);
    }
}
