/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.web;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.timetablepublisher.table.TimeTableFactory;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.Params;
import org.timetablepublisher.view.pdf.PdfDesign;
import org.timetablepublisher.view.pdf.TrimetPdfDesignImpl;

/**
 * If you want to avoid that your Servlet times out, 
 * you should use this ProgressServlet.
 * 
 * @author fpurcell -- from code written by iText's blowagie
 */
public class PdfServlet extends HttpServlet implements Constants
{
    private static final long serialVersionUID = -993498522971953L;

    protected String m_fsPath        = null;
    protected String m_templatesDir  = null;
    protected String m_csvMappingDir = null;
    
    public void init(ServletConfig config)
        throws ServletException 
    {
        super.init(config);
        
        // get OS specific path to templates directory in web container
        ServletContext context = config.getServletContext();
        m_fsPath = context.getRealPath("/");
        m_templatesDir  = m_fsPath + "/templates";
        m_csvMappingDir = m_fsPath + "WEB-INF";
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        // super.service();
        doGet(request, response);
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     * @param req
     * @param resp
     * @throws IOException
     * @throws ServletException
     */
    public void doGet (HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        // do the processing to create the PDF
        ByteArrayOutputStream os = singleTable(new Params(req));

        /**
         *  now output that PDF back to the client
         */        
        //setting some response headers
        resp.setHeader("Expires", "0");
        resp.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        resp.setHeader("Pragma", "public");
        
        //setting the content type
        resp.setContentType("application/pdf");
        
        // the contentlength is needed for MSIE!!!        
        resp.setContentLength(os.size());
        
        // write ByteArrayOutputStream to the ServletOutputStream
        ServletOutputStream out = resp.getOutputStream();
        os.writeTo(out);
        out.flush();        
    }    
    
    public ByteArrayOutputStream singleTable(Params params)
    {
        TimesTable tt = TimeTableFactory.create(params);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();            
        PdfDesign pdf = new TrimetPdfDesignImpl();
        pdf.makeDocument(tt, baos);
        return baos;
    }
}