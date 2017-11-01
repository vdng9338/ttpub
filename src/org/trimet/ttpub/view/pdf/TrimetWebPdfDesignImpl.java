/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.trimet.ttpub.view.pdf;


import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.FileUtils;
import org.timetablepublisher.utils.IntUtils;
import org.timetablepublisher.view.pdf.TrimetPdfDesignImpl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;


/**
 * The purpose of WebPdfDesignImpl is to provide the design for the trimet.org
 * website, which expands on the TrimetPDF Design by adding logos and verbiage
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 20, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class TrimetWebPdfDesignImpl extends TrimetPdfDesignImpl implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(TrimetWebPdfDesignImpl.class.getCanonicalName());        
    
    private final String m_copy;
    private final String m_lateNote;
    private final String m_pmTimes;
    private final String m_logoFile;
    private final String m_logoPath;
    
    
    public TrimetWebPdfDesignImpl()
    {
        m_copy     = TrimetProperties.COPYRIGHT.get();
        m_lateNote = TrimetProperties.LATE_NOTE.get();
        m_pmTimes  = TrimetProperties.PM_TIMES.get();
        m_logoFile = TrimetProperties.LOGO_FILE.get();
        m_logoPath = TrimetProperties.LOGO_PATH.get();
    }
    
    
    
    /**
     * The purpose of TrimetWebPageEvent is to print the Trimet Header & Footer on each 
     * page of the pdf.   
     * 
     * @author  Frank Purcell (purcellf@trimet.org)
     * @date    Oct 26, 2006
     * @project http://timetablepublisher.org
     * @version Revision: 1.0
     * @since   1.0
     */
    static public class TrimetWebPageEvent extends PdfPageEventHelper 
    {
        private Date   m_date;
        private String m_copyright;
        private Image  m_logo;
        
        public TrimetWebPageEvent(String date, String copy, String logoPath, String logoFile)
        {
            m_copyright = formatNoteUnicodeReplacement(copy);
            m_date      = IntUtils.getDate(date);

            try
            {
                String img = FileUtils.findFilePath(logoPath, logoFile);
                m_logo = Image.getInstance(img);
                m_logo.setAlignment(Image.MIDDLE);                                
                m_logo.scalePercent(24.0f); // default is 72 dpi -- 24% == 300 dpi                
            }
            catch (Exception e)
            {
                LOGGER.log(SEVERE, "TrimetWebPageEvent error", e);
            }
        }
        
        public void onStartPage(PdfWriter writer, Document document) 
        {
            super.onStartPage(writer, document);
            try
            {
                document.add(m_logo);
            }
            catch (DocumentException e)
            {
                LOGGER.log(SEVERE, "TrimetWebPageEvent.onStartPage error", e);
            }            
        }
        
        public void onEndPage(PdfWriter writer, Document document) 
        {
            super.onEndPage(writer, document);
            
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            
            BaseFont helv = null;
            BaseFont helvBold = null;
            try
            {
                helv = BaseFont.createFont(FontFactory.HELVETICA, BaseFont.WINANSI, false);
                helvBold = BaseFont.createFont(FontFactory.HELVETICA_BOLD, BaseFont.WINANSI, false);
            }
            catch (Exception e)
            {
                LOGGER.log(SEVERE, "TrimetWebPageEvent.onEndPage error", e);
            }

            float textBase = document.bottom() - 20;
            cb.beginText();
            cb.setFontAndSize(helv, 6);
            cb.setTextMatrix(document.left(), textBase);
            
            // page number
            cb.showText("Page " + writer.getPageNumber());

            // copyright
            cb.setTextMatrix(document.right() / 2 - (10 + m_copyright.length()), textBase);
            cb.showText(m_copyright);

            // schedule effective date
            SimpleDateFormat d = new SimpleDateFormat(Constants.PRETTY_DATE_FORMAT);
            String date = "Effective: " + d.format(m_date);
            cb.setTextMatrix(document.right() - (50 + date.length()), textBase);
            cb.showText(date);

            cb.endText();
            cb.saveState();
        }        
    }
    
    public PdfPageEventHelper getPageEvent(TimesTable tt)
    {
        return new TrimetWebPageEvent(tt.getDate(), m_copy, m_logoPath, m_logoFile);
    }
    
    public Phrase getTime(Row row, int index)
    {        
        return getTime(row, index, null, null, true); 
    }


    public List<String> getFooterContent()
    {        
        List<String> retVal = new ArrayList<String>();        
        retVal.add(m_pmTimes);
        retVal.add(m_lateNote);        
        return retVal;
    }
}
