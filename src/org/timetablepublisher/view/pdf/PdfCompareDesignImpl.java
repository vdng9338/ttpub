/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.pdf;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;

import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.IntUtils;

/**
 * The purpose of PdfCompareDesignImpl is to TODO
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Jan 12, 2007
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class PdfCompareDesignImpl extends TrimetPdfDesignImpl implements PdfDesign
{
    protected String m_dateA = "1-1-2000";
    protected String m_dateB = "1-1-2000";
    
    public PdfCompareDesignImpl(TimesTable ttA, TimesTable ttB)
    {
        if(ttA != null && ttA.getDate() != null) m_dateA = ttA.getDate(); 
        if(ttB != null && ttB.getDate() != null) m_dateB = ttB.getDate(); 
    }
    
    static public class PageEvent extends PdfDesignImpl.PageEvent
    {
        private Date m_diffDate = null;
        
        public PageEvent(String date, String diffDate)
        {
            super(date);
            m_diffDate = IntUtils.getDate(diffDate);
        }

        synchronized public String getEffectiveDateString()
        {
            SimpleDateFormat d = new SimpleDateFormat(Constants.DATE_FORMAT);
            String date = "Compare: " + d.format(m_date) + " v. " + d.format(m_diffDate);            
            return date;
        }
    }
    
    public PdfPageEventHelper getPageEvent(TimesTable tt)
    {
        return new PageEvent(m_dateA, m_dateB);
    }
    
    
    public void makeRow(PdfPTable table, Row row, int numCols, boolean doBorder)
    {
        addDateAndTrip(table, row, doBorder);
        super.makeRow(table, row, numCols, doBorder);
    }
    
    public Font getFont(Row row, int index)
    {
        Font retVal = HELV_NORM_6;
        Cell c = row.getCell(index);
        if(row != null && c != null)
        {
            if((row.getTime(index) % 86400) >= 43200) 
            {
                if(c.isHighlighted()) {
                    retVal = HIGHLIGHT_BOLD_6;    
                }
                else {
                    retVal = HELV_BOLD_6;    
                }
            }
            else
            {
                if(c.isHighlighted()) {
                    retVal = HIGHLIGHT_NORM_6;
                }
                else {
                    retVal = HELV_NORM_6;    
                }                
            }
        }
                
        return retVal; 
    }
    

    protected void addDateAndTrip(PdfPTable table, Row row, boolean doBorder)
    {
        Rectangle border = getRowBorder(doBorder);
        addCell(row.getDate(), table, row, border, doBorder);
        addCell(row.getTrip(), table, row, border, doBorder);
    }    

    private void addCell(String string, PdfPTable table, Row row, Rectangle border, boolean doBorder)
    {
        float padding = 1f;
        float leading = 6f;
        
        Paragraph p = new Paragraph();
        p.add(new Phrase(string, HELV_NORM_6));
        PdfPCell cell = makeCell(p, 
                                 Element.ALIGN_BOTTOM, Element.ALIGN_RIGHT, 
                                 leading, padding, border, true, false);
        cell.setFixedHeight  (doBorder ? 9f : 7f);
        cell.setPaddingBottom(doBorder ? 2f : 1f);
        cell.setPaddingRight (4f);
        p.setLeading(leading);
        table.addCell(cell);        
    }

    protected void spacing(PdfPCell cell, boolean doBorder)
    {
        cell.setFixedHeight  (doBorder ? 9f : 7f);
        cell.setPaddingBottom(doBorder ? 2f : 1f);
        cell.setPaddingRight (4f);
    }
    
    public void addDateAndStopColumnToTable(PdfPTable table)
    {
        makeColumn("Date", table);
        makeColumn("Trip", table);
    }

    public void setWidths(PdfPTable table, float[] widths) throws DocumentException
    {
        int newLen = widths.length + 2;
        float[] newWidths = new float[newLen];
        newWidths[0] = calcColSize(" BIG  Date    ", newLen);
        newWidths[1] = calcColSize(" Trip ",   newLen);
        for(int i = 2; i < newLen; i++)
        {
            newWidths[i] = widths[i-2]; 
        }
        
        table.setWidths(newWidths);
    }

    private void makeColumn(String s, PdfPTable table)
    {
        float padding = 0f;
        float leading = 0f;
        Rectangle border = new Rectangle(0f, 0f);
        border.setBorderWidthLeft(0f);
        border.setBorderWidthRight(0f);
        border.setBorderWidthBottom(1f);
        border.setBorderWidthTop(1f);
        border.setBorderColor(Color.BLACK);

        Paragraph p = formatLineBreaks(s, HELV_BOLD_6);
        PdfPCell cell = makeCell(p, 
                                 Element.ALIGN_BOTTOM, Element.ALIGN_LEFT, 
                                 leading, padding, border, true, false);
        cell.setFixedHeight(49f);
        cell.setRotation(0);
        p.setLeading(6f);
        cell.addElement(p);
        cell.setPaddingBottom(1f);
        cell.setPaddingRight(3f);
        table.addCell(cell);
    }
}

