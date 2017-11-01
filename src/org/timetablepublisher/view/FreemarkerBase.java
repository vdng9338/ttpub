/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.timetablepublisher.table.TimesTable;
import org.timetablepublisher.utils.Constants;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 
 * 
 * @author Frank Purcell
 */
public class FreemarkerBase implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(FreemarkerBase.class.getCanonicalName());    
    
    final protected Map<String, Object> m_params;    
    final protected Configuration       m_config;
    final protected Template            m_template;
    protected       Writer              m_writer;
    
    public FreemarkerBase(String templateDir, String templateFile, OutputStream out)
        throws Exception
    {
        m_writer = new OutputStreamWriter(out);

        // setup Freemarker
        m_config = new Configuration();        
        m_params = new HashMap<String, Object>();
        addParams("now", new Date());
        addParams(PARAMETERS,  new ParameterMap().getMap());        
        initializeContext(templateDir);        
        m_template = getTemplate(templateFile);
    }
    
    public void process(TimesTable tt)
    {
        if(tt != null)
        {
            LOGGER.log(DEBUG, tt.getRouteDescription());
        }
        
        try
        {
            if(tt != null)
            {
                addParams(TIMES_TABLE, tt);
                addParams(ROUTE_LIST,  tt.getRouteNames());
            }
            templateProcess();
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, "couldn't process", e);
        }
    }
    
    public static void process(TimesTable tt, String outFileName, String fmTemplateDir, String fmHtmlTemplate)
    {
        File f = null;
        OutputStream fs = null;
        FreemarkerBase fml = null;
        try
        {
            f   = new File(outFileName);
            fs  = new FileOutputStream(f);
            fml = new FreemarkerBase(fmTemplateDir, fmHtmlTemplate, fs);
            if(tt != null) {
                fml.process(tt);
            } else {
                fml.templateProcess();
            }
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, "couldn't process", e);
        }
        finally
        {
            try
            {
                fs.flush();
                fs.close();
                fml.close();                
            }
            catch(Exception z)
            {                
            }
            finally
            {
                f = null;
                fs = null;
            }
        }
        
    }
    
    protected void initializeContext(String dir)
      throws Exception
    {        
        // template dir 
        m_config.setDirectoryForTemplateLoading(new File(dir));
        
        // use [# ] syntax (or the old psudo-xml)
        m_config.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);

        // allows freemarker to call any java bean methods
        BeansWrapper wrapper = new BeansWrapper();
        wrapper.setExposureLevel(BeansWrapper.EXPOSE_ALL);

        // TODO: might want to use EXPOSE_PROPERTIES_ONLY 
        m_config.setObjectWrapper(wrapper);
    }

  
    public void addParams(String name, Object obj)
    {
        m_params.put(name, obj);
    }

    public Template getTemplate(String templateFile)
        throws Exception
    {
        if(templateFile == null) templateFile = "indesign.bcid";
        return m_config.getTemplate(templateFile); 
    }   
    
    public void templateProcess()
      throws Exception
    {
        m_template.process(m_params, m_writer);    
    }

    public void templateProcess(Template t)
      throws Exception
    {
        t.process(m_params, m_writer);    
    }
    
    public void close()
    {
        try { m_writer.close(); } catch(Exception e) {}
    }    
}
