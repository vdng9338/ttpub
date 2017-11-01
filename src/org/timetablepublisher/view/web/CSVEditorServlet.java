/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Template;
import freemarker.template.TemplateModel;

import org.timetablepublisher.configure.Configure;
import org.timetablepublisher.configure.TimePoints;
import org.timetablepublisher.configure.loader.ConfigurationLoader;
import org.timetablepublisher.configure.loader.ConfigurationLoaderImpl;
import org.timetablepublisher.configure.loader.CsvColumn;
import org.timetablepublisher.table.TimeTableFactory;
import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.Params;


/**
 * The purpose of CSVEditorServlet is to provide an editing interface into the in-memory 
 * TimeTable Configuration.  
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 24, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 * @see     
 */
@SuppressWarnings("unchecked")
public class CSVEditorServlet extends FreemarkerBaseServlet
{    
    private static final long serialVersionUID = -5111116288522111153L;
    private static final Logger LOGGER = Logger.getLogger(CSVEditorServlet.class.getCanonicalName());
    
    protected boolean preTemplateProcess(
                          HttpServletRequest  req, 
                          HttpServletResponse resp,
                          Template            template,
                          TemplateModel       data,
                          Params              params,
                          Params              oldParams                          
        )
        throws ServletException, IOException     
    {   
        String csvFileName  = req.getParameter(EDIT_CSV_FILE);
        String csvRowID     = req.getParameter(EDIT_ID);      
        String csvShowRoute = req.getParameter(CSV_EDIT_ROUTE);      
        String csvEditsMade = req.getParameter(EDITS_MADE);      

        // some variable setup -- note that setParams(req, m_tt) will update FTL params when route direction changes, etc...
        TimesTable tt = TimeTableFactory.create(params);
        setParams(req, tt);
        ConfigurationLoader loader = tt.getConfiguration();
        if(loader == null)
        {
            LOGGER.log(SEVERE, "Configuration Loader is NULL -- NOT GOOD...going to try to do something");
            loader = new ConfigurationLoaderImpl("."); 
            tt.setLoader(loader);
        }
        
        // process edits / deletes to a single config
        if(csvEditsMade != null)
        {
            Configure rdata = loader.findData(csvFileName, csvRowID);
            if(rdata != null)
            {
                boolean updated = false;
                
                if(csvEditsMade.equals(DELETE)) {
                    loader.deleteConfig(rdata);
                    updated = true;
                }
                else {
                    updated = rdata.setFieldValues(req, "cell_");
                }
                
                if(updated) {
                    loader.setDirty(rdata.getClass());
                }
                
                // sort timepoints, just in case a change was made to sequence
                TimePoints.sort(tt);
            }
        }
        
        // get config data ready for freemarker rendering
        if(csvFileName != null)
        {            
            try
            {
                //
                // for showing a row in the little edit pop-up
                //
                if(csvRowID != null)
                {
                    if(csvRowID.equals("NEW"))
                    {
                        Configure config = new Configure(tt);
                        config.setLang(""); config.setKey("*");
                        Integer hash = loader.newConfig(csvFileName, config); 
                        if(hash != null)
                        {
                            csvRowID = hash.toString();                            
                        }
                    }

                    Configure rdata = loader.findData(csvFileName, csvRowID);
                    if(rdata != null) 
                    {
                        req.setAttribute(CSV_FIELD_VALUES, rdata.getFieldValues());
                        req.setAttribute(EDIT_ID,    csvRowID);
                        req.setAttribute(EDIT_CSV_FILE, csvFileName);
                    }
                }
                
                //
                // for showing the entire table - both field names (columns) and values (rows of cells)
                //
                List<Configure> cdata  = loader.getData(csvFileName);
                List<CsvColumn> fields = Configure.getFields(csvFileName);
                if(cdata != null && cdata.size() > 0)
                {            
                    req.setAttribute(CSV_DATA, cdata);
                    if(fields == null) {
                        fields = cdata.get(0).getFields();
                    }
                }
                req.setAttribute(CSV_COL_NAMES, fields);
            } 
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if(csvShowRoute != null)
        {
            //
            //  the values set here are built for editing all configuration
            //
            
            // creating a m_tt sets up some very much needed configuration data
            if(tt != null)
            {
                Configure findMe = new Configure(tt);
                
                for(Class csv : ConfigurationLoaderImpl.CSV)
                {
                    if(csv == null) continue;
                    
                    List<Configure> allCSV = loader.getData(csv);
                    if(allCSV == null) continue;
                    
                    List<Configure> foundCons = new ArrayList<Configure>();
                    for(Configure con : allCSV)
                    {
                        if(con.equals(findMe)) {
                            foundCons.add(con);
                        }                    
                    }
                    
                    req.setAttribute(csv.getSimpleName() + CSV_DATA,      foundCons);
                    req.setAttribute(csv.getSimpleName() + CSV_COL_NAMES, Configure.getFields(csv));
                }
            }
        }
        
        req.setAttribute(CSV_LIST,      loader.getCsvNames());
        req.setAttribute(EDIT_CSV_FILE, csvFileName);
              
        return true;
    }
}
