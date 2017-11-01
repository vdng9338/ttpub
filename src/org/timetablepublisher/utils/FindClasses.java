/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FindClasses
{
    /**
     * list Classes inside a given package
     * @author Jon Peck http://jonpeck.com (adapted from http://www.javaworld.com/javaworld/javatips/jw-javatip113.html)
     * @param pckgname String name of a Package, EG "java.lang"
     * @return Class[] classes inside the root of the given package
     * @throws ClassNotFoundException if the Package is invalid
     * 
     *  FRANK NOTE: This doesn't work on Windows
     */
    public static List<Class> find(String packageName)
        throws ClassNotFoundException 
    {
        List<Class> classes = new ArrayList<Class>();
        
        // Get a File object for the package
        File directory = null;        
        try 
        {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            String path = '/' + packageName.replace('.', '/');
            URL resource = cld.getResource(path);
            if (resource == null) {
                throw new ClassNotFoundException("No resource for " + path);
            }
            directory = new File(resource.getFile());
        }
        catch (NullPointerException x) 
        {
            throw new ClassNotFoundException(packageName + " (" + directory + ") does not appear to be a valid package");
        }

        if (directory.exists()) 
        {
            // Get the list of the files contained in the package
            String[] files = directory.list();
            for (int i = 0; i < files.length; i++) 
            {
                // we are only interested in .class files
                if (files[i].endsWith(".class")) 
                {
                    // removes the .class extension
                    classes.add(Class.forName(packageName + '.' + files[i].substring(0, files[i].length() - 6)));                            
                }
            }
        } 
        else 
        {
            throw new ClassNotFoundException(packageName + " does not appear to be a valid package");
        }

        return classes;
    }
}
