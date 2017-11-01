/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.view;

import java.util.Date;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

import java.io.OutputStreamWriter;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;


public class ZipTimeTables extends FreemarkerBase
{
    protected ZipOutputStream m_zip = null;
    
    public ZipTimeTables(String templateDir, String templateFile, OutputStream zipStream)
        throws Exception
    {
        super(templateDir, templateFile, zipStream);
        
        if(! (zipStream instanceof PrintStream))
        {
            m_zip = new ZipOutputStream(zipStream);
            m_writer = new OutputStreamWriter(m_zip);
        }
    }
        
    static int x=1;
    public void addFileToZip(String name) throws Exception
    {
        String fileName = name;
        if(name == null) 
        {
            fileName = "file" + x++ + ".txt";
        }
        
        m_zip.putNextEntry(new ZipEntry(fileName));            
    }

    public void addFileToZip(String name, String text) throws Exception
    {
        addFileToZip(name);
        m_writer.write(text);
    }

    
    public ZipOutputStream getZip()
    {
        return m_zip;
    }

    public void close()
    {
        super.close();
        try { m_zip.close(); } catch(Exception e) {}
    }
        
    public static String fileDetails(File f)
    {
        String read  = f.canRead()  ? "r" : "-";
        String write = f.canWrite() ? "w" : "-";
        return f.getName() + " " + read + write + " size: " + f.length() + "   last updated: " + new Date(f.lastModified());
    }

    public void closeEntry()
    {
        try 
        { 
            m_zip.closeEntry();
        } 
        catch(Exception e) 
        {}   
    }

}


