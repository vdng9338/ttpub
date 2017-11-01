/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.timetablepublisher.utils.Constants;

/**
 * The purpose of GenericFreemarkerProcessor is to TODO
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Dec 8, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
public class FreemarkerProcessor implements Constants
{
    private static final Logger LOGGER = Logger.getLogger(FreemarkerProcessor.class.getCanonicalName());    
    File f = null;
    OutputStream fs = null;
    FreemarkerBase m_fml = null;

    public FreemarkerProcessor(String outputFileName, String fmTemplateDir, String fmTemplate)
    {
        try
        {
            f   = new File(outputFileName);
            fs  = new FileOutputStream(f);
            m_fml = new FreemarkerBase(fmTemplateDir, fmTemplate, fs);
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, "couldn't process", e);
        }
    }
    
    public void process()
    {
        try
        {
            m_fml.templateProcess();
        }
        catch(Exception e)
        {
            LOGGER.log(SEVERE, "couldn't process", e);
        }
    }
    
    public void close()
    {
        try
        {
            fs.flush();
            fs.close();
            m_fml.close();                
        }
        catch(Exception z)
        {                
        }
        finally
        {
            f = null;
            fs = null;
            m_fml = null;
        }
    }
  
    public void addParams(String name, Object obj)
    {
        m_fml.addParams(name, obj);
    } 
}
