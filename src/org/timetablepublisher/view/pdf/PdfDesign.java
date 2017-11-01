/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.pdf;

import java.awt.Color;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.SplitCharacter;
import com.lowagie.text.pdf.PdfChunk;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Rectangle;

import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.Footnote;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.TimeTableProperties;

/**
 * The purpose of PdfDesign is to be the base interface for PDF TimeTable generation.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    May 10, 2006
 * @project ttpub
 * @version Revision: 1.0
 * @since   1.0
 */
public interface PdfDesign
{
    public enum PdfDesignTypes 
    {
        BasicDesign,
        MultiTableDesign,
        TriMetDesign,
        CompareTablesDesign
        ;
        
        
        // TODO -- this is a factory...but we need a common NO PARAM constructor for each style
        public static PdfDesign makeDesign(PdfDesignTypes design)
        {
            PdfDesign retVal = null;
            
            try
            {
                switch(design)
                {
                case TriMetDesign:
                    break;
                case MultiTableDesign:
                    break;
                case CompareTablesDesign:
                    break;
                default:
                case BasicDesign:
                    break;
                }
            }
            catch(Exception e)
            {
            }
            
            return retVal;
        }
        public static PdfDesign makeDesign(String design)
        {
            if(design == null || design.length() < 1)
                return makeDesign(construct());
            
            return makeDesign(construct(design));
        }
        public PdfDesign makeDesign()
        {
            return makeDesign(this);
        }
        
        public static PdfDesignTypes construct() { return construct(TimeTableProperties.PDF_TEMPLATE.get(BasicDesign.name()));}
        public static PdfDesignTypes construct(String s)
        {
            PdfDesignTypes retVal = null;
            try
            {
                String d = s.toUpperCase().trim();
                for(PdfDesignTypes k : PdfDesignTypes.values())
                {
                    if(d.equals(k.name().toUpperCase()))
                    {
                        retVal = k;
                        break;
                    }
                }
            }
            catch(Exception e)
            {
            }
            
            if(retVal == null)
            {
                retVal = BasicDesign;
            }
            
            return retVal;
        }
    }
    
    public static final Font   HELV_BOLD_8   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  8, Font.NORMAL, Color.BLACK);
    public static final Font   HELV_NORM_8   = FontFactory.getFont(FontFactory.HELVETICA,       8, Font.NORMAL, Color.BLACK);
    public static final Font   HELV_BOLD_6   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  6, Font.NORMAL, Color.BLACK);
    public static final Font   HELV_NORM_6   = FontFactory.getFont(FontFactory.HELVETICA,       6, Font.NORMAL, Color.BLACK);
    public static final Font   HELV_NORM_4   = FontFactory.getFont(FontFactory.HELVETICA,       4, Font.NORMAL, Color.BLACK);
    public static final Font   HELV_WHITE_4  = FontFactory.getFont(FontFactory.HELVETICA_BOLD,  4, Font.NORMAL, Color.WHITE);
    public static final Font   HELV_WHITE_12 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.NORMAL, Color.WHITE);    

    public static final Font   HIGHLIGHT_NORM_6 = FontFactory.getFont(FontFactory.HELVETICA,      6, Font.ITALIC, Color.RED);
    public static final Font   HIGHLIGHT_BOLD_6 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, Font.ITALIC, Color.RED);
    
    public static final Font   COUR_NORM_4   = FontFactory.getFont(FontFactory.COURIER, 4, Font.NORMAL, Color.BLACK);
    public static final Font   COUR_NORM_6   = FontFactory.getFont(FontFactory.COURIER, 6, Font.NORMAL, Color.BLACK);
    public static final Font   COUR_NORM_8   = FontFactory.getFont(FontFactory.COURIER, 8, Font.NORMAL, Color.BLACK);
    public static final Font   COUR_BOLD_4   = FontFactory.getFont(FontFactory.COURIER_BOLD, 4, Font.NORMAL, Color.BLACK);
    public static final Font   COUR_BOLD_6   = FontFactory.getFont(FontFactory.COURIER_BOLD, 6, Font.NORMAL, Color.BLACK);
    public static final Font   COUR_BOLD_8   = FontFactory.getFont(FontFactory.COURIER_BOLD, 8, Font.NORMAL, Color.BLACK);
    public static final Font   COUR_WHITE_4  = FontFactory.getFont(FontFactory.COURIER_BOLD, 4,  Font.NORMAL, Color.WHITE);
    public static final Font   COUR_WHITE_12 = FontFactory.getFont(FontFactory.COURIER_BOLD, 12, Font.NORMAL, Color.WHITE);  

    public static final String COPY          = "\u00A9";
    public static final String BULL          = "\u2022";
    public static final String DASH          = "\u2014";
    public static final String TM            = "\u2122";

    public static final String COPY_HTML     = "&copy;";
    public static final String DASH_HTML     = "&mdash;";
    public static final String BULL_HTML     = "&bull;";
    public static final String TM_HTML       = "<SMALL><SUP>TM</SUP></SMALL>";
    public static final String YEAR_HTML     = "<YEAR/>";
    
    public static final String UC_BOLD       = "<B>";
    public static final String LC_BOLD       = "<b>";
    public static final String REGEX_BOLD    = "<[Bb]>";
    public static final String REGEX_CL_BOLD = "</[Bb]>";
    public static final String LINE_BREAK    = "<BR/>";
    public static final String NEW_LINE      = "\n";
    public static final String FWD_SLASH     = "/";

    
    public void     makeRow(PdfPTable table, Row r, int numCols, boolean doBorder);
    public void     makeFootnotes(PdfPTable table, Collection<Footnote> footnotes)     throws DocumentException;
    public void     makeStops(PdfPTable table, List<Stop> stops, boolean showStopID) throws DocumentException;
    public void     makeTitle(PdfPTable table, String routeName, String keyName, String destName);
    public PdfPCell makeCell(String text, Font font,
                             int vAlignment, int hAlignment, 
                             float leading, float padding, 
                             Rectangle borders, boolean ascender, boolean descender); 
    public PdfPCell makeCell(Paragraph paragraph,
                             int vAlignment, int hAlignment, 
                             float leading, float padding, 
                             Rectangle borders, boolean ascender, boolean descender); 
    
    
    public float              getWidthPercentage(int numCols);
    public void               makeDocument(TimesTable tt, OutputStream output);    
    public PdfPageEventHelper getPageEvent(TimesTable tt);
    public PdfPTable          getDocumentFooter(float width);
        
    public static class SplitChar implements SplitCharacter 
    {
        /** @see http://itextdocs.lowagie.com/tutorial/objects/chunk/ */
        public boolean isSplitCharacter(int start, int current, int end, char[] cc, PdfChunk[] ck) 
        {
            char c;
            if (ck == null)
                c = cc[current];
            else
                c = ck[Math.min(current, ck.length - 1)].getUnicodeEquivalent(cc[current]);
            
            if (c <= ' ' || c == '-' || c == '.' || c == '/') 
            {
                return true;
            }
            if (c < 0x2e80)
                return false;

            return ((c >= 0x2e80 && c < 0xd7a0)
            || (c >= 0xf900 && c < 0xfb00)
            || (c >= 0xfe30 && c < 0xfe50)
            || (c >= 0xff61 && c < 0xffa0));
         }  
        
        /**
         * @see com.lowagie.text.SplitCharacter#isSplitCharacter(int, int, int, char[], com.lowagie.text.pdf.PdfChunk[])
         */
        public boolean isSplitCharacterOLD(int start, int current, int end, char[] cc, PdfChunk[] ck) 
        {
            char c;
            if (ck == null)
                c = cc[current];
            else
                c = ck[Math.min(current, ck.length - 1)].getUnicodeEquivalent(cc[current]);
            
            return (c == '/');
        }
    }    
}
