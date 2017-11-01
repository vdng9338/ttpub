/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.trimet.ttpub.view.pdf;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.Element;
import com.lowagie.text.Chunk;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;

import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.Footnote;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.view.pdf.PdfDesign;
import org.timetablepublisher.view.pdf.PdfDesignImpl;

/**
 * 
 */
public class TrimetSequenceBookPdfDesignImpl extends PdfDesignImpl implements PdfDesign
{
    public void makeRow(PdfPTable table, Row r, int numCols, boolean doBorder)
    {
        float padding = 1f;
        float leading = 6f;
        Rectangle border = new Rectangle(0f, 0f);
        border.setBorderWidthLeft(0f);
        border.setBorderWidthRight(0f);
        border.setBorderWidthTop(0f);
        border.setBorderWidthBottom(doBorder ? 1f : 0f);
        border.setBorderColor(Color.BLACK);

        // ROW of CELLs
        for(int i = 0; i < numCols; i++)
        {
            Paragraph p   = new Paragraph();
            String    str = r.getTimeAsStr(i);
            if(str == null) {
                p.add(new Phrase(DASH, COUR_NORM_6));
            } 
            else 
            {
                String fn = r.getFootnoteSymbol(i);
                if(fn != null && fn.length() >= 1) 
                {
                    Chunk c = new Chunk(fn, COUR_NORM_4);
                    c.setTextRise(3f);
                    p.add(c);
                }
                p.add(new Phrase(str, ((r.getTime(i) % 86400) >= 43200) ? COUR_BOLD_6 : COUR_NORM_6));
            }

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
        
        // bottom table bar && easter egg watermark                
        PdfPCell space = makeCell("TimeTablePubliser.com: PDF generation —  created by Frank Purcell.",
                          COUR_WHITE_4,
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
                                     COUR_NORM_8,
                                     Element.ALIGN_TOP, Element.ALIGN_RIGHT, 
                                     leading, padding, border, false, false);
            table.addCell(symbol);
            
            Paragraph p   = formatNote(fn.getFormattedNote(), COUR_NORM_8, COUR_BOLD_8);
            p.setLeading(leading);
            PdfPCell note = makeCell(p,
                    Element.ALIGN_TOP, Element.ALIGN_LEFT, 
                    leading, padding, border, false, false);

            table.addCell(note);
        }        
    }


    public void makeStops(PdfPTable table, List<Stop> stops, boolean showStopIDs)
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

            Paragraph p = new Paragraph();
            Chunk chunk = new Chunk(s, COUR_BOLD_6);
            if(s.contains("/")) {
                chunk.setSplitCharacter(new SplitChar());
            }
            p.add(chunk);
            if(showStopIDs && !c.hideStopId()) 
            {
                p.add(new Phrase("\nStop ID " + c.getStopId(), COUR_NORM_6));
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
        
        table.setWidths(widths);
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
        } catch (DocumentException e)
        {
            e.printStackTrace();
        }

    
        // ROUTE
        String formattedRouteName = routeName.replaceAll(", ", "\n");
        PdfPCell route = makeCell(formattedRouteName,
                                  COUR_WHITE_12,
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
                                COUR_BOLD_8,
                                Element.ALIGN_BOTTOM, Element.ALIGN_LEFT, 
                                leading, padding, border, false, false);
        key.setPaddingLeft(2f);
        key.setPaddingRight(0f);
        key.setMinimumHeight(12f);        
        table.addCell(key);
    
    
        // DEST
        PdfPCell dest = makeCell(destName,
                                 COUR_BOLD_8,
                                 Element.ALIGN_BOTTOM, Element.ALIGN_RIGHT, 
                                 leading, padding, border, false, false);        
        dest.setPaddingLeft(0f);
        dest.setPaddingRight(2f);
        dest.setFixedHeight(15f);
        table.addCell(dest);
    }
}
