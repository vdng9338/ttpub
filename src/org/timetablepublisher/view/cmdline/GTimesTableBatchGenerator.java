/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.cmdline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.timetablepublisher.schedule.gtfs.Agency;
import org.timetablepublisher.schedule.gtfs.Routes;
import org.timetablepublisher.schedule.gtfs.loader.GTFSDataLoader;
import org.timetablepublisher.table.TableUtils;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.table.TimeTableFactory.TableType;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;
import org.timetablepublisher.utils.Constants;
import org.timetablepublisher.utils.FileUtils;
import org.timetablepublisher.view.FreemarkerBase;
import org.timetablepublisher.view.ZipTimeTables;
import org.timetablepublisher.view.pdf.TrimetPdfDesignImpl;

/**
 * The purpose of GTimesTableBatchGenerator is to provide a GTimesTable specific generator,
 * such that ALL available GTimesTable data (from N different agencies) is ripped into TimesTables 
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 27, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class GTimesTableBatchGenerator extends TTCmdLineBase
{
    private static final Logger LOGGER = Logger.getLogger(GTimesTableBatchGenerator.class.getCanonicalName());
    public static final GTFSDataLoader loader = GTFSDataLoader.loader();

    /** here's an example of custom (to this class) */
    @Option(name="-kc", usage="King County Metro")
    public void kc(boolean ignore)
    {
        m_fmHtmlPath = "webPageTemplates/metroTable.web";
        m_fmMapPath  = "webPageTemplates/metroGMap.web";
    }    
    
    public GTimesTableBatchGenerator()
    {
        m_tableType = TableType.GTFS;
    }
    
    synchronized public void oneOff()
    {
        TimesTable tt = makeTable();
        TableUtils.print(tt);
        FreemarkerBase.process(tt, m_outHtml, m_fmTemplateDir, m_fmHtmlPath);
        if(!m_genMaps) {
            FreemarkerBase.process(tt, m_outMap,  m_fmTemplateDir, m_fmMapPath);
        }
    }

    synchronized public void setTemplates(ZipTimeTables po) throws Exception
    {
        m_htmlTemplate    = po.getTemplate(m_fmHtmlPath);
        m_mapTemplate     = po.getTemplate(m_fmMapPath);
        m_webTestTemplate = po.getTemplate(m_fmWebTestPath);
        m_appTestTemplate = po.getTemplate(m_fmAppTestPath);
        m_pdfTemplate     = new TrimetPdfDesignImpl(); 
    }
    
    public int process(String agency, Routes rd, ZipTimeTables po)
        throws Exception
    {
        setTemplates(po);
        
        // TODO: we should be using List<RouteDescription> getRouteNames()
        //       so that we get *all* the routes -- including Combo Routes !!!
        
        //
        // PROCESS:
        //    1. Get TimeTable Data
        //    2. Tell the ZIP file about the timetable file your about to add
        //    3. Format the timetable data according to the freemarker template
        //       NOTE: we stream the Freemarker template result to the ZIP stream
        //    4. Tell the ZIP file to close this timetable
        //

        // step 1: get the time-table from our data store
        m_agency = agency;
        m_route  = rd.getRouteID();
/*
        m_dir    = rd.getDirType();
        m_key    = rd.getKeyType();
*/
        // TODO ...
        m_dir = DirType.Inbound;
        m_key = KeyType.Weekday;
        
        TimesTable tt = makeTable();
        if(tt == null) return m_actualNumOfHtmlFiles; 
        
        if(LOGGER.isLoggable(Constants.DEBUG)) LOGGER.log(Constants.DEBUG, tt.getRouteDescription());
        po.addParams(Constants.TIMES_TABLE, tt);

        // step 2: tell zip about the file you're about to stream into the ZIP file
        String filePath = agency + "/" + tt.getDir().toString() + "/";
        String testPath = filePath + "tests/";
        String fileName = "s" + FileUtils.pad(3, "0", tt.getRouteShortName()) + "_" + tt.getKey().ivalue() + "_";
        String htmFile  = fileName + ".htm";
        String mapFile  = fileName + "map.htm";
        String pdfFile  = fileName + ".pdf";        

        // step 2a: tell freemarker about the file names, so it can build links to these files
        po.addParams(Constants.PDF_FILE, pdfFile);
        po.addParams(Constants.MAP_FILE, mapFile);
        po.addParams(Constants.HTM_FILE, htmFile);
        
        // step 3: have freemarker stream the html via the template engine
        // step 4: have freemarker stream the mapified html via the template engine
        // step 5a: tests
        // step 6: PDF generation
        makeHtml(tt, po, filePath + htmFile,         m_htmlTemplate);
        if(!m_genMaps) {
            makeHtml(tt, po, filePath + mapFile,     m_mapTemplate);
        }
        makeHtml(tt, po, testPath + "web" + htmFile, m_webTestTemplate);
        makeHtml(tt, po, testPath + "app" + htmFile, m_appTestTemplate);
        makePdf(tt, po, filePath + pdfFile,      m_pdfTemplate);
        
        return m_actualNumOfHtmlFiles;
    }
    
    synchronized public void zip()
    {
        String today = Constants.dateSDF.format(new Date());
        ZipTimeTables po = null;

        try 
        {                        
            FileUtils.mkdir(m_zipDirName);
            FileUtils.rename(m_zipDirName, m_zipFile);

            LOGGER.log(Constants.DEBUG, "create zip file: " + m_zipDirName + m_zipFile); 
            File f  = new File(m_zipDirName + m_zipFile);
            OutputStream fs = new FileOutputStream(f);
            po = new ZipTimeTables(m_fmTemplateDir, m_fmHtmlPath, fs);
            po.addParams(Constants.PREVIEW, m_preview);
            
            LOGGER.log(Constants.DEBUG, "set zip comment");
            String comment = "Created on: "        + today + "\n\n";
                   comment += "PARAMETERS: \n";
                   comment += "  - Service Date: " + m_svcDate          + "\n";
                   comment += "  - Routes: "       + "all routes"     + "\n";
                   comment += "  - Directions: "   + "Both Inbound / Outbound" + "\n";
                   comment += "  - Service Keys: " + "W, S, U"        + "\n";
            po.getZip().setComment(comment);
                        
            LOGGER.log(Constants.DEBUG, "starting to loop through routes");
            for(Agency a : loader.getAgencies())
            {
                System.out.println("Agency: " + a.getName());
                try
                {
                    List rList = loader.getData(a, Routes.class);
                    int i = 1;
                    
                    for(Routes rd :(List<Routes>)rList)
                    {
                        System.out.println("\t route " + rd.getRouteID() +  " -- processing " + i + " of " + rList.size());
                        process(a.getName(), rd,  po);
                        i++;
                    }
                } 
                catch (RuntimeException e)
                {
                    LOGGER.log(Constants.DEBUG, "Couldn't create TT data for agency " + a.getName());
                }
            }
        }
        catch(Exception e)
        {
            LOGGER.log(Constants.SEVERE, "VERY VERY BAD: TimeTable Batch Process Ended PreMaturely: " + e.toString(), e);
        }
        finally
        {
            // Complete the ZIP file
            LOGGER.log(Constants.DEBUG, "closing the zip file");
            if(po != null) po.close();
        }
        
        LOGGER.info("\n\n\n>>>>>>> File Report:\n\tEstimated = " + m_estimatedNumOfFiles  + 
                    " HTML and PDFs.\n\tActual number of HTML: " + m_actualNumOfHtmlFiles +
                    "\n\tActual number of PDFs: "                + m_actualNumOfPdfFiles);        
    }
    
    public void run()
    {          
        if(m_zipFile != null)
        {
            zip();
        }
        else
        {
            oneOff();
        }
    }
    
    
    public static void main(String[] args) 
    {
        GTimesTableBatchGenerator bean = new GTimesTableBatchGenerator();
        CmdLineParser parser = new CmdLineParser(bean);
        try 
        {
            parser.parseArgument(args);
            bean.run();
        } 
        catch (CmdLineException e) 
        {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }
    }
}
