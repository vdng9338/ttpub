/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.pdf;

import java.awt.Color;
import java.io.OutputStream;
import java.util.List;

import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.TimeTableCompare;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class PdfCompareTableDoc
{
    private static final int LARGER_PAGE = 70;
    
    private PdfCompareDesignImpl m_design; // note: we 'should' make an interface for this
    private Document     m_document;
    private PdfWriter    m_writer ;    
    private OutputStream m_output;
    private String       m_svcDate;    
    private List<TimeTableCompare> m_diffList;
    private boolean      m_printAllCompares;
    
    public PdfCompareTableDoc(List<TimeTableCompare> diffList, PdfCompareDesignImpl design, OutputStream output, String svcDate, boolean printAll) 
    {
        m_design  = design;
        m_output  = output;
        m_svcDate = svcDate;
        m_diffList = diffList;
        m_printAllCompares = printAll;
        process();
    }
        
    
    public void process()
    {
        for(TimeTableCompare c : m_diffList)
        {
            if(c == null) continue;
            if(m_printAllCompares || !c.areStopTimesEqual()) 
            {
                addTable(c.getScheduleA(), c.getDiffTable());                
            }
        }
        close();
    }


    public void open(Rectangle size)
       throws DocumentException    
    {
        if(m_document == null || !m_document.isOpen())
        {
            m_document = new Document(size);            
            m_writer   = PdfWriter.getInstance(m_document, m_output);
            m_writer.setPageEvent(m_design.getPageEvent(null));
            
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

    public void setDesign(PdfCompareDesignImpl design)
    {
        m_design = design;
    }     
    
    /**
     * 
     * 
     * @param m_tt
     * @param name 
     * @param output
     */
    public void addTable(TimesTable tt, List<Row> diffTable)
    {
        if(tt == null || diffTable == null || tt.getTimePoints() == null) return;
        
        try 
        {            
            if(diffTable.size() > LARGER_PAGE) 
                open(PageSize.LEGAL);
            else
                open(PageSize.LETTER);
            
            m_document.newPage();            
            
            // document & set some basic parameters
            PdfPTable head  = new PdfPTable(2);

            int numCols = tt.getTimePoints().size();
            PdfPTable table = new PdfPTable(numCols + 2);

            float width = m_design.getWidthPercentage(numCols + 7);
            head.setWidthPercentage(width);
            table.setWidthPercentage(width);
            
            table.setHeaderRows(1);

            // TITLE
            m_design.makeTitle(head, "Compare: " + tt.getRouteName(), tt.getKeyName(), tt.getDestination());

            // STOPS
            m_design.addDateAndStopColumnToTable(table);
            m_design.makeStops(table, tt.getTimePoints(), tt.showStopIDs());

            // ROWS
            for(int i = 0; i < diffTable.size() - 1; i+=2)                
            {
                Row A = diffTable.get(i);
                Row B = diffTable.get(i+1);
                
                m_design.makeRow(table, A, numCols, false);
                m_design.makeRow(table, B, numCols, true);
            }

            // PUT TABLE INTO DOCUMENT
            m_document.add(head);
            m_document.add(table);
        } 
        catch (DocumentException de) 
        {
            System.err.println(de.getMessage());
        }
    }
}
