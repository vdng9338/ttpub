/**
 *  Copyright 2007 TriMet, released under a modified Mozilla 1.1 license.
 *  Please see http://timetablepublisher.org/downloads/list
 *  (TriMet OSS License.htm) for the full text of the license.
 */

package org.timetablepublisher.utils;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.timetablepublisher.table.TimeTableFactory.TableType;
import org.timetablepublisher.table.TimesTable.DirType;
import org.timetablepublisher.table.TimesTable.KeyType;

import org.timetablepublisher.utils.TimeTableProperties;

public class Params implements Constants
{
    // attributes with default values
    protected String  m_agency        = TimeTableProperties.DEFAULT_AGENCY.get("");
    protected String  m_routeID       = TimeTableProperties.DEFAULT_ROUTE.get("4");
    protected DirType m_dir           = DirType.Inbound;
    protected KeyType m_key           = KeyType.Weekday;
    protected String  m_date          = null;
    
    protected Locale  m_locale        = Locale.ENGLISH;
    protected String  m_diffDate      = null;
    protected boolean m_update        = false;
    protected TableType m_tableType   = TableType.getDefault();
    
    protected boolean m_saveConfig    = false;
    protected boolean m_bypassConfig  = false;
    protected boolean m_reloadConfig  = false;
    protected boolean m_revertTimePointConfig = false;
    
    protected boolean m_useRoute      = false;
    protected boolean m_useKey        = false;
    protected boolean m_useDir        = false;
    protected boolean m_justTests     = false;

    protected String m_configureDataDir;
    protected String m_scheduleDataDir;
    
    
    public Params(String routeStr, String dirStr, String keyStr, String dateStr)
    {
        init(null, routeStr, dirStr, keyStr, dateStr, null);
    }
    public Params(String agencyStr, String routeStr, String dirStr, String keyStr, String dateStr)
    {
        init(agencyStr, routeStr, dirStr, keyStr, dateStr, null);
    }
    public Params(HttpServletRequest req)
    {
        String agencyStr = req.getParameter(AGENCY);
        String routeStr  = req.getParameter(ROUTE);
        String dirStr    = req.getParameter(DIR);
        String keyStr    = req.getParameter(KEY);
        String dateStr   = req.getParameter(DATE);
        String submit    = req.getParameter(SUBMIT);
        String language  = req.getParameter(LANGUAGE);
        boolean save     = (submit != null && submit.startsWith(PERSIST)) ? true : false;   
        boolean bypass   = (submit != null && submit.startsWith(BYPASS))  ? true : false;
        boolean reload   = (submit != null && submit.startsWith(RELOAD))  ? true : false;
        boolean revert   = (submit != null && submit.startsWith(REVERT))  ? true : false;
        
        // session junk fixes a stupid refresh / reload bug, which nullifies edits being made after a reload
        // NOTE: you don't want to touch the session if editing (eg: you're editing, not reloading)
        String edit = req.getParameter(EDITS_MADE);
        HttpSession s = req.getSession();
        if(s != null && (edit == null || edit.length() < 1))
        {
            String ses  = s.getAttribute(SUBMIT) + "   ";  // force a new string to be created
            
            if(reload)      s.setAttribute(SUBMIT, RELOAD);
            else if(revert) s.setAttribute(SUBMIT, REVERT);
            else            s.setAttribute(SUBMIT, SUBMIT);

            ses = ses.trim();
            if(reload && ses.equals(RELOAD)) reload = false;
            if(revert && ses.equals(REVERT)) revert = false;
        }
        
        String diffDateStr = req.getParameter(DIFF_DATE);
        
        // configure parameters
        m_useRoute  = req.getParameter(USE_ROUTE) != null ? true : false;
        m_useKey    = req.getParameter(USE_KEY)   != null ? true : false;
        m_useDir    = req.getParameter(USE_DIR)   != null ? true : false;
        m_tableType = TableType.construct(req.getParameter(METHOD));

        // set Param member variables
        if (req.getLocale() != null) 
            m_locale = req.getLocale(); // default: call before init()             
        init(agencyStr, routeStr, dirStr, keyStr, dateStr, language);
        m_diffDate     = diffDateStr;            
        m_saveConfig   = save;
        m_bypassConfig = bypass;        
        m_reloadConfig = reload;
        m_revertTimePointConfig = revert;
        
        m_configureDataDir = req.getParameter(CONFIG_DATA_DIR);
        m_scheduleDataDir  = req.getParameter(SCHEDULE_DATA_DIR);
        
        seperateAgencyRoute();
    }

    public Params(HttpServletRequest req, String csvScheduleDataDir)
    {
        this(req);
        if(m_scheduleDataDir == null) {
            m_scheduleDataDir = csvScheduleDataDir;
        }
    }
    void init(String agencyStr, String routeStr, String dirStr, String keyStr, String dateStr, String language)
    {
        if(hasContent(agencyStr)) m_agency  = agencyStr;        
        if(hasContent(routeStr )) m_routeID = routeStr;
        if(hasContent(dirStr   )) m_dir     = DirType.construct(dirStr);
        if(hasContent(keyStr   )) m_key     = KeyType.construct(keyStr);
        if(hasContent(language )) m_locale  = new Locale(language);
        if(hasContent(dateStr  )) m_date    = dateStr;
    }
    
    private boolean hasContent(String str)
    {
        return str != null && str.length() > 0;
    }
    private boolean hasContent(Object str)
    {
        return str != null;
    }
    public boolean equal(Params p)
    {
        // are these two Param object equal?  
        
        // set to true here...and to false below if we make it through a bunch of checks
        m_update   = true;
        p.m_update = true;

        if(p == null)                                   return false;
        if(p.hasNulls())                                return false;
        // TODO -- AGENCY not collected seperately, so this is broken:
        // if(!p.m_agency.equals(m_agency))             return false;
        if(!p.m_dir.equals(m_dir))                      return false;
        if(!p.m_routeID.equals(m_routeID))              return false;
        if(!p.m_key.equals(m_key))                      return false;
        if(m_date != null && !p.m_date.equals(m_date))  return false;
        
        // if we made it here, then these two object are equal -- thus no need to update 
        m_update   = false;
        p.m_update = false;
        
        return true;
    }

    public boolean hasNulls()
    {
        if(
           // !hasContent(m_agency == null)   TODO -- AGENCY not collected seperately, so this is broken: 
           !hasContent(m_routeID) ||
           !hasContent(m_date   ) ||
           !hasContent(m_dir    ) ||
           !hasContent(m_key    ) 
        ) 
            return true;
        
        return false;
    }
    
    public void setRtDirKey(String route, DirType dir, KeyType key)
    {
        m_routeID = route;
        m_key = key;
        m_dir = dir;
        seperateAgencyRoute();
    }

    public void setParams(SelectedTableParams tp)
    {
        m_agency  = tp.getAgency();
        m_routeID = tp.getRoute();
        m_key     = tp.getKey();
        m_dir     = tp.getDir();
    }
    
    /**
     * this method seperates the agency and route name
     */
    public void seperateAgencyRoute()
    {        
        if(m_routeID.contains(Constants.AGENCY_ROUTE_SEP))
        {
            String[] n = m_routeID.split(Constants.AGENCY_ROUTE_SEP);
            if(n.length == 1)
            {
                m_routeID = n[0];
            }
            else if(n.length == 2)
            {
                m_agency = n[0];
                m_routeID  = n[1];
            }
        }
    }
    
    
    public String getAgency()
    {
        return m_agency;
    }
    public String getRouteID()
    {
        return m_routeID;
    }
    public void setRouteID(String rt)
    {
        m_routeID = rt;
    }    
    public DirType getDir()
    {
        return m_dir;
    }
    public KeyType getKey()
    {
        return m_key;
    }  
    public String getDate()
    {
        return m_date;
    }
    public String getDiffDate()
    {
        return m_diffDate;
    }
    public Locale getLocale()
    {
        return m_locale;
    }

    
    public boolean isComplete()
    {
        return !hasNulls();
    }
    public boolean isBypassConfig()
    {
        return m_bypassConfig;
    }    
    public boolean isReloadConfig()
    {
        return m_reloadConfig;
    }
    public boolean isRevertTimePointConfig()
    {
        return m_revertTimePointConfig;
    }
    public boolean isSaveConfig()
    {
        return m_saveConfig;
    }
    public TableType getTableType()
    {
        return m_tableType;
    }
    public boolean isUpdate()
    {
        return m_update;
    }
    public boolean isUseDir()
    {
        return m_useDir;
    }
    public boolean isUseKey()
    {
        return m_useKey;
    }
    public boolean isUseRoute()
    {
        return m_useRoute;
    }
    public String getLanguage()
    {
        String retVal = null;
        if(m_locale != null)
        {
            retVal = m_locale.getLanguage();
        }
        return retVal;
    }
    
    public String getConfigureDataDir()
    {
        return m_configureDataDir;
    }
    public String getScheduleDataDir()
    {
        return m_scheduleDataDir;
    }

    public void setKey(KeyType k)
    {
        m_key = k;
    }
    public void setDir(DirType d)
    {
        m_dir = d;
    }
    public void setDateIfNull(String date)
    {
        if(m_date == null || m_date.length() < 1)
        {
            m_date = date;
        }
    }
}
