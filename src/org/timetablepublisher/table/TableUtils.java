/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.table;


import java.util.List;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.OutputStream;


public class TableUtils
{    
    public static void toXml(Object obj, OutputStream os)
    {
        XMLEncoder e = new XMLEncoder(new BufferedOutputStream(os));
        e.writeObject(obj);
        e.close();
    }
    public static Cell[] toArray(Row r)
    {
        List<Cell> c = r.getRow();
        Cell[] retVal = new Cell[c.size()];
        c.toArray(retVal);
        return retVal;        
    }    
    
   
    public static void printWithNextPrevCell(TimesTable tab)
    {
        List<Row> rows = tab.getTimeTable();

        System.out.print("\t\t");     
        for(Stop tp : tab.getTimePoints())
        {                
            if(tp == null)
            {
                System.out.print("\t\t\t\t");     
                continue;
            }
            
            String desc = tp.getDescription();
            System.out.print(desc.substring(0, desc.length() < 14 ? desc.length() : 14) + "\t\t\t");
        }
        System.out.println();
        
        if(rows != null)        
        for(int i = 0; i < rows.size(); i++)
        {
            if(rows.get(i) == null) continue;
            
            boolean printedTrip = false;
            String  tabs = "";
            for(int j = 0; j < rows.get(i).getLen(); j++)
            {                
                Cell c = rows.get(i).getCell(j);
                if(c == null)
                {
                    if(!printedTrip) tabs += "\t\t\t\t";
                    else             System.out.print("\t\t\t\t");     
                    continue;
                }
                    
                if(!printedTrip)
                {
                    System.out.print(c.getTrip() + ":\t\t" + tabs);  
                    printedTrip = true;
                }
                Cell n = RowImpl.getNextCell(rows, i, j);
                Cell p = RowImpl.getPrevCell(rows, i, j);
                
                System.out.print(rows.get(i).getTimeAsStr(j) + " ");
                System.out.print("(" + (p != null ? p.getTimeAsStr() : "no prev")
                                     + " "
                                     + (n != null ? n.getTimeAsStr() : "no next")
                                     + ")\t\t"); 
            }
            System.out.println();
        }        
    }

    public static void printPrevRowTime(TimesTable tab)
    {
        List<Row> rows = tab.getTimeTable();

        System.out.print("\n\t\t");     
        for(Stop tp : tab.getTimePoints())
        {                
            if(tp == null)
            {
                System.out.print("\t\t\t\t");     
                continue;
            }
            
            String desc = tp.getDescription();
            System.out.print(tp.getStopId() + " ");
            System.out.print(desc.substring(0, desc.length() < 10 ? desc.length() : 10) + "\t\t\t");
        }
        System.out.println();
        
        if(rows != null)        
        for(int i = 0; i < rows.size(); i++)
        {
            if(rows.get(i) == null) continue;
            
            boolean printedTrip = false;
            String  tabs = "";
            for(int j = 0; j < rows.get(i).getLen(); j++)
            {                
                Cell c = rows.get(i).getCell(j);
                if(c == null)
                {
                    if(!printedTrip) tabs += "\t\t\t\t";
                    else             System.out.print("\t\t\t\t");     
                    continue;
                }
                    
                if(!printedTrip)
                {
                    System.out.print(c.getTrip() + ":\t\t" + tabs);  
                    printedTrip = true;
                }
                Cell n = null;
                Cell p = null;
                // prev row
                if(i > 0)
                {
                    n = RowImpl.getNextCell(rows.get(i-1), j+1);                
                    p = RowImpl.getPrevCell(rows.get(i-1), j-1);
                }
                // next row
                if(i < rows.size()-1)
                {                    
                    n = RowImpl.getNextCell(rows.get(i+1), j+1);                
                    p = RowImpl.getPrevCell(rows.get(i+1), j-1);
                }
                
                System.out.print(rows.get(i).getTimeAsStr(j) + " ");
                System.out.print("(" + (p != null ? p.getTimeAsStr() : "no prev")
                                     + " "
                                     + (n != null ? n.getTimeAsStr() : "no next")
                                     + ")\t\t"); 
            }
            System.out.println();
        }        
    }

    public static void print(TimesTable tab)
    {
        List<Row> rows = tab.getTimeTable();

        System.out.print("\n\n\t\t");     
        for(Stop tp : tab.getTimePoints())
        {                
            if(tp == null)
            {
                System.out.print("\t\t\t\t");     
                continue;
            }
            
            System.out.print(tp.getStopId() + " ");
            
            String desc = tp.getDescription();
            if(desc != null)
                System.out.print(desc.substring(0, desc.length() < 5 ? desc.length() : 5) + "\t");
        }
        System.out.println();
        
        if(rows != null)        
        for(int i = 0; i < rows.size(); i++)
        {
            if(rows.get(i) == null) continue;
            
            boolean printedTrip = false;
            String  tabs = "";
            for(int j = 0; j < rows.get(i).getLen(); j++)
            {                
                Cell c = rows.get(i).getCell(j);
                if(c == null)
                {
                    if(!printedTrip) tabs += "\t\t  ";
                    else             System.out.print("\t\t");     
                    continue;
                }
                    
                if(!printedTrip)
                {
                    System.out.print(c.getTrip() + ":\t\t" + tabs);  
                    printedTrip = true;
                }
                
                System.out.print(rows.get(i).getTimeAsStr(j) + "\t\t");
            }
            System.out.println();
        }
        System.out.println();
        
        for(Footnote fn : tab.getFootnotes())
        {                
            if(fn == null) continue;            
            System.out.println(fn.getSymbol() + ":  " + fn.getFormattedNote());
        }        
    }
}
