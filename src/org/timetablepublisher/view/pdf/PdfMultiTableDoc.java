/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.pdf;

import java.io.OutputStream;

import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PdfMultiTableDoc
{
    private static final int LARGER_PAGE = 70;
    
    private Document     m_document = null;
    private PdfDesign    m_design;
    private PdfWriter    m_writer ;    
    private OutputStream m_output;
    private String       m_svcDate;    

    public PdfMultiTableDoc(PdfDesign design, OutputStream output, String svcDate) 
    {
        m_design  = design;
        m_output  = output;
        m_svcDate = svcDate;
    }
        
    
    public void open(Rectangle size)
       throws DocumentException    
    {
        if(m_document == null || !m_document.isOpen())
        {
            m_document = new Document(size);            
            m_writer   = PdfWriter.getInstance(m_document, m_output);
            m_writer.setPageEvent(new PdfDesignImpl.PageEvent(m_svcDate));
            
            m_document.open();
            m_document.setPageSize(size);            
        }
        
        m_document.setPageSize(size);
    }

    public void close()
    {
        if(m_document != null && m_document.isOpen()) {
            m_document.close();
        }
    }    

    public void setDesign(PdfDesign design)
    {
        m_design = design;
    }     
    
    /**
     * 
     * 
     * @param m_tt
     * @param output
     */
    public void addTable(TimesTable tt)
    {
        if(tt == null || tt.getTimePoints() == null || tt.getTimeTable() == null) return;
        
        try 
        {            
            if(tt.getTimeTable().size() > LARGER_PAGE) 
                open(PageSize.LEGAL);
            else
                open(PageSize.LETTER);
            
            m_document.newPage();            
            
            // document & set some basic parameters
            PdfPTable head  = new PdfPTable(2);
            PdfPTable foot  = new PdfPTable(2);

            int numCols = tt.getTimePoints().size();
            PdfPTable table = new PdfPTable(numCols);

            float width = m_design.getWidthPercentage(numCols);
            head.setWidthPercentage(width);
            foot.setWidthPercentage(width);
            table.setWidthPercentage(width);
            
            table.setHeaderRows(1);

            // TITLE
            m_design.makeTitle(head, tt.getRouteName(), tt.getKeyName(), tt.getDestination());

            // STOPS
            m_design.makeStops(table, tt.getTimePoints(), tt.showStopIDs());

            // ROWS
            int i = 1;
            for(Row r : tt.getTimeTable())
            {
                m_design.makeRow(table, r, numCols, i % 5 == 0 ? true : false);
                i++;
            }

            // FOOTNOTES
            m_design.makeFootnotes(foot, tt.getFootnotes());

            // PUT TABLE INTO DOCUMENT
            m_document.add(head);
            m_document.add(table);
            m_document.add(foot);
        } 
        catch (DocumentException de) 
        {
            System.err.println(de.getMessage());
        }
    }
}
