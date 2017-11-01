/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.pdf;

import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.Chunk;

import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.IntUtils;

/**
 * The purpose of PdfDesignImpl is to provide a default set of routines for PDF production.
 * This class can be extended, and additional look & feel provided from customization of the
 * PdfDesign interface routines.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 26, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 * @see     org.timetablepublisher.view.pdf.PdfDesign
 */
abstract public class PdfDesignImpl implements PdfDesign
{
    /**
     * The purpose of PageEvent is to print the page and effective date on each page of the pdf
     * 
     * @author  Frank Purcell (purcellf@trimet.org)
     * @date    Oct 26, 2006
     * @project http://timetablepublisher.org
     * @version Revision: 1.0
     * @since   1.0
     */
    static public class PageEvent extends PdfPageEventHelper 
    {
        protected Date m_date = null;
        
        public PageEvent()
        {
            m_date = new Date();
        }
        public PageEvent(String date)
        {
            m_date = IntUtils.getDate(date);
        }
        
        
        public void onEndPage(PdfWriter writer, Document document) 
        {
            super.onEndPage(writer, document);
            
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            
            BaseFont helv = null;
            try
            {
                helv = BaseFont.createFont(FontFactory.HELVETICA, BaseFont.WINANSI, false);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // start text
            cb.beginText();
            float textBase = document.bottom() - 20;
            
            // page number
            cb.setFontAndSize(helv, 8);
            cb.setTextMatrix(document.left(), textBase);
            cb.showText("Page #" + writer.getPageNumber());

            
            // create date
            SimpleDateFormat dt = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_NICE);
            String created = "Created on: " + dt.format(new Date());
            float center = ((document.right() - document.left()) / 2) - (20 + created.length() / 2);
            cb.setTextMatrix(center, textBase);
            cb.showText(created);
            
            // effective date
            String date = getEffectiveDateString();
            cb.setTextMatrix(document.right() - (70 + date.length()), textBase);
            cb.showText(date);
            
            // end text
            cb.endText();
            cb.saveState();
        }
        
        public String getEffectiveDateString()
        {
            SimpleDateFormat d = new SimpleDateFormat(Constants.PRETTY_DATE_FORMAT);
            String date = "Effective: " + d.format(m_date);            
            return date;
        }
    }
    
    public PdfPageEventHelper getPageEvent(TimesTable tt)
    {
        return new PageEvent(tt.getDate());
    }

    
    public PdfPTable getDocumentFooter(float width)
    {
        return null;
    }

    
    /**
     * 
     * 
     * @param m_tt
     * @param output
     */
    public void makeDocument(TimesTable tt, OutputStream output)
    {
        // step 1: creation of a document-object
        Document document = new Document(PageSize.LETTER);
                
        try 
        {            
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter writer = PdfWriter.getInstance(document, output);

            // step 3: page event -- header & footer control
            PdfPageEventHelper event = getPageEvent(tt);
            if(event != null) {
                writer.setPageEvent(event);
            }

            // step 3: we open the document & set some basic parameters
            document.open();
            document.setPageSize(PageSize.LETTER);
            int numCols = tt.getTimePoints().size();
            PdfPTable head  = new PdfPTable(2);
            PdfPTable foot  = new PdfPTable(2);
            PdfPTable table = new PdfPTable(numCols);
            
            float width = getWidthPercentage(numCols);
            head.setWidthPercentage(width);
            foot.setWidthPercentage(width);
            table.setWidthPercentage(width);
            table.setHeaderRows(1);
            
            // TITLE
            makeTitle(head, tt.getRouteName(), tt.getKeyName(), tt.getDestination());

            // STOPS
            makeStops(table, tt.getTimePoints(), tt.showStopIDs());

            // ROWS
            int i = 1;
            for(Row r : tt.getTimeTable())
            {
                makeRow(table, r, numCols, i % 5 == 0 ? true : false);
                i++;
            }

            // FOOTNOTES
            makeFootnotes(foot, tt.getFootnotes());
            PdfPTable footer  = getDocumentFooter(width);

            // PUT TABLE INTO DOCUMENT
            document.add(head);
            document.add(table);
            document.add(foot);
            if(footer != null)
                document.add(footer);
        } 
        catch (DocumentException de) 
        {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        if(document != null && document.isOpen()) {
            document.close();
        }
    }


    public float getWidthPercentage(int numCols)
    {
        float retVal = (float)(numCols * 6);
        if(retVal < 36)  retVal = 36.0f;
        if(retVal > 100) retVal = 100.0f;
        
        return retVal;
    }

    public static float calcColSize(String str, int numCols)
    {
        if(numCols <= 1) return 1;

        if(str.length() > 24) return 1.10f / numCols;  // large string
        if(str.length() < 13) return 0.90f / numCols;  // small string
        return 1.0f / numCols;                         // normal sized string
    }

    
    public static Chunk getTextElement(String string, Font font) 
    {
        Chunk retVal = new Chunk(string, font);
        return retVal; 
    }
    synchronized public static String formatNoteUnicodeReplacement(String note)
    {
        String retVal = new String(note);
        retVal = retVal.replaceAll(DASH_HTML, DASH);
        retVal = retVal.replaceAll(COPY_HTML, COPY);
        retVal = retVal.replaceAll(BULL_HTML, BULL);
        retVal = retVal.replaceAll(TM_HTML,   TM);
        retVal = retVal.replaceAll(YEAR_HTML, Constants.YEAR.format(new Date()));
        
        return retVal;        
    }

    // TODO: think about rewriting this with HTMLWorker.parseToList
    // @see WebPdfDesignImpl.java
    // @see http://www.mail-archive.com/itext-questions@lists.sourceforge.net/msg20413.html
    public static Paragraph formatNote(String note, Font norm, Font bold)
    {
        Paragraph retVal = new Paragraph();
        
        if(note.contains(UC_BOLD) || note.contains(LC_BOLD))
        {
            // step 0: we have this string: "Hi There <B>Buddy</B>"
            //         to simplify our logic below, we'll mainipulate it a bit
            String tmpNote = " " + note + " ";

            // step 1: break it into " Hi There " "Buddy</B> "
            String[] stuff = tmpNote.split(REGEX_BOLD);
            for(String s : stuff)
            {
                // step 2: break it again on the </B>
                String[] splits = s.split(REGEX_CL_BOLD);
                if(splits.length < 1) continue;
                
                if(splits.length == 1)
                {
                    // step 2a: make sure we're dealing with a real string
                    if(splits[0] == null)     continue;
                    if(" ".equals(splits[0])) continue;
                    
                    // step 2b: OK, we're dealing with " Hi There "
                    String tmp = splits[0].trim() + " ";
                    retVal.add(new Chunk(tmp, norm));
                }
                else if(splits.length == 2)
                {
                    // step 2a: make sure we're dealing with a real string
                    if(splits[0] == null)     continue;
                    if(" ".equals(splits[0])) continue;

                    // step 2c: OK, we're dealing with "Buddy" " "
                    retVal.add(new Chunk(splits[0], bold));
                    
                    // step 2d: again check for goodness, then print
                    if(splits[1] == null)     continue;
                    if(" ".equals(splits[1])) continue;
                    retVal.add(new Chunk(splits[1], norm));
                }
                else
                {
                    System.out.println("Error: PDF -- Unexpected");
                    
                    // just print the thing
                    retVal.add(new Chunk(s, norm));                    
                }
            }
        }
        else
        {
            retVal.add(new Chunk(note, norm));
        }

        return retVal;
    }

    
    public static Paragraph formatLineBreaks(String input, Font font)
    {
        return formatLineBreaks(input, LINE_BREAK, true, font);
    }
    public static Paragraph formatLineBreaks(String input, String ctlChar, boolean splitChar, Font font)
    {       
        Paragraph retVal = new Paragraph();
        if(input == null) return retVal;
        
        // step 1: break it into "Hi There<BR/>Buddy"
        String[] splits = input.split(ctlChar);
        if(splits.length < 1) return retVal;
        
        // step 2: make the Paragraph with the different split lines        
        if(splits.length == 1)
        {
            // 2a: there were no line breaks, so just add this.
            Chunk chunk = new Chunk(input, font);
            if(splitChar)
                chunk.setSplitCharacter(new SplitChar());
            
            retVal.add(chunk);
        }
        else
        {
            // 2b: there were line breaks, so add the first split, then the subsequent with NEW_LINE chars 
            retVal.add(new Phrase(splits[0], font));            
            for(int i = 1; i < splits.length; i++)
            {
                 // 2c: add each additional split with a new line
                retVal.add(new Phrase(NEW_LINE + splits[i], font));
            }
        }

        return retVal;
    }
    
    
    public PdfPCell makeCell(String text, Font font, 
                             int vAlignment, int hAlignment, 
                             float leading, float padding, 
                             Rectangle borders, boolean ascender, boolean descender) 
    {
        String tmp;
        if(text == null)                  
            tmp = " ";
        else
            tmp = new String(text);
        
        if(tmp.contains(Constants.SPACE)) 
            tmp = tmp.replaceAll(Constants.SPACE, " ");
        
        Paragraph p = new Paragraph(tmp, font);
        p.setLeading(leading);
        return makeCell(p, vAlignment, hAlignment, leading, padding, borders, ascender, descender);
    }


    public PdfPCell makeCell(Paragraph paragraph,
                             int vAlignment, int hAlignment, 
                             float leading, float padding, 
                             Rectangle borders, boolean ascender, boolean descender) 
    {
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setLeading(leading, 0);
        cell.setVerticalAlignment(vAlignment);
        cell.setHorizontalAlignment(hAlignment);
        cell.cloneNonPositionParameters(borders);
        cell.setUseAscender(ascender);
        cell.setUseDescender(descender);
        cell.setUseBorderPadding(true);
        cell.setPadding(padding);

        return cell;
    }    
}

