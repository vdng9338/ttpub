/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.timetablepublisher.table.Cell;
import org.timetablepublisher.table.Row;
import org.timetablepublisher.table.Stop;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Constants;


/**
 * The purpose of TimeTableReport is to collect 
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    May 8, 2007
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class TimeTableReport implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(TimeTableReport.class.getCanonicalName());
    
    public enum Severity {Warning, Error, Note};
    
    protected final StringBuffer m_sb     = new StringBuffer();        
    protected final DelayDraw    m_draw   = new DelayDraw();
    protected int                m_numColumns = 3;
    protected boolean            m_numericStopIDs = false;
    
    public class DelayDraw
    {
        public StringBuffer header = new StringBuffer();
        public StringBuffer buff   = new StringBuffer(); 
        public TimesTable   m_tt;
        public String       m_url;
        
        public void open(TimesTable ntt, String nurl)
        {
            m_tt     = ntt;
            m_url    = nurl;
            LOGGER.log(DEBUG, "open table: " + m_url);
        }
        
        public void close()
        {
            m_sb.append(header);
            m_sb.append(buff);

            m_tt   = null;
            m_url  = null;
            header = new StringBuffer();               
            buff   = new StringBuffer(); 
            LOGGER.log(DEBUG, "close table: " + m_url);
        }

        public boolean isNewEntry(TimesTable tt)
        {
            if(tt != null && tt != m_tt)
                return true;

            return false;
        }

        public StringBuffer getNewHeader()
        {
            header = new StringBuffer();
            return header;
        }
    }
    
    public TimeTableReport()
    {
        String now = PRETTY_DT.format(new Date());
        m_sb.append("<HTML><HEAD><title>TimeTableReport: ").append(now).append("</title>")
            .append("\n<SCRIPT LANGUAGE=\"JavaScript\">function showAll(sw){ var table = document.getElementById('table'); var cells = table.getElementsByTagName(\"tr\"); for (var i = 0; i < cells.length; i++) {cells[i].style.display = '';}}</SCRIPT>\n")
            .append("</HEAD><BODY>")
            .append("<H2>Report Generated: ").append(now).append("</H2><br/>")
            .append("<A HREF=\"#\" onClick=\"showAll(); return false;\">show all</A><br/>")
            .append("<TABLE id=\"table\" cellpadding=\"1\" cellspacing=\"1\" border=\"1\">");
        
        LOGGER.log(DEBUG, "TimeTableReport constructor");
    }

    public void close()
    {
        m_draw.close();
        m_sb.append("</TABLE></body></html>");
    }
    
    public boolean isNumericStopIDs()
    {
        return m_numericStopIDs;
    }

    public void setNumericStopIDs(boolean numericStopIDs)
    {
        m_numericStopIDs = numericStopIDs;
    }
    
    public void inspectTable(TimesTable tt, String url)
    {
        if(tt == null) return;
        
        // check table is OK
        List<Stop> col = tt.getTimePoints();
        List<Row>  row = tt.getTimeTable();
        if(col == null || col.size() <= 1)
        {
            if(col.size() == 1)
                warning(tt, url, "one stop", "the table only has one timepoint");
            else
                error(tt, url, "no stops", "the table doesn't have any timepoints");
        }
        if(row == null || row.size() < 1)
        {
            error(tt, url, "no rows", "the table doesn't have any rows (eg: trips)");            
        }
         
        // check for blank columns
        if(col != null && row != null) 
        for(int i = 0; i < col.size(); i++)
        {
            Stop s = col.get(i);
            if(s == null)
            {
                error(tt, url, "null column", "column #" + i + " of the table is null");            
                continue;
            }
            
            String stopID = s.getStopId();  
            String desc   = s.getDescription();
            if(stopID == null || stopID.length() < 1)
            {
                error(tt, url, "no stop id", "column #" + i + " of the table has a NULL stop id");
                stopID = "col #" + i;
            }
            else if(isNumericStopIDs() && !stopID.toLowerCase().equals(stopID.toUpperCase()))
            {
                error(tt, url, "alpha-numeric stop id", stopID + " looks to have <B>non-numeric</B> characters");
            }
            if(desc == null || desc.length() < 1 || desc.equals(stopID))
            {
                warning(tt, url, "no stop desc.", stopID + " looks to have a NULL stop description");            
            }
            
            if(!s.isPublic())
            {                
                majorError(tt, url, "Non-Public Stop ID", stopID + " appears to be a stop that is NOT PUBLIC...<B>very bad</B>!");            
            }
            
            boolean hasTimes = false;
            for(Row r : row)
            {
                Cell c = r.getCell(i);
                if(c != null && c.getTime() != null && c.getTimeAsStr() != null)
                {
                    hasTimes = true;
                    break;
                }
            }
            
            if(!hasTimes)
            {                
                majorError(tt, url, "NO STOP TIMES!", stopID + " doesn't have any stop times...<B>very bad</B>!");            
            }
        }
    }
    public void inspectTable(TimesTable tt)
    {
        inspectTable(tt, null);
        LOGGER.log(DEBUG, "inspecting table");
    }
    

    public void newEntry(StringBuffer sb, String route, String routeDesc, DirType tdir, KeyType tkey, String date, String url, String trStyle)
    {   
        if(trStyle == null || trStyle.length() < 1)
        {
            trStyle = "display:none";
        }
        
        String dir = tdir != null ? tdir.name() : "unspecified"; 
        String key = tkey != null ? tkey.name() : "unspecified"; 
        
        String link = "";
        String turl = url != null ? url : m_draw.m_url;
        if(turl != null)
            link = "  <a href=" + turl + ">" + route + " link</a>";
        
        sb.append("<tr style=\"").append(trStyle).append("\">")
          .append("<td>").append(link).append("</td>")
          .append("<td colspan=\"2\">")
          .append(routeDesc).append(" / ").append(dir).append(" / ").append(key).append(" &mdash; ").append(date)
          .append("</td>")
          .append("</tr>");
    }
    
    public void newEntry(String route, String routeDesc, DirType tdir, KeyType tkey, String date, String url)
    {
        m_draw.close();
        newEntry(m_sb, route, routeDesc, tdir, tkey, date, url, null);
    }
    
    public void newEntry(TimesTable tt, String url, String trStyle, boolean forceHeader)
    {
        boolean isNew = m_draw.isNewEntry(tt);
        if(forceHeader || isNew)
        {
            if(isNew)
            {
                m_draw.close();
                m_draw.open(tt, url);
            }
            
            newEntry(m_draw.getNewHeader(), tt.getRouteID(), tt.getRouteDescription(), tt.getDir(), tt.getKey(), tt.getDate(), url, trStyle);            
        }
    }

    public void newEntry(TimesTable tt, String url)
    {
        newEntry(tt, url, "display:none", false);
    }
    
    
    public void addComment(TimesTable tt, String url, Severity s, String problem, String description, String trStyle, String tdStyle)
    {
        String sevStyle = ""; 
        if(s == Severity.Error)
        {
            sevStyle = "color:red; font-style:italic; font-weight:bold;";
            newEntry(tt, url, sevStyle, true);
        }
        else
        {
            trStyle = "display:none";
            newEntry(tt, url);
        }
        
        
        m_draw.buff.append("<tr style=\"").append(trStyle).append("\">")
            .append("<td style=\"").append(tdStyle).append(sevStyle).append("\">").append(s.name()).append("</td>")
            .append("<td style=\"").append(tdStyle).append("\">").append(problem     ).append("</td>")
            .append("<td style=\"").append(tdStyle).append("\">").append(description ).append("</td>")
            .append("</tr>");
    }
    public void addComment(TimesTable tt, String url, Severity s, String problem, String description)
    {
        addComment(tt, url, s, problem, description, "", "");
        LOGGER.log(DEBUG, "report serverity:" + s + "; problem:" + problem + "; description:" + description);
    }
    public void majorError(TimesTable tt, String url, String problem, String description)
    {
        addComment(tt, url, Severity.Error, problem, description);
    }
    public void error(TimesTable tt, String url, String problem, String description)
    {
        addComment(tt, url, Severity.Error, problem, description);
    }
    public void note(TimesTable tt, String url, String problem, String description)
    {
        addComment(tt, url, Severity.Note, problem, description);
    }
    public void note(String problem, String description)
    {
        addComment(null, null, Severity.Note, problem, description);
    }
    public void warning(TimesTable tt, String url, String problem, String description)
    {
        addComment(tt, url, Severity.Warning, problem, description);
    }


    public String getReport()
    {
        LOGGER.log(DEBUG, "getReport() called");
        return m_sb.toString();
    }
}
