/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


/**
 * The purpose of FileUtils is to provide a set of static utility methods for opening / finding / traversing files & dirs.
 * 
 * @author  Frank Purcell (purcellf@trimet.org)
 * @date    Oct 27, 2006
 * @project http://timetablepublisher.org
 * @version Revision: 1.0
 * @since   1.0
 */
@SuppressWarnings("unchecked")
public class FileUtils
{
    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getCanonicalName());
    
    static public final String FILE_NAME_METHOD = "getFileName";
    
    public static File findDir(String csvDir, String base)
    {
        String[] prefixList = {base, "", ".", "./", "./src/", "WEB-INF/classes/", "../webapps/ttpub/", "../webapps/ttweb/"};
        return findDir(csvDir, prefixList);
    }
    
    public static File findDir(String csvDir, String[] pList)
    {   
        if(csvDir == null) return null;
        
        String[] prefixList = {"", ".", "./"};
        if(pList  != null) 
            prefixList = pList;
                
        for(String s : prefixList)
        {
            File f = null;
            if(csvDir != null)
            {
                String dir = s + csvDir;
                f = new File(dir);
                if(f != null && f.exists()) 
                {
                    return f;
                }                    
            }            
        }        
        return null;
    }

    
    /**
     * make a directory (if it doesn't exist)
     */
    public static File mkdir(String dirName)
    {
        // zip dir
        File zipDir = new File(dirName);
        zipDir.mkdir();
        return zipDir;
    }

    /**
     * check if an existing file exists with the same name, and rename that old file
     */
    synchronized public static void rename(String dirName, String fileName)
    {
        // rename any existing zip file (eg: don't overwrite)
        File f = new File(dirName + fileName);
        if(f.exists()) 
        {
            String now = Constants.dateTimeSDF.format(new Date());
            
            File bkup = new File(dirName + fileName + "_bkup_" + now + ".zip");
            LOGGER.log(Constants.DEBUG, "File Backup: " + fileName + " to " + bkup.getName()); 
            f.renameTo(bkup);
        }        
    }
    

    /**
     * pad a string -- used to pad file names
     * 
     * @param len
     * @param padding
     * @param inStr
     * @param padRight
     * @return
     */
    public static String pad(int len, String padding, String inStr)
    {
        return pad(len, padding, inStr, false);
    }
    public static String pad(int len, String padding, String inStr, boolean padRight)
    {
        if(len < 0 || len > 100)             return inStr;
        if(padding == null || inStr == null) return inStr;
        if(inStr.length() >= len)            return inStr;
        
        
        String retVal = inStr;
        try
        {
            for(int i = 0; i < len - inStr.length(); i++)
            {
                if(padRight)
                {
                    retVal = retVal + padding;
                    if(retVal.length() > len)
                    {
                        retVal = retVal.substring(0, len);
                    }
                }
                else
                {
                    retVal = padding + retVal;
                    if(retVal.length() > len)
                    {
                        int l =  retVal.length() - len;
                        retVal = retVal.substring(l);
                    }                
                }
                
                if(retVal.length() >= len)
                {
                    break;
                }
            }
        }
        catch (RuntimeException e)
        {
        }
        
        return retVal;
    }
    
    public static String findFilePath(String dir, String fileName)
    {
        String retVal = null;
        File f = findFile(dir, fileName);
        if(f != null) {
            retVal = f.getAbsolutePath();
        }
        return retVal; 
    }
    
    public static File findFile(String dir, String fileName)
    {
        File retVal = null;
        
        File fDir = new File(dir);
        if(fDir.isDirectory())
        {
            List<File> files = findFiles(fDir, fileName);
            if(files != null)
            {
                for(File f : files)
                {
                    if(f == null) continue;
                    retVal = f;
                    break;
                }
            }
        }
            
        return retVal;
    }

    public static List<File> findFiles(String dir, String fileName)
    {
        List<File> retVal = null;
        
        File fDir = new File(dir);
        if(fDir.isDirectory())
        {
            retVal = findFiles(fDir, fileName);            
        }
            
        return retVal;
    }
    
    public static List<File> findFiles(File input, String fileName)
    {        
        List<File> retVal = new ArrayList<File>();
    
        File[] files = input.listFiles();
        if(files == null) return null;
        
        for(File f : files)
        {
            if(f == null) continue;
            
            if(f.isDirectory()) 
            {             
                // recurse file system, adding any hits to the return list
                List<File> ret = findFiles(f, fileName);
                if(ret != null) {
                    retVal.addAll(ret);
                }
            }
            else
            {
                // add to retVal
                if(f.getName().equals(fileName)) {
                    retVal.add(f);
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * 
     * @param file
     * @param c
     * @return
     */
    public static String getFilePath(File file, Class c)
    {
        String dir  = ".";
        if(file != null) 
        {
            try
            {
                File tmp = file.getParentFile();
                if(tmp != null) {
                    dir = tmp.getCanonicalPath();
                }
            }
            catch(Exception e) {}
        }
        
        if(!dir.endsWith("/"))
            dir += "/";
        
        return FileUtils.getFilePath(dir, c);
    }
    
    
    /**
     * 
     * @param dir
     * @param c
     * @return
     */
    public static String getFilePath(final String dir, Class c)
    {
        String file;
        String simpleName = "";        
    
        try
        {
            // try to get simple name from class
            simpleName = c.getSimpleName();
            
            // try to call the getFileName method defined in the CSV POJOs via Reflection
            file = dir + c.getMethod(FILE_NAME_METHOD, (Class[])null)
                                .invoke(c.newInstance(), (Object[])null);
        }
        catch(Exception e)
        {
            file = dir + simpleName + ".csv";
        }
        
        return file;
    }

}
